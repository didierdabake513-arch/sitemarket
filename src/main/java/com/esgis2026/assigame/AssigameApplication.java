package com.esgis2026.assigame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/* @EnableScheduling active les tâches planifiées (@Scheduled), notamment
   CommandeAutoStatutScheduler qui fait passer automatiquement les commandes
   de EXPEDIEE à LIVREE après un délai fixe — voir cette classe pour le détail. */
@SpringBootApplication
@EnableScheduling
public class AssigameApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssigameApplication.class, args);
	}

}
