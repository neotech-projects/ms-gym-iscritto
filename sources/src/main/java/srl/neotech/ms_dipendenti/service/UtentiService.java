package srl.neotech.ms_dipendenti.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srl.neotech.ms_dipendenti.dao.SocietaMapper;
import srl.neotech.ms_dipendenti.dao.UtenteMapper;
import srl.neotech.ms_dipendenti.dto.LoginResponse;
import srl.neotech.ms_dipendenti.dto.ModificaProfiloRequest;
import srl.neotech.ms_dipendenti.dto.Societa;
import srl.neotech.ms_dipendenti.dto.Utente;
import srl.neotech.ms_dipendenti.dto.UtenteExample;

@Service
public class UtentiService {
    
    @Autowired
    private UtenteMapper utenteMapper;

    @Autowired
    private SocietaMapper societaMapper;

    /**
     * Restituisce il profilo utente verificando che l'authToken corrisponda al token dell'utente.
     * Il caricamento avviene con selectByPrimaryKey per avere tutti i campi popolati.
     */
    public Utente getProfiloUtente(Integer utenteId, String authToken) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalArgumentException("L'authToken è obbligatorio");
        }
        Utente utente = utenteMapper.selectByPrimaryKey(utenteId);
        if (utente == null) {
            throw new RuntimeException("Utente non trovato con ID: " + utenteId);
        }
        String tokenDb = utente.getToken() != null ? utente.getToken() : "";
        if (!authToken.trim().equals(tokenDb)) {
            throw new RuntimeException("UTENTE_NON_AUTORIZZATO");
        }
        nomeSocieta(utente);
        return utente;
    }

    /**
     * Restituisce il profilo utente per email. Se authToken è valorizzato, verifica che coincida con il token dell'utente (per chiamate API); se null/vuoto salta il controllo (es. login).
     */
    public Utente getProfiloUtenteByEmail(String email, String authToken) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email non può essere null o vuota");
        }
        UtenteExample example = new UtenteExample();
        example.createCriteria().andEmailEqualTo(email);
        List<Utente> utenti = utenteMapper.selectByExample(example);
        if (utenti == null || utenti.isEmpty()) {
            throw new RuntimeException("Utente non trovato con email: " + email);
        }
        Utente utente = utenti.get(0);
        if (authToken != null && !authToken.isBlank()) {
            String tokenDb = utente.getToken() != null ? utente.getToken() : "";
            if (!authToken.trim().equals(tokenDb)) {
                throw new RuntimeException("UTENTE_NON_AUTORIZZATO");
            }
        }
        nomeSocieta(utente);
        return utente;
    }

    private void nomeSocieta(Utente utente) {
        if (utente.getSocietaId() != null) {
            Societa societa = societaMapper.selectByPrimaryKey(utente.getSocietaId());
            if (societa != null) {
                utente.setSocietaNome(societa.getNome());
            }
        }
    }

    public LoginResponse login(String email, String password) {

        email = email.trim().toLowerCase();

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La password è obbligatoria");
        }
        Utente utente = getProfiloUtenteByEmail(email, null);
        String passwordDb = utente.getPassword();
        if (passwordDb == null || passwordDb.isBlank()) {
            throw new RuntimeException("PASSWORD_NON_IMPOSTATA");
        }
        if (!password.equals(passwordDb)) {
            throw new RuntimeException("CREDENZIALI_NON_VALIDE");
        }
        return new LoginResponse(utente.getId(), utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getToken());
    }

    public void cambiaPassword(Integer utenteId, String vecchiaPassword, String nuovaPassword) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        if (vecchiaPassword == null || vecchiaPassword.isBlank()) {
            throw new IllegalArgumentException("La vecchia password è obbligatoria");
        }
        if (nuovaPassword == null || nuovaPassword.isBlank()) {
            throw new IllegalArgumentException("La nuova password è obbligatoria");
        }
        
        Utente utente = utenteMapper.selectByPrimaryKey(utenteId);
        if (utente == null) {
            throw new RuntimeException("Utente non trovato con ID: " + utenteId);
        }
        
        String passwordDb = utente.getPassword();
        if (passwordDb == null || !passwordDb.equals(vecchiaPassword)) {
            throw new RuntimeException("VECCHIA_PASSWORD_NON_VALIDA");
        }
        
        utente.setPassword(nuovaPassword);
        utente.setAggiornato(new java.util.Date());
        utenteMapper.updateByPrimaryKey(utente);
    }

    public Utente modificaProfilo(Integer utenteId, ModificaProfiloRequest request) {
        if (utenteId == null) {
            throw new IllegalArgumentException("L'ID utente non può essere null");
        }
        if (request == null) {
            throw new IllegalArgumentException("I dati di modifica non possono essere null");
        }
        
        Utente utente = utenteMapper.selectByPrimaryKey(utenteId);
        if (utente == null) {
            throw new RuntimeException("Utente non trovato con ID: " + utenteId);
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            utente.setEmail(request.getEmail());
        }
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            utente.setTelefono(request.getTelefono());
        }
        
        utente.setAggiornato(new java.util.Date());
        utenteMapper.updateByPrimaryKey(utente);
        
        return utente;
    }
}
