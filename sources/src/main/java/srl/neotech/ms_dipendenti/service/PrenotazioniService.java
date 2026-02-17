package srl.neotech.ms_dipendenti.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dao.PrenotazioneMapper;
import srl.neotech.ms_dipendenti.dto.Prenotazione;
import srl.neotech.ms_dipendenti.dto.PrenotazioneExample;

@Service
public class PrenotazioniService {

    @Autowired
    private PrenotazioneMapper prenotazioneMapper;

    public List<Prenotazione> getPrenotazioniByUtenteId(Integer utenteId) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria().andUtenteIdEqualTo(utenteId);
        return prenotazioneMapper.selectByExample(example);
    }

    /** Numero di prenotazioni effettuate dall'utente nel mese corrente (campo creata). */
    public long countPrenotazioniNelMeseCorrente(Integer utenteId) {
        if (utenteId == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date inizioMese = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date fineMese = cal.getTime();
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria()
                .andUtenteIdEqualTo(utenteId)
                .andCreataGreaterThanOrEqualTo(inizioMese)
                .andCreataLessThan(fineMese);
        return prenotazioneMapper.countByExample(example);
    }

    /**
     * Insert iniziale in T_PRENOTAZIONI. Usa la Prenotazione dal form (data, oraInizio, durataMinuti, stato).
     */
    public Prenotazione creaPrenotazione(Integer utenteId, Prenotazione prenotazione) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        if (prenotazione == null) {
            throw new IllegalArgumentException("La prenotazione non può essere null");
        }
        prenotazione.setUtenteId(utenteId);
        prenotazione.setCreata(prenotazione.getCreata() != null ? prenotazione.getCreata() : new java.util.Date());
        prenotazione.setUsata(null);
        prenotazione.setAnnullata(null);
        prenotazione.setId(null);
        prenotazioneMapper.insertSelective(prenotazione);
        return prenotazione;
    }

    /**
     * Aggiorna il record esistente della prenotazione (per id): imposta annullata = now, usata = false.
     */
    public Prenotazione annullaPrenotazione(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID prenotazione non può essere null");
        }
        Prenotazione esistente = prenotazioneMapper.selectByPrimaryKey(id);
        if (esistente == null) {
            throw new RuntimeException("Prenotazione non trovata con id: " + id);
        }
        esistente.setAnnullata(new java.util.Date());
        esistente.setUsata(false);
        prenotazioneMapper.updateByPrimaryKeySelective(esistente);
        return esistente;
    }
}
