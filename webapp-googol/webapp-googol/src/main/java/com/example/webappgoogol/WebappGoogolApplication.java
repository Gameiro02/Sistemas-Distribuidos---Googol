package com.example.webappgoogol;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
			/* TUDO LOCAL */
			// searchModule = (SearchModuleInterface)
			// Naming.lookup("rmi://localhost/SearchModule");

			/* RMI PC LOBO */
			Registry registry = LocateRegistry.getRegistry(Configuration.RMI_ADDRESS_SEARCH_MODULE, Configuration.RMI_PORT_SEARCH_MODULE);
			searchModule = (SearchModuleInterface) registry.lookup("SearchModule");
		} catch (Exception e) {
			System.out.println("Erro ao conectar com o servidor, tentando novamente em 3 segundos");
			Thread.sleep(3000);
		}
		return searchModule;
	}
}
