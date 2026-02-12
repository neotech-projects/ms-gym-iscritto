package srl.neotech.ms_dipendenti.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dao.UtenteMapper;
import srl.neotech.ms_dipendenti.dto.Utente;


@Service
public class DashboardService {
    
    @Autowired
    private UtenteMapper utenteMapper;

    public Utente getStats() {
        return utenteMapper.selectByPrimaryKey(1);
    }
}
