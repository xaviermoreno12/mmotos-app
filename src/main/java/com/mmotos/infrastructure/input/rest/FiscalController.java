package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.DistinguishedNameDTO;
import com.mmotos.application.dto.GenerarCsrResponse;
import com.mmotos.infrastructure.fiscal.CsrGeneratorService;
import com.mmotos.infrastructure.output.persistence.entity.ConfiguracionFiscalEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ConfiguracionFiscalJpaRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.util.Map;

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
}
