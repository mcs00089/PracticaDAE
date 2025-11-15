
package es.ujaen.dae.incidenciasUrbanas.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author mcs00089
 *
 */
@SpringBootApplication(scanBasePackages="es.ujaen.dae.incidenciasUrbanas")
@EntityScan(basePackages="es.ujaen.dae.incidenciasUrbanas.entidades")
@EnableCaching
public class IncidenciasUrbanas {
    public static void main(String[] args) {
        SpringApplication.run(IncidenciasUrbanas.class);
    }
}
