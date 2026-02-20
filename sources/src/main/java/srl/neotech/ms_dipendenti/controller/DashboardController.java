package srl.neotech.ms_dipendenti.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/prenotazioni")
    public ResponseEntity<Integer> getNumPrenotazioni(@RequestParam(name = "utenteId") Integer utenteId) {
        return ResponseEntity.ok(dashboardService.getNumPrenotazioniProssime(utenteId));
    }

    @GetMapping("/allenamenti")
    public ResponseEntity<Long> getNumAllenamenti(@RequestParam(name = "utenteId") Integer utenteId) {
        return ResponseEntity.ok(dashboardService.getNumAllenamentiMeseCorrente(utenteId));
    }

    @GetMapping("/mie-prenotazioni")
    public ResponseEntity<List<Prenotazione>> getMiePrenotazioni(@RequestParam(name = "utenteId") Integer utenteId) {
        return ResponseEntity.ok(dashboardService.getMiePrenotazioni(utenteId));
    }
}