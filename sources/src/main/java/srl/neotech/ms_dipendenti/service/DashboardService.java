package srl.neotech.ms_dipendenti.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dto.Prenotazione;

@Service
public class DashboardService {

    @Autowired
    private PrenotazioniService prenotazioniService;

    /** Numero di prossime prenotazioni dell'utente (card Prenotazioni). */
    public int getNumPrenotazioniProssime(Integer utenteId, String authToken) {
        List<Prenotazione> list = prenotazioniService.getPrenotazioniByUtenteId(utenteId, authToken);
        return list != null ? list.size() : 0;
    }

    /** Numero di prenotazioni effettuate nel mese corrente (card Allenamenti - Questo mese). */
    public long getNumAllenamentiMeseCorrente(Integer utenteId, String authToken) {
        return prenotazioniService.countPrenotazioniNelMeseCorrente(utenteId, authToken);
    }

    /** Lista delle mie prossime prenotazioni (sezione dashboard). */
    public List<Prenotazione> getMiePrenotazioni(Integer utenteId, String authToken) {
        return prenotazioniService.getPrenotazioniByUtenteId(utenteId, authToken);
    }

    /** Statistiche dashboard: prenotazioni e allenamenti in una sola chiamata (chiavi per frontend). */
    public Map<String, Object> getStatistiche(Integer utenteId, String authToken) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("prenotazioni", getNumPrenotazioniProssime(utenteId, authToken));
        stats.put("allenamenti", getNumAllenamentiMeseCorrente(utenteId, authToken));
        return stats;
    }
}
