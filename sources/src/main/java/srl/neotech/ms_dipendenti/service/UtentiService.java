package srl.neotech.ms_dipendenti.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dao.UtenteMapper;
import srl.neotech.ms_dipendenti.dto.LoginResponse;
import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.dto.UtenteExample;

@Service
public class UtentiService {
    
    @Autowired
    private UtenteMapper utenteMapper;

    public Utente getProfiloUtente(Integer utenteId) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        Utente utente = utenteMapper.selectByPrimaryKey(utenteId);
        if (utente == null) {
            throw new RuntimeException("Utente non trovato con ID: " + utenteId);
        }
        return utente;
    }

    public Utente getProfiloUtenteByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email non può essere null o vuota");
        }
        UtenteExample example = new UtenteExample();
        example.createCriteria().andEmailEqualTo(email);
        List<Utente> utenti = utenteMapper.selectByExample(example);
        if (utenti == null || utenti.isEmpty()) {
            throw new RuntimeException("Utente non trovato con email: " + email);
        }
        return utenti.get(0);
    }

    public LoginResponse login(String email, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La password è obbligatoria");
        }
        Utente utente = getProfiloUtenteByEmail(email);
        String passwordDb = utente.getPassword();
        if (passwordDb == null || passwordDb.isBlank()) {
            throw new RuntimeException("PASSWORD_NON_IMPOSTATA");
        }
        if (!password.equals(passwordDb)) {
            throw new RuntimeException("CREDENZIALI_NON_VALIDE");
        }
        return new LoginResponse(utente.getId(), utente.getNome(), utente.getCognome());
    }
}
