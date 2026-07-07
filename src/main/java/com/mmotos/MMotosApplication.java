package com.mmotos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.mmotos.infrastructure.config.AppProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class MMotosApplication {

    private static final Logger log = LoggerFactory.getLogger(MMotosApplication.class);

    public static void main(String[] args) {
        checkNativeLibraries();
        SpringApplication.run(MMotosApplication.class, args);
    }

    private static void checkNativeLibraries() {
        String arch = System.getProperty("os.arch");
        String os   = System.getProperty("os.name");
        log.info("Sistema operativo: {} | Arquitectura: {}", os, arch);

        // JSSC carga nativamente una DLL (Windows) o .so (Linux).
        // El instalador de la aplicación es responsable de copiar la DLL correcta.
        // Aquí solo advertimos si la arquitectura no es la esperada.
        boolean is64bit = arch != null && (arch.contains("64") || arch.equalsIgnoreCase("amd64"));
        if (!is64bit) {
            log.warn("Arquitectura x86 detectada. Asegurate de usar la DLL nativa correcta para JSSC (x86). " +
                     "El hardware serial puede no funcionar si la DLL no coincide con la JVM instalada.");
        }

        // Verificamos que el módulo JSSC esté accesible en el classpath
        try {
            Class.forName("jssc.SerialPort");
            log.info("Librería JSSC disponible en classpath.");
        } catch (ClassNotFoundException e) {
            log.warn("JSSC no encontrada en classpath. El hardware fiscal serial no estará disponible. " +
                     "Verificá la dependencia en pom.xml o el instalador de la aplicación.");
        }
    }
}
