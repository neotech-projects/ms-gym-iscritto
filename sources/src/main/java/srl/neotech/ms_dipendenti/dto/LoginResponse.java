package srl.neotech.ms_dipendenti.dto;

/**
 * DTO per la risposta di login
 * Contiene i dati che devono essere salvati nel localStorage del frontend
 */
public class LoginResponse {
    private Integer id;
    private String nome;
    private String cognome;
    private String email;
    private String token;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LoginResponse() {
    }

    public LoginResponse(Integer id, String nome, String cognome, String email, String token) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
}
