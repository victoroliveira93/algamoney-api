package com.example.algamoney.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.algamoney.api.config.property.AlgamoneyApiProperty;

@SpringBootApplication
@EnableConfigurationProperties(AlgamoneyApiProperty.class)
public class AlgamoneyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlgamoneyApiApplication.class, args);
	}

}

/*
 * Classe de iniciaçã do SpringBoot
 * @SpringBootApplication possui outras anotações como: 
 * @EnableAutoConfiguration que tenta já configurar as configurações feitas
 * @ComponentScan que detecta todos os componentes que fazem parte do sistema, por isso é importante
 * deixar a classe de iniciação no primeiro pacote e deixar os demais pacotes abaixo dele
 * 
 * */
