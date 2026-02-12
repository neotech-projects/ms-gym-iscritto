package srl.neotech.ms_dipendenti.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.service.PrenotazioniService;

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioniController {

    @Autowired
    private PrenotazioniService prenotazioniService;

    @GetMapping("/mie-prenotazioni")
    public ResponseEntity<List<Prenotazione>> getMiePrenotazioni(
            @RequestHeader(value = "X-User-Id", required = true) Integer userId) {
        try {
            List<Prenotazione> prenotazioni = prenotazioniService.getPrenotazioniByUtenteId(userId);
            return ResponseEntity.ok(prenotazioni);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
