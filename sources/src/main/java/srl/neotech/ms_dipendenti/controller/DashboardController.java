package srl.neotech.ms_dipendenti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Utente getStats() {
        return dashboardService.getStats();
    }


}