package srl.neotech.ms_dipendenti.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dto.Prenotazione;

@Service
public class DashboardService {

    @Autowired
    private PrenotazioniService prenotazioniService;

    /** Numero di prossime prenotazioni dell'utente (card Prenotazioni). */
    public int getNumPrenotazioniProssime(Integer userId) {
        java.util.List<Prenotazione> list = prenotazioniService.getPrenotazioniByUtenteId(userId);
        return list != null ? list.size() : 0;
    }

    /** Numero di prenotazioni effettuate nel mese corrente (card Allenamenti - Questo mese). */
    public long getNumAllenamentiMeseCorrente(Integer userId) {
        return prenotazioniService.countPrenotazioniNelMeseCorrente(userId);
    }
}
