/*
    Minica, a very simple certificate authority
    Copyright (C) 2011  Maurice Perry <maurice@perry.ch>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.tastefuljava.minica;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class X509CertificateBuilder {
    private final BigInteger sn;
    private final X500Principal principal;
    private Date start = today();
    private Date end = addYears(start, 2);
    private String algorithm = "RSA";
    private int keySize = 1024;
    private String signatureAlgorithm = "SHA1withRSA";
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private X500Principal issuer;
    private PrivateKey issuerKey;
    private int basicConstraints = Integer.MAX_VALUE;
    private ECParameterSpec ecSpec;

    public X509CertificateBuilder(BigInteger sn, X500Principal principal) {
        this.sn = sn;
        this.principal = principal;
    }

    public X509CertificateBuilder(X509Certificate cert) {
        this.sn = cert.getSerialNumber();
        this.principal = cert.getSubjectX500Principal();
        this.start = cert.getNotBefore();
        this.end = cert.getNotAfter();
        this.publicKey = cert.getPublicKey();
        this.algorithm = publicKey.getAlgorithm();
        this.signatureAlgorithm = cert.getSigAlgName();
        this.issuer = cert.getIssuerX500Principal();
        this.basicConstraints = cert.getBasicConstraints();
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public void setSignatureAlgorithm(String newValue) {
        this.signatureAlgorithm = newValue;
    }

    public void setAlgorithm(String algorithm, int keySize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
    }

    public void setAlgorithm(String algorithm, ECParameterSpec ecSpec) {
        this.algorithm = algorithm;
        this.ecSpec = ecSpec;
    }

    public void setIssuer(X500Principal issuer, PrivateKey issuerKey) {
        this.issuer = issuer;
        this.issuerKey = issuerKey;
    }

    public void setBasicConstraints(int newValue) {
        basicConstraints = newValue;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate build()
            throws OperatorCreationException, CertificateException,
            IOException, NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException {
        if (publicKey == null) {
            KeyPair pair = generateKeyPair();
            publicKey = pair.getPublic();
            privateKey = pair.getPrivate();
        }
        if (issuer == null) {
            issuer = principal;
            issuerKey = privateKey;
        }
        JcaX509v3CertificateBuilder certGen
                = new JcaX509v3CertificateBuilder(issuer, sn, start, end,
                        principal, publicKey);
        if (basicConstraints < 0) {
            certGen.addExtension(X509Extension.basicConstraints, true,
                    new BasicConstraints(false));
        } else if (basicConstraints != Integer.MAX_VALUE) {
            certGen.addExtension(X509Extension.basicConstraints, true,
                    new BasicConstraints(basicConstraints));
        }
        JcaContentSignerBuilder builder
                = new JcaContentSignerBuilder(signatureAlgorithm);
        builder.setProvider("BC");
        ContentSigner signr = builder.build(issuerKey);
        X509CertificateHolder certHolder = certGen.build(signr);
        return decode(certHolder.getEncoded());
    }

    private KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException {
        KeyPairGenerator gen = getKeyPairGenerator();
        return gen.generateKeyPair();
    }

    private KeyPairGenerator getKeyPairGenerator()
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException {
        KeyPairGenerator gen;
        if ("ECDSA".equals(algorithm) && ecSpec != null) {
            gen = KeyPairGenerator.getInstance("ECDSA", "BC");
            gen.initialize(ecSpec, new SecureRandom());
        } else {
            gen = KeyPairGenerator.getInstance(algorithm);
            gen.initialize(keySize, new SecureRandom());
        }
        return gen;
    }

    public static X509Certificate decode(byte[] data)
            throws CertificateException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (InputStream in = new ByteArrayInputStream(data)) {
            return (X509Certificate)factory.generateCertificate(in);
        }
    }

    public static Date today() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date addYears(Date date, int count) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, count);
        return cal.getTime();
    }

    public void setECSpec(ECParameterSpec ecSpec) {
        this.ecSpec = ecSpec;
    }
}
