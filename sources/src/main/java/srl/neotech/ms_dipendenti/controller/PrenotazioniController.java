package srl.neotech.ms_dipendenti.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.service.PrenotazioniService;
import srl.neotech.ms_dipendenti.service.UtentiService;

@RestController
@CrossOrigin(origins = "https://gyminvestire.neotech.srl", allowCredentials = "true")
@RequestMapping("/api/prenotazioni")
public class PrenotazioniController {

    private static final String SESSION_COOKIE_NAME = "ISCRITTO_AUTH_TOKEN";

    @Autowired
    private PrenotazioniService prenotazioniService;

    @Autowired
    private UtentiService utentiService;

    @GetMapping("/test")
    public String test() {
        return "ciao";
    }

    @GetMapping("/mie-prenotazioni")
    public ResponseEntity<List<Prenotazione>> getMiePrenotazioni(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestHeader(name = "authToken", required = true) String authToken) {
        try {
            List<Prenotazione> prenotazioni = prenotazioniService.getPrenotazioniByUtenteId(utenteId, authToken);
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
    public ResponseEntity<List<Prenotazione>> getStoricoPrenotazioni(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestHeader(name = "authToken", required = true) String authToken) {
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

    @PostMapping("/check-prenotazione")
    public ResponseEntity<String> checkPrenotazione(
            @RequestParam(name = "uuid_door") String uuid_door,
            @RequestParam(name = "utenteId", required = false) Integer utenteId,
            @RequestHeader(name = "authToken", required = false) String authToken,
            @CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionCookie) {
        try {
            String token = null;
            if (authToken != null && !authToken.isBlank()) {
                token = authToken.trim();
            } else if (sessionCookie != null && !sessionCookie.isBlank()) {
                token = sessionCookie.trim();
            }
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessione richiesta");
            }
            Integer resolvedUtenteId = utenteId;
            if (resolvedUtenteId == null) {
                try {
                    Utente u = utentiService.findUtenteByAuthToken(token);
                    resolvedUtenteId = u.getId();
                } catch (RuntimeException e) {
                    if ("UTENTE_NON_TROVATO".equals(e.getMessage())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessione non valida");
                    }
                    throw e;
                }
            }
            prenotazioniService.checkPrenotazione(uuid_door, resolvedUtenteId, token);
            return ResponseEntity.ok("ok");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Richiesta non valida";
            return ResponseEntity.badRequest().body(msg);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Operazione non consentita";
            return ResponseEntity.badRequest().body(msg);
        } catch (Exception e) {
            String msg = e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "Errore di sistema";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
        }
    }
}
