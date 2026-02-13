package srl.neotech.ms_dipendenti.sidecar.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtProperties {

    /**
     * Secret per la verifica della firma HMAC del JWT (almeno 256 bit / 32 caratteri per HS256).
     */
    @NotBlank(message = "app.jwt.secret è obbligatorio per la verifica JWT")
    private String secret = "";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
