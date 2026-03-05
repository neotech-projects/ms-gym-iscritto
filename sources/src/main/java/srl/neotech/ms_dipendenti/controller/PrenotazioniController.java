package srl.neotech.ms_dipendenti.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.service.PrenotazioniService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/prenotazioni")
public class PrenotazioniController {

    @Autowired
    private PrenotazioniService prenotazioniService;

    @GetMapping("/mie-prenotazioni")
    public ResponseEntity<List<Prenotazione>> getMiePrenotazioni(@RequestParam(name = "utenteId") Integer utenteId) {
        try {
            List<Prenotazione> prenotazioni = prenotazioniService.getPrenotazioniByUtenteId(utenteId);
            return ResponseEntity.ok(prenotazioni);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/generali")
    public ResponseEntity<List<Prenotazione>> getPrenotazioniGenerali() {
        try {
            List<Prenotazione> prenotazioni = prenotazioniService.getPrenotazioniGenerali();
            return ResponseEntity.ok(prenotazioni);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/storico")
    public ResponseEntity<List<Prenotazione>> getStoricoPrenotazioni(@RequestParam(name = "utenteId") Integer utenteId, @RequestParam(name= "auth_token") String authToken) {
        try {
            List<Prenotazione> prenotazioni = prenotazioniService.getStoricoPrenotazioni(utenteId, authToken);
            return ResponseEntity.ok(prenotazioni);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Prenotazione> creaPrenotazione(@RequestParam(name = "utenteId") Integer utenteId, @RequestBody Prenotazione prenotazione) {
        try {
            Prenotazione created = prenotazioniService.creaPrenotazione(utenteId, prenotazione);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping
    public ResponseEntity<Prenotazione> annullaPrenotazione(@RequestParam(name = "idPrenotazione") Integer idPrenotazione) {
        try {
            Prenotazione aggiornata = prenotazioniService.annullaPrenotazione(idPrenotazione);
            return ResponseEntity.ok(aggiornata);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static final String APRIPORTA_BASE_URL = "http://cloud.neotech.srl/spa/apriporta";

    /**
     * Verifica la prenotazione in seguito alla scansione del QR code.
     * Redirect a cloud.neotech.srl/spa/apriporta?UUID=... con esito e messaggio (porta aperta, nessuna prenotazione, ecc.).
     */
    @PostMapping("/check-prenotazione")
    public ResponseEntity<Void> checkPrenotazione(
            @RequestParam(name = "uuid") String uuid,
            @RequestParam(name = "utenteId") Integer utenteId) {
        try {
            prenotazioniService.checkPrenotazione(uuid, utenteId);
            String redirectUrl = APRIPORTA_BASE_URL + "?UUID=" + URLEncoder.encode(uuid, StandardCharsets.UTF_8)
                    + "&esito=ok&messaggio=" + URLEncoder.encode("Porta aperta", StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        } catch (IllegalArgumentException e) {
            String redirectUrl = APRIPORTA_BASE_URL + "?UUID=" + URLEncoder.encode(uuid, StandardCharsets.UTF_8)
                    + "&esito=ko&messaggio=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        } catch (RuntimeException e) {
            String redirectUrl = APRIPORTA_BASE_URL + "?UUID=" + URLEncoder.encode(uuid, StandardCharsets.UTF_8)
                    + "&esito=ko&messaggio=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        } catch (Exception e) {
            String redirectUrl = APRIPORTA_BASE_URL + "?UUID=" + URLEncoder.encode(uuid, StandardCharsets.UTF_8)
                    + "&esito=ko&messaggio=" + URLEncoder.encode("Errore di sistema", StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        }
    }
}
