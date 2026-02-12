package srl.neotech.ms_dipendenti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.LoginRequest;
import srl.neotech.ms_dipendenti.dto.LoginResponse;
import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.service.UtentiService;

@RestController
@RequestMapping("/api/utenti")
public class UtentiController {

    @Autowired
    private UtentiService utentiService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = utentiService.login(loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/profilo")
    public ResponseEntity<Utente> getProfiloUtente(
            @RequestHeader(value = "X-User-Id", required = true) Integer userId) {
        try {
            Utente profilo = utentiService.getProfiloUtente(userId);
            return ResponseEntity.ok(profilo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/profilo-by-email")
    public ResponseEntity<Utente> getProfiloUtenteByEmail(
            @RequestHeader(value = "X-User-Email", required = true) String email) {
        try {
            Utente profilo = utentiService.getProfiloUtenteByEmail(email);
            return ResponseEntity.ok(profilo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
