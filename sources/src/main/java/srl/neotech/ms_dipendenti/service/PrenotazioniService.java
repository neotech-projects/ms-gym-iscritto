package srl.neotech.ms_dipendenti.service;

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
}
