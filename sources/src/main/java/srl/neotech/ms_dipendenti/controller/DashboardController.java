package srl.neotech.ms_dipendenti.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.service.DashboardService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/prenotazioni")
    public ResponseEntity<Integer> getNumPrenotazioni(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestHeader(name = "authToken", required = true) String authToken) {
        return ResponseEntity.ok(dashboardService.getNumPrenotazioniProssime(utenteId, authToken));
    }

    @GetMapping("/allenamenti")
    public ResponseEntity<Long> getNumAllenamenti(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestHeader(name = "authToken", required = true) String authToken) {
        return ResponseEntity.ok(dashboardService.getNumAllenamentiMeseCorrente(utenteId, authToken));
    }

    @GetMapping("/mie-prenotazioni")
    public ResponseEntity<List<Prenotazione>> getMiePrenotazioni(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestHeader(name = "authToken", required = true) String authToken) {
        return ResponseEntity.ok(dashboardService.getMiePrenotazioni(utenteId, authToken));
    }

    @GetMapping("/statistiche")
    public ResponseEntity<Map<String, Object>> getStatistiche(
            @RequestParam(name = "utenteId") Integer utenteId,
            @RequestHeader(name = "authToken", required = true) String authToken) {
        return ResponseEntity.ok(dashboardService.getStatistiche(utenteId, authToken));
    }
}