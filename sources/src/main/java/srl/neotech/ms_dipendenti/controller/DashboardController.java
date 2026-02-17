package srl.neotech.ms_dipendenti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /** Numero prossime prenotazioni (card Prenotazioni). */
    @GetMapping("/prenotazioni")
    public ResponseEntity<Integer> getNumPrenotazioni(@RequestParam(name = "userId") Integer userId) {
        return ResponseEntity.ok(dashboardService.getNumPrenotazioniProssime(userId));
    }

    /** Numero prenotazioni nel mese corrente (card Allenamenti - Questo mese). */
    @GetMapping("/allenamenti")
    public ResponseEntity<Long> getNumAllenamenti(@RequestParam(name = "userId") Integer userId) {
        return ResponseEntity.ok(dashboardService.getNumAllenamentiMeseCorrente(userId));
    }
}