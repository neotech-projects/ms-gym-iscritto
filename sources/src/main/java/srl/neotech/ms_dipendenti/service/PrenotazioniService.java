package srl.neotech.ms_dipendenti.service;

import java.sql.Date;
import java.time.LocalDate;
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

    /**
     * Prenotazioni dell'utente: prenotate ma non ancora usate e non annullate.
     */
    public List<Prenotazione> getPrenotazioniByUtenteId(Integer utenteId) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        LocalDate oggi = LocalDate.now();
        java.sql.Date dataOggi = Date.valueOf(oggi);
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria()
                .andUtenteIdEqualTo(utenteId)
                .andDataGreaterThanOrEqualTo(dataOggi)
                .andAnnullataIsNull()
                .andUsataIsNull();
        example.or(example.createCriteria()
                .andUtenteIdEqualTo(utenteId)
                .andDataGreaterThanOrEqualTo(dataOggi)
                .andAnnullataIsNull()
                .andUsataEqualTo(false));
        example.setOrderByClause("data ASC, ora_inizio ASC");
        return prenotazioneMapper.selectByExample(example);
    }

    /**
     * Ritorna tutte le prenotazioni future e passate dell'utente (storico prenotazioni),
     * ordinate per data e ora inizio decrescente (le più recenti per prime).
     */
    public List<Prenotazione> getStoricoPrenotazioni(Integer utenteId) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria().andUtenteIdEqualTo(utenteId);
        example.setOrderByClause("data DESC, ora_inizio DESC");
        return prenotazioneMapper.selectByExample(example);
    }

    /** Numero di prenotazioni con data nel mese corrente (usa campo data, solo date senza timezone). */
    public long countPrenotazioniNelMeseCorrente(Integer utenteId) {
        if (utenteId == null) {
            return 0;
        }
        LocalDate oggi = LocalDate.now();
        Date inizioMese = Date.valueOf(oggi.withDayOfMonth(1));
        Date fineMese = Date.valueOf(oggi.withDayOfMonth(1).plusMonths(1));
        PrenotazioneExample example = new PrenotazioneExample();
        example.createCriteria()
                .andUtenteIdEqualTo(utenteId)
                .andDataGreaterThanOrEqualTo(inizioMese)
                .andDataLessThan(fineMese);
        return prenotazioneMapper.countByExample(example);
    }

    /**
     * Insert iniziale in T_PRENOTAZIONI. Usa la Prenotazione dal form (data, oraInizio, durataMinuti).
     * Imposta stato = "Confermata" (non ancora annullata o usata).
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
        prenotazione.setStato("Confermata");
        prenotazioneMapper.insertSelective(prenotazione);
        return prenotazione;
    }

    /**
     * Aggiorna il record esistente della prenotazione (per id): imposta annullata = now, usata = false, stato = "Annullata".
     */
    public Prenotazione annullaPrenotazione(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID prenotazione non può essere null");
        }
        Prenotazione prenotazione = prenotazioneMapper.selectByPrimaryKey(id);
        if (prenotazione == null) {
            throw new RuntimeException("Prenotazione non trovata con id: " + id);
        }
        prenotazione.setAnnullata(new java.util.Date());
        prenotazione.setUsata(false);
        prenotazione.setStato("Annullata");
        prenotazioneMapper.updateByPrimaryKeySelective(prenotazione);
        return prenotazione;
    }
}
