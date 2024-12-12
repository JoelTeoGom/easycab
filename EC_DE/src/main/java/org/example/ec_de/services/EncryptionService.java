package org.example.ec_de.services;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private final KeyPair rsaKeyPair;
    private PublicKey centralPublicKey; // Clave pública de Central

    public EncryptionService() throws Exception {
        // Genera claves RSA (asimétricas) para EC_DE
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        this.rsaKeyPair = keyPairGenerator.generateKeyPair();
    }

    // Registrar clave pública de Central
    public void setCentralPublicKey(PublicKey centralPublicKey) {
        this.centralPublicKey = centralPublicKey;
    }

    // Obtener clave pública de Central
    public PublicKey getCentralPublicKey() {
        return centralPublicKey;
    }

    // Obtener clave pública del taxi (EC_DE)
    public PublicKey getTaxiPublicKey() {
        return rsaKeyPair.getPublic();
    }

    // Obtener clave privada del taxi (EC_DE)
    public PrivateKey getTaxiPrivateKey() {
        return rsaKeyPair.getPrivate();
    }

    // RSA Encryption
    public String encryptWithRSA(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decryptWithRSA(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    // AES Encryption
    public String encryptWithAES(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decryptWithAES(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    public SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public String encodeKey(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public SecretKey decodeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
