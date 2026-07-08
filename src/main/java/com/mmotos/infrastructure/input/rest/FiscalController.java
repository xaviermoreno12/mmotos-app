package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.DistinguishedNameDTO;
import com.mmotos.application.dto.GenerarCsrResponse;
import com.mmotos.infrastructure.fiscal.CsrGeneratorService;
import com.mmotos.infrastructure.output.persistence.entity.ConfiguracionFiscalEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ConfiguracionFiscalJpaRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.KeyPair;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/fiscal")
public class FiscalController {

    private static final Logger log = LoggerFactory.getLogger(FiscalController.class);
    private static final String ALIAS_CLAVE = "clave_privada_afip";
    private static final String ALIAS_CSR   = "ultimo_csr_afip";

    private final CsrGeneratorService csrGenerator;
    private final ConfiguracionFiscalJpaRepository fiscalRepo;

    public FiscalController(CsrGeneratorService csrGenerator,
                             ConfiguracionFiscalJpaRepository fiscalRepo) {
        this.csrGenerator = csrGenerator;
        this.fiscalRepo   = fiscalRepo;
    }

    @PostMapping("/generar-csr")
    @PreAuthorize("hasRole('DUENO')")
    public GenerarCsrResponse generarCsr(@Valid @RequestBody DistinguishedNameDTO dn) {
        log.info("Generando CSR para ARCA — CUIT: {}", dn.cuit());

        KeyPair keyPair = csrGenerator.generateKeyPair();
        String csr      = csrGenerator.generateCsr(keyPair, dn);
        String privKey  = csrGenerator.privateKeyToPem(keyPair);
        String pubKey   = csrGenerator.publicKeyToPem(keyPair.getPublic());

        // Guardar o actualizar clave privada en DB
        fiscalRepo.findByAlias(ALIAS_CLAVE).ifPresentOrElse(
            e -> { e.setValor(privKey); fiscalRepo.save(e); },
            () -> fiscalRepo.save(new ConfiguracionFiscalEntity(ALIAS_CLAVE, privKey))
        );

        // Guardar el CSR generado
        fiscalRepo.findByAlias(ALIAS_CSR).ifPresentOrElse(
            e -> { e.setValor(csr); fiscalRepo.save(e); },
            () -> fiscalRepo.save(new ConfiguracionFiscalEntity(ALIAS_CSR, csr))
        );

        log.info("CSR generado y clave privada guardada en DB");
        return new GenerarCsrResponse(csr, pubKey);
    }

    @GetMapping("/csr-actual")
    @PreAuthorize("hasRole('DUENO')")
    public Map<String, String> csrActual() {
        String csr = fiscalRepo.findByAlias(ALIAS_CSR)
            .map(ConfiguracionFiscalEntity::getValor)
            .orElse(null);
        return Map.of(
            "csr", csr != null ? csr : "",
            "generado", csr != null ? "si" : "no"
        );
    }

    private static final Set<String> MODOS_VALIDOS = Set.of("NO_FISCAL", "AFIP", "HASAR");

    @GetMapping("/estado")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public Map<String, String> estado() {
        String modo = fiscalRepo.findByAlias("modo_fiscal")
            .map(ConfiguracionFiscalEntity::getValor)
            .orElse("NO_FISCAL");
        return Map.of("modo", modo);
    }

    @PutMapping("/configurar")
    @PreAuthorize("hasRole('DUENO')")
    public Map<String, String> configurar(@RequestBody Map<String, String> body) {
        String modo = body.getOrDefault("modo", "NO_FISCAL").toUpperCase();
        if (!MODOS_VALIDOS.contains(modo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modo inválido: " + modo);
        }
        fiscalRepo.findByAlias("modo_fiscal").ifPresentOrElse(
            e -> { e.setValor(modo); fiscalRepo.save(e); },
            () -> fiscalRepo.save(new ConfiguracionFiscalEntity("modo_fiscal", modo))
        );
        log.info("Modo fiscal actualizado a: {}", modo);
        return Map.of("modo", modo, "mensaje", "Modo guardado. Reiniciá el servidor para aplicar los cambios.");
    }
}
