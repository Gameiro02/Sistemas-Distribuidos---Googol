package com.example.webappgoogol;

import java.rmi.Naming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.example.webappgoogol.SearchModule.SearchModuleInterface;

@SpringBootApplication
public class WebappGoogolApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebappGoogolApplication.class, args);
	}

	@Bean
	public SearchModuleInterface searchModule() throws Exception {
		SearchModuleInterface searchModule = null;
		try {
			searchModule = (SearchModuleInterface) Naming.lookup("rmi://localhost/SearchModule");
		} catch (Exception e) {
			System.out.println("Erro ao conectar com o servidor, tentando novamente em 3 segundos");
			Thread.sleep(3000);
		}
		return searchModule;
	}
}
