package srl.neotech.ms_dipendenti.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import jakarta.mail.internet.MimeMessage;

import srl.neotech.ms_dipendenti.dao.AccessoMapper;
import srl.neotech.ms_dipendenti.dao.ConfigurazioneMapper;
import srl.neotech.ms_dipendenti.dao.PrenotazioneMapper;
import srl.neotech.ms_dipendenti.dao.UtenteMapper;
import srl.neotech.ms_dipendenti.dto.Accesso;
import srl.neotech.ms_dipendenti.dto.Configurazione;
import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.dto.PrenotazioneExample;
import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.dto.UtenteExample;

@Service
public class PrenotazioniService {

    private static final Logger log = LoggerFactory.getLogger(PrenotazioniService.class);

    private static final DateTimeFormatter DATA_IT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN);
    private static final DateTimeFormatter ORA = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${shelly.id}")
    private String shellyId;

    @Value("${shelly.key}")
    private String shellyKey;

    @Value("${spring.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.task.scheduling.time-zone:Europe/Rome}")
    private String schedulingTimeZone;

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Autowired
    private PrenotazioneMapper prenotazioneMapper;

    @Autowired
    private ConfigurazioneMapper configurazioneMapper;

    @Autowired
    private UtenteMapper utenteMapper;

    @Autowired
    private AccessoMapper accessoMapper;

    @Autowired
    private RestTemplate restTemplate;

    private static final String SHELLY_DEVICE_STATUS_URL = "https://shelly-250-eu.shelly.cloud/device/status";
    private static final String SHELLY_RELAY_CONTROL_URL = "https://shelly-250-eu.shelly.cloud/device/relay/control";

    private static final JsonMapper SHELLY_JSON = JsonMapper.builder().build();

    /**
     * Ogni giorno invia un promemoria HTML
     * per ogni prenotazione valida con data uguale al giorno corrente.
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "${spring.task.scheduling.time-zone:Europe/Rome}")
    public void sendRemainderPrenotazione() {
        if (javaMailSender == null) {
            log.warn("JavaMailSender non configurato: promemoria prenotazioni saltato");
            return;
        }
        String email = mittenteEmail();
        if (email == null || email.isBlank()) {
            log.warn("spring.mail.from e spring.mail.username vuoti: promemoria prenotazioni saltato");
            return;
        }
        List<Prenotazione> oggi = findPrenotazioniValidePerData(LocalDate.now());
        if (oggi.isEmpty()) {
            log.info("Promemoria prenotazioni: nessuna prenotazione valida per oggi");
            return;
        }
        int inviate = 0;
        for (Prenotazione prenotazione : oggi) {
            try {
                if (prenotazione.getUtenteId() == null) {
                    continue;
                }
                Utente utente = utenteMapper.selectByPrimaryKey(prenotazione.getUtenteId());
                if (utente == null || utente.getEmail() == null || utente.getEmail().isBlank()) {
                    log.warn("Utente {} senza email: prenotazione {} saltata", prenotazione.getUtenteId(), prenotazione.getId());
                    continue;
                }
                String html = buildReminderHtml(utente, prenotazione);
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
                helper.setFrom(email);
                helper.setTo(utente.getEmail().trim());
                helper.setSubject("Promemoria: la tua prenotazione in palestra");
                helper.setText(html, true);
                javaMailSender.send(message);
                inviate++;
            } catch (Exception e) {
                log.error("Errore invio promemoria per prenotazione id={}: {}", prenotazione.getId(), e.getMessage(), e);
            }
        }
        log.info("Promemoria prenotazioni: inviate {} email su {} prenotazioni", inviate, oggi.size());
    }

    /**
     * Prenotazioni non annullate, non usate, con data uguale a {@code data}.
     */
    private List<Prenotazione> findPrenotazioniValidePerData(LocalDate data) {
        Date sqlData = Date.valueOf(data);
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria()
                .andDataEqualTo(sqlData)
                .andAnnullataIsNull()
                .andUsataIsNull();
        example.or(example.createCriteria()
                .andDataEqualTo(sqlData)
                .andAnnullataIsNull()
                .andUsataEqualTo(false));
        example.setOrderByClause("ora_inizio ASC");
        return prenotazioneMapper.selectByExample(example);
    }

    private String mittenteEmail() {
        if (mailFrom != null && !mailFrom.isBlank()) {
            return mailFrom.trim();
        }
        return mailUsername != null ? mailUsername.trim() : "";
    }

    private static String buildReminderHtml(Utente utente, Prenotazione prenotazione) {
        LocalDate giorno = prenotazione.getData() != null
                ? new Date(prenotazione.getData().getTime()).toLocalDate()
                : LocalDate.now();
        String dataTesto = DATA_IT.format(giorno);
        String oraTesto = prenotazione.getOraInizio() != null ? ORA.format(prenotazione.getOraInizio()) : "—";
        String durata = prenotazione.getDurataMinuti() != null
                ? prenotazione.getDurataMinuti() + " minuti"
                : "—";
        String nome = escapeHtml(utente.getNome());
        String cognome = escapeHtml(utente.getCognome());
        return """
                <!DOCTYPE html>
                <html lang="it">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <link href="https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,400;0,9..40,600;1,9..40,400&display=swap" rel="stylesheet" />
                  <title>Promemoria prenotazione</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f4f2ee;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f2ee;padding:24px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" style="max-width:520px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 8px 24px rgba(30,40,50,0.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#1a3a4a 0%%,#2d5a6e 100%%);padding:28px 24px;">
                              <p style="margin:0;font-family:'DM Sans',system-ui,Segoe UI,Helvetica,Arial,sans-serif;font-size:13px;letter-spacing:0.12em;text-transform:uppercase;color:rgba(255,255,255,0.85);">Promemoria</p>
                              <h1 style="margin:8px 0 0;font-family:'DM Sans',system-ui,Segoe UI,Helvetica,Arial,sans-serif;font-size:22px;font-weight:600;color:#ffffff;line-height:1.3;">La tua sessione in palestra</h1>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 24px 8px;font-family:'DM Sans',system-ui,Segoe UI,Helvetica,Arial,sans-serif;font-size:16px;line-height:1.6;color:#2c3338;">
                              <p style="margin:0 0 16px;">Ciao <strong>%s %s</strong>,</p>
                              <p style="margin:0 0 16px;">Ti ricordiamo che hai una <strong>prenotazione confermata</strong> per oggi.</p>
                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f8f9fa;border-radius:8px;border:1px solid #e8eaed;">
                                <tr>
                                  <td style="padding:16px 18px;font-family:'DM Sans',system-ui,Segoe UI,Helvetica,Arial,sans-serif;font-size:15px;color:#2c3338;">
                                    <p style="margin:0 0 8px;"><span style="color:#5f6b73;">Data</span><br /><strong style="color:#1a3a4a;">%s</strong></p>
                                    <p style="margin:0 0 8px;"><span style="color:#5f6b73;">Ora di inizio</span><br /><strong style="color:#1a3a4a;">%s</strong></p>
                                    <p style="margin:0;"><span style="color:#5f6b73;">Durata</span><br /><strong style="color:#1a3a4a;">%s</strong></p>
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:20px 0 0;font-size:14px;color:#5f6b73;">Arriva qualche minuto prima dell’orario indicato. Buon allenamento!</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:0 24px 28px;font-family:'DM Sans',system-ui,Segoe UI,Helvetica,Arial,sans-serif;font-size:12px;color:#9aa3a9;">
                              Messaggio automatico, non rispondere a questa email.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(nome, cognome, dataTesto, oraTesto, durata);
    }

    private static String escapeHtml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Prenotazioni dell'utente: prenotate ma non ancora usate e non annullate.
     */
    public List<Prenotazione> getPrenotazioniByUtenteId(Integer utenteId, String authToken) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        UtenteExample exUtente = new UtenteExample();
        exUtente.createCriteria().andIdEqualTo(utenteId).andTokenEqualTo(authToken.trim());
        List<Utente> utenti = utenteMapper.selectByExample(exUtente);
        if (utenti == null || utenti.isEmpty()) {
            throw new IllegalArgumentException("L'utente non è autorizzato a vedere le prenotazioni");
        }
        LocalDate oggi = LocalDate.now();
        java.sql.Date dataOggi = Date.valueOf(oggi);
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria()
                .andUtenteIdEqualTo(utenteId)
                .andDataGreaterThanOrEqualTo(dataOggi)
                .andAnnullataIsNull()
                .andUsataIsNull();
        example.or(example.createCriteria()
                .andUtenteIdEqualTo(utenteId)
                .andDataGreaterThanOrEqualTo(dataOggi)
                .andAnnullataIsNull()
                .andUsataEqualTo(false));
        example.setOrderByClause("data ASC, ora_inizio ASC");
        return prenotazioneMapper.selectByExample(example);
    }

    /**
     * Ritorna tutte le prenotazioni future e passate dell'utente (storico prenotazioni),
     * ordinate per data e ora inizio decrescente (le più recenti per prime).
     */
    public List<Prenotazione> getStoricoPrenotazioni(Integer utenteId, String authToken) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        UtenteExample exUtente = new UtenteExample();
        exUtente.createCriteria().andIdEqualTo(utenteId).andTokenEqualTo(authToken);
        List<Utente> utenti = utenteMapper.selectByExample(exUtente);
        if (utenti == null || utenti.isEmpty()) {
            throw new IllegalArgumentException("L'utente non è autorizzato a vedere le prenotazioni");
        }
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria().andUtenteIdEqualTo(utenteId);
        example.setOrderByClause("data DESC, ora_inizio DESC");
        return prenotazioneMapper.selectByExample(example);
    }

    /**
     * Tutte le prenotazioni del mese corrente per qualsiasi utente: non annullate e non usate.
     * Anno e mese sono ricavati dalla data odierna.
     */
    public List<Prenotazione> getPrenotazioniGenerali() {
        LocalDate oggi = LocalDate.now();
        LocalDate inizio = oggi.withDayOfMonth(1);
        LocalDate fine = inizio.plusMonths(1);
        Date dataDa = Date.valueOf(inizio);
        Date dataA = Date.valueOf(fine);
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria()
                .andDataGreaterThanOrEqualTo(dataDa)
                .andDataLessThan(dataA)
                .andAnnullataIsNull()
                .andUsataIsNull();
        example.or(example.createCriteria()
                .andDataGreaterThanOrEqualTo(dataDa)
                .andDataLessThan(dataA)
                .andAnnullataIsNull()
                .andUsataEqualTo(false));
        example.setOrderByClause("data ASC, ora_inizio ASC");
        return prenotazioneMapper.selectByExample(example);
    }

    /** Numero di prenotazioni con data nel mese corrente (usa campo data, solo date senza timezone). */
    public long countPrenotazioniNelMeseCorrente(Integer utenteId, String authToken) {
        if (utenteId == null) {
            return 0;
        }
        UtenteExample exUtente = new UtenteExample();
        exUtente.createCriteria().andIdEqualTo(utenteId).andTokenEqualTo(authToken.trim());
        List<Utente> utenti = utenteMapper.selectByExample(exUtente);
        if (utenti == null || utenti.isEmpty()) {
            throw new IllegalArgumentException("L'utente non è autorizzato");
        }
        LocalDate oggi = LocalDate.now();
        Date inizioMese = Date.valueOf(oggi.withDayOfMonth(1));
        Date fineMese = Date.valueOf(oggi.withDayOfMonth(1).plusMonths(1));
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria()
                .andUtenteIdEqualTo(utenteId)
                .andDataGreaterThanOrEqualTo(inizioMese)
                .andDataLessThan(fineMese);
        return prenotazioneMapper.countByExample(example);
    }

    /**
     * Insert iniziale in T_PRENOTAZIONI. Usa la Prenotazione dal form (data, oraInizio, durataMinuti).
     * Imposta stato = "Confermata" (non ancora annullata o usata).
     */
    public Prenotazione creaPrenotazione(Integer utenteId, Prenotazione prenotazione) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        if (prenotazione == null) {
            throw new IllegalArgumentException("La prenotazione non può essere null");
        }
        prenotazione.setUtenteId(utenteId);
        prenotazione.setCreata(prenotazione.getCreata() != null ? prenotazione.getCreata() : new java.util.Date());
        prenotazione.setUsata(null);
        prenotazione.setAnnullata(null);
        prenotazione.setId(null);
        prenotazione.setStato("Confermata");
        prenotazioneMapper.insertSelective(prenotazione);
        return prenotazione;
    }

    /**
     * Aggiorna il record esistente della prenotazione (per id): imposta annullata = now, usata = false, stato = "Annullata".
     */
    public Prenotazione annullaPrenotazione(Integer idPrenotazione) {
        if (idPrenotazione == null) {
            throw new IllegalArgumentException("L'ID prenotazione non può essere null");
        }
        Prenotazione prenotazione = prenotazioneMapper.selectByPrimaryKey(idPrenotazione);
        if (prenotazione == null) {
            throw new RuntimeException("Prenotazione non trovata con id: " + idPrenotazione);
        }
        prenotazione.setAnnullata(new java.util.Date());
        prenotazione.setUsata(false);
        prenotazione.setStato("Annullata");
        prenotazioneMapper.updateByPrimaryKeySelective(prenotazione);
        return prenotazione;
    }

    /**
     * Verifica la prenotazione in base all'UUID (dal QR) e utenteId.
     * Con utenteId si va su T_PRENOTAZIONI e si prende la prenotazione dell'utente.
     * In T_CONFIGURAZIONI si verifica che l'uuid corrisponda (chiave = uuid; valore è varchar, non id).
     */
    public Prenotazione checkPrenotazione(String uuid_door, Integer utenteId, String authToken) {
        if (uuid_door == null || uuid_door.isBlank()) {
            throw new IllegalArgumentException("L'UUID non può essere null o vuoto");
        }
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        ZoneId zone = ZoneId.of(schedulingTimeZone);
        // Verifica che l'uuid esista in T_CONFIGURAZIONI (chiave = uuid, valore è varchar)
        Configurazione config = configurazioneMapper.selectByPrimaryKey("QRCODE_UUID");
        if (config == null || !config.getValore().equals(uuid_door)) {
            throw new RuntimeException("UUID non valido");
        }
        Utente utente = utenteMapper.selectByPrimaryKey(utenteId);
        if (utente == null || utente.getToken() == null || !utente.getToken().equals(authToken.trim())) {
            throw new IllegalArgumentException("L'utente non è autorizzato a vedere le prenotazioni");
        }
        String tipoUtente = utente.getTipoUtente();
        if (tipoUtente != null && tipoUtente.trim().equalsIgnoreCase("personal trainer")) {
            try {
                apriPorta();
                registraAccesso(utenteId, null, true);
                return null;
            } catch (Exception e) {
                registraAccesso(utenteId, null, false);
                throw e;
            }
        }
        // Da T_PRENOTAZIONI si prende la prenotazione per utenteId (la prima valida)
        List<Prenotazione> prenotazioni = getPrenotazioniByUtenteId(utenteId, authToken);
        if (prenotazioni == null || prenotazioni.isEmpty()) {
            throw new RuntimeException("Nessuna prenotazione trovata per l'utente");
        }
        Prenotazione prenotazione = prenotazioni.get(0);
        if (prenotazione.getAnnullata() != null) {
            throw new RuntimeException("Prenotazione annullata");
        }
        if (prenotazione.getData() != null) {
            LocalDate dataPrenotazione = new java.sql.Date(prenotazione.getData().getTime()).toLocalDate();
            if (dataPrenotazione.isBefore(LocalDate.now(zone))) {
                throw new RuntimeException("Prenotazione scaduta");
            }
        }
        // Controllo TIME_PREFETCH: limite massimo di minuti prima in cui l'utente può accedere
        Configurazione timePrefetchConfig = configurazioneMapper.selectByPrimaryKey("TIME_PREFETCH");
        if (timePrefetchConfig != null && timePrefetchConfig.getValore() != null && !timePrefetchConfig.getValore().isBlank()) {
            int minutiPrefetch;
            try {
                minutiPrefetch = Integer.parseInt(timePrefetchConfig.getValore().trim());
            } catch (NumberFormatException e) {
                minutiPrefetch = 0;
            }
            LocalDateTime oraAttuale = LocalDateTime.now(zone);
            LocalDate dataPrenotazione = prenotazione.getData() != null
                    ? new java.sql.Date(prenotazione.getData().getTime()).toLocalDate()
                    : LocalDate.now(zone);
            LocalTime oraInizio = LocalTime.of(0, 0);
            if (prenotazione.getOraInizio() != null) {
                oraInizio = prenotazione.getOraInizio();
            }
            LocalDateTime inizioPrenotazione = LocalDateTime.of(dataPrenotazione, oraInizio);
            LocalDateTime primoAccessoConsentito = inizioPrenotazione.minusMinutes(minutiPrefetch);
            if (oraAttuale.isBefore(primoAccessoConsentito)) {
                String valorePrefetch = timePrefetchConfig.getValore().trim();
                throw new RuntimeException("Troppo presto, torna entro " + valorePrefetch + " minuti (accesso consentito a partire dalle "
                        + primoAccessoConsentito.toLocalTime().toString() + ")");
            }
        }
        try {
            apriPorta();
            registraAccesso(utenteId, prenotazione.getId(), true);
            return prenotazione;
        } catch (Exception e) {
            registraAccesso(utenteId, prenotazione.getId(), false);

            throw new RuntimeException("Impossibile aprire la porta: dispositivo Shelly non pronto");
        }
    }

    private void registraAccesso(Integer utenteId, Integer prenotazioneId, boolean portaAperta) {
        Accesso accesso = new Accesso();
        accesso.setUtenteId(utenteId);
        accesso.setPrenotazioneId(prenotazioneId);
        accesso.setDataOraAccesso(new java.util.Date());
        accesso.setEsito(portaAperta ? "ok" : "ko");
        accessoMapper.insertSelective(accesso);
        if (portaAperta && prenotazioneId != null) {
            Prenotazione prenotazione = new Prenotazione();
            prenotazione.setId(prenotazioneId);
            prenotazione.setUsata(true);
            prenotazioneMapper.updateByPrimaryKeySelective(prenotazione);
        }
    }

    private void apriPorta() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> statusParams = new LinkedMultiValueMap<>();
        statusParams.add("id", shellyId);
        statusParams.add("auth_key", shellyKey);
        HttpEntity<MultiValueMap<String, String>> statusRequest = new HttpEntity<>(statusParams, headers);
        ResponseEntity<String> statusResponse = restTemplate.postForEntity(
                SHELLY_DEVICE_STATUS_URL,
                statusRequest,
                String.class);

        String statusBody = statusResponse.getBody();
        if (statusBody == null || statusBody.isBlank()) {
            throw new RuntimeException("Shelly: risposta stato vuota");
        }
        JsonNode root;
        try {
            root = SHELLY_JSON.readTree(statusBody);
        } catch (Exception e) {
            throw new RuntimeException("Shelly: risposta stato non valida", e);
        }
        if (!root.path("isok").asBoolean(false)) {
            throw new RuntimeException("Shelly: richiesta stato fallita (isok=false)");
        }
        JsonNode data = root.path("data");
        boolean online = data.path("online").asBoolean(false);
        JsonNode deviceStatus = data.path("device_status");
        boolean ison = relayChannelIson(deviceStatus, 0);

        if (!online || !ison) {
            throw new RuntimeException(
                    "Impossibile aprire la porta: dispositivo Shelly non pronto (online=" + online + ", ison=" + ison + ")");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", shellyId);
        params.add("turn", "on");
        params.add("channel", "0");
        params.add("auth_key", shellyKey);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                SHELLY_RELAY_CONTROL_URL,
                request,
                String.class);
        log.debug("Shelly relay/control: {}", response.getBody());
    }

    /**
     * Stato accensione del canale relay: Gen1 {@code relays[i].ison}, Gen2 {@code switch:i.output}.
     */
    private static boolean relayChannelIson(JsonNode deviceStatus, int channel) {
        if (deviceStatus == null || deviceStatus.isMissingNode()) {
            return false;
        }
        JsonNode relays = deviceStatus.path("relays");
        if (relays.isArray() && relays.size() > channel) {
            return relays.get(channel).path("ison").asBoolean(false);
        }
        JsonNode sw = deviceStatus.path("switch:" + channel);
        if (!sw.isMissingNode()) {
            return sw.path("output").asBoolean(false);
        }
        return false;
    }
}
