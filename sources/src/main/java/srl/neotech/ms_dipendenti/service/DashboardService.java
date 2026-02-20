package srl.neotech.ms_dipendenti.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dto.Prenotazione;

@Service
public class DashboardService {

    @Autowired
    private PrenotazioniService prenotazioniService;

    /** Numero di prossime prenotazioni dell'utente (card Prenotazioni). */
    public int getNumPrenotazioniProssime(Integer utenteId) {
        List<Prenotazione> list = prenotazioniService.getPrenotazioniByUtenteId(utenteId);
        return list != null ? list.size() : 0;
    }

    /** Numero di prenotazioni effettuate nel mese corrente (card Allenamenti - Questo mese). */
    public long getNumAllenamentiMeseCorrente(Integer utenteId) {
        return prenotazioniService.countPrenotazioniNelMeseCorrente(utenteId);
    }

    /** Lista delle mie prossime prenotazioni (sezione dashboard). */
    public List<Prenotazione> getMiePrenotazioni(Integer utenteId) {
        return prenotazioniService.getPrenotazioniByUtenteId(utenteId);
    }
}
