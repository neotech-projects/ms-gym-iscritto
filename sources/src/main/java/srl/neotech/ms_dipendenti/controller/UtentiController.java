package srl.neotech.ms_dipendenti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@CrossOrigin(origins = "*")
@RequestMapping("/api/utenti")
public class UtentiController {

    @Autowired
    private UtentiService utentiService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = utentiService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
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
    public ResponseEntity<Utente> getProfiloUtente(@RequestParam(name = "utenteId") Integer utenteId) {
        try {
            Utente profilo = utentiService.getProfiloUtente(utenteId);
            return ResponseEntity.ok(profilo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/profilo-by-email")
    public ResponseEntity<Utente> getProfiloUtenteByEmail(@RequestParam(name = "email") String email) {
        try {
            Utente profilo = utentiService.getProfiloUtenteByEmail(email);
            return ResponseEntity.ok(profilo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
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
