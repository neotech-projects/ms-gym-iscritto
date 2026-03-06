package srl.neotech.ms_dipendenti.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dao.ConfigurazioneMapper;
import srl.neotech.ms_dipendenti.dao.PrenotazioneMapper;
import srl.neotech.ms_dipendenti.dao.UtenteMapper;
import srl.neotech.ms_dipendenti.dto.Configurazione;
import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.dto.PrenotazioneExample;
import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.dto.UtenteExample;

@Service
public class PrenotazioniService {

    @Autowired
    private PrenotazioneMapper prenotazioneMapper;

    @Autowired
    private ConfigurazioneMapper configurazioneMapper;

    @Autowired
    private UtenteMapper utenteMapper;

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
    public Prenotazione checkPrenotazione(String uuid, Integer utenteId, String authToken) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("L'UUID non può essere null o vuoto");
        }
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        // Verifica che l'uuid esista in T_CONFIGURAZIONI (chiave = uuid, valore è varchar)
        Configurazione config = configurazioneMapper.selectByPrimaryKey(uuid.trim());
        if (config == null) {
            throw new RuntimeException("UUID non valido");
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
            if (dataPrenotazione.isBefore(LocalDate.now())) {
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
            LocalDateTime oraAttuale = LocalDateTime.now();
            LocalDate dataPrenotazione = prenotazione.getData() != null
                    ? new java.sql.Date(prenotazione.getData().getTime()).toLocalDate()
                    : LocalDate.now();
            LocalTime oraInizio = LocalTime.of(0, 0);
            if (prenotazione.getOraInizio() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(prenotazione.getOraInizio());
                oraInizio = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
            }
            LocalDateTime inizioPrenotazione = LocalDateTime.of(dataPrenotazione, oraInizio);
            LocalDateTime primoAccessoConsentito = inizioPrenotazione.minusMinutes(minutiPrefetch);
            if (oraAttuale.isBefore(primoAccessoConsentito)) {
                String valorePrefetch = timePrefetchConfig.getValore().trim();
                throw new RuntimeException("Troppo presto, torna entro " + valorePrefetch + " minuti (accesso consentito a partire dalle "
                        + primoAccessoConsentito.toLocalTime().toString() + ")");
            }
        }
        return prenotazione;
    }
}
