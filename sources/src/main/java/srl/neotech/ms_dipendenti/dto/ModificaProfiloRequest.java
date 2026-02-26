package srl.neotech.ms_dipendenti.dto;

public class ModificaProfiloRequest {
    private String email;
    private String telefono;

    public ModificaProfiloRequest() {
    }

    public ModificaProfiloRequest(String email, String telefono) {
        this.email = email;
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
