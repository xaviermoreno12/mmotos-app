package com.mmotos.infrastructure.fiscal;

import com.mmotos.application.dto.DistinguishedNameDTO;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

@Service
public class CsrGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(CsrGeneratorService.class);

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error generando par de claves RSA 2048", e);
        }
    }

    public String generateCsr(KeyPair keyPair, DistinguishedNameDTO dn) {
        try {
            X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.C,  "AR")
                .addRDN(BCStyle.ST, dn.st() != null ? dn.st() : "Buenos Aires")
                .addRDN(BCStyle.L,  dn.l()  != null ? dn.l()  : "")
                .addRDN(BCStyle.O,  dn.o())
                .addRDN(BCStyle.OU, dn.ou() != null ? dn.ou() : "")
                .addRDN(BCStyle.CN, dn.cn())
                .addRDN(BCStyle.SERIALNUMBER, "CUIT " + dn.cuit().replaceAll("[^0-9]", ""))
                .build();

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .build(keyPair.getPrivate());

            PKCS10CertificationRequest csr = new JcaPKCS10CertificationRequestBuilder(
                subject, keyPair.getPublic()
            ).build(signer);

            return toPem("CERTIFICATE REQUEST", csr.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Error generando CSR", e);
        }
    }

    public String privateKeyToPem(KeyPair keyPair) {
        try {
            return toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Error exportando clave privada", e);
        }
    }

    public String publicKeyToPem(PublicKey publicKey) {
        try {
            return toPem("PUBLIC KEY", publicKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Error exportando clave pública", e);
        }
    }

    private String toPem(String type, byte[] encoded) throws Exception {
        StringWriter sw = new StringWriter();
        try (PemWriter pw = new PemWriter(sw)) {
            pw.writeObject(new PemObject(type, encoded));
        }
        return sw.toString();
    }
}
