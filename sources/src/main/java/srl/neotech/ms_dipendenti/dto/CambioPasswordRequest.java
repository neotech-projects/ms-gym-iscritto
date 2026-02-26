package srl.neotech.ms_dipendenti.dto;

public class CambioPasswordRequest {
    private String vecchiaPassword;
    private String nuovaPassword;

    public CambioPasswordRequest() {
    }

    public CambioPasswordRequest(String vecchiaPassword, String nuovaPassword) {
        this.vecchiaPassword = vecchiaPassword;
        this.nuovaPassword = nuovaPassword;
    }

    public String getVecchiaPassword() {
        return vecchiaPassword;
    }

    public void setVecchiaPassword(String vecchiaPassword) {
        this.vecchiaPassword = vecchiaPassword;
    }

    public String getNuovaPassword() {
        return nuovaPassword;
    }

    public void setNuovaPassword(String nuovaPassword) {
        this.nuovaPassword = nuovaPassword;
    }
}
