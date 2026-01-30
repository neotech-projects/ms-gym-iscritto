package srl.neotech.ms_dipendenti;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication()
@MapperScan("srl.neotech.ms_dipendenti.dao")
@ComponentScan(basePackages = "srl.neotech.ms_dipendenti")
@PropertySource("classpath:application.properties")
@Configuration
public class MsDipendentiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsDipendentiApplication.class, args);
	}

}
