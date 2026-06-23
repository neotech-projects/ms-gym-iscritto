package srl.neotech.ms_dipendenti.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.CambioPasswordRequest;
import srl.neotech.ms_dipendenti.dto.LoginRequest;
import srl.neotech.ms_dipendenti.dto.LoginResponse;
import srl.neotech.ms_dipendenti.dto.ModificaProfiloRequest;
import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.service.UtentiService;

@RestController
@CrossOrigin(origins = "https://gyminvestire.neotech.srl", allowCredentials = "true")
@RequestMapping("/api/utenti")
public class UtentiController {

    private static final String SESSION_COOKIE_NAME = "ISCRITTO_AUTH_TOKEN";

    /** HTTPS + SPA cross-origin: true e same-site=None (vedi application-prod.properties). */
    @Value("${app.session.cookie.secure:false}")
    private boolean sessionCookieSecure;

    @Value("${app.session.cookie.same-site:Lax}")
    private String sessionCookieSameSite;

    @Value("${app.session.cookie.max-age-days:60}")
    private int sessionCookieMaxAgeDays;

    @Autowired
    private UtentiService utentiService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = utentiService.login(loginRequest.getEmail(), loginRequest.getPassword());
            String sameSite = sessionCookieSameSite != null ? sessionCookieSameSite.trim() : "Lax";
            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(SESSION_COOKIE_NAME, response.getToken())
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(sessionCookieMaxAgeDays))
                    .secure(sessionCookieSecure);
            if (!sameSite.isEmpty()) {
                cookieBuilder = cookieBuilder.sameSite(sameSite);
            }
            ResponseCookie cookie = cookieBuilder.build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if ("CREDENZIALI_NON_VALIDE".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            if ("PASSWORD_NON_IMPOSTATA".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/profilo")
    public ResponseEntity<Utente> getProfiloUtente(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestHeader(name = "authToken", required = true) String authToken) {
        try {
            Utente profilo = utentiService.getProfiloUtente(utenteId, authToken);
            return ResponseEntity.ok(profilo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if ("UTENTE_NON_AUTORIZZATO".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/profilo-by-email")
    public ResponseEntity<Utente> getProfiloUtenteByEmail(
            @RequestParam(name = "email") String email,
            @RequestHeader(name = "authToken", required = true) String authToken) {
        try {
            Utente profilo = utentiService.getProfiloUtenteByEmail(email, authToken);
            return ResponseEntity.ok(profilo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if ("UTENTE_NON_AUTORIZZATO".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/cambia-password")
    public ResponseEntity<Void> cambiaPassword(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestBody CambioPasswordRequest request) {
        try {
            utentiService.cambiaPassword(utenteId, request.getVecchiaPassword(), request.getNuovaPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if ("VECCHIA_PASSWORD_NON_VALIDA".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/modifica-profilo")
    public ResponseEntity<Utente> modificaProfilo(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestBody ModificaProfiloRequest request) {
        try {
            Utente utente = utentiService.modificaProfilo(utenteId, request);
            return ResponseEntity.ok(utente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
