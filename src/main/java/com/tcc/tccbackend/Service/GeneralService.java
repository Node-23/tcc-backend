package com.tcc.tccbackend.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class GeneralService {

    private static final String AES_ALGORITHM = "AES";
    private static final String KEY = "KEY_DE_TESTE";
    private static final Logger logger = LoggerFactory.getLogger(GeneralService.class);
//    private static final EmailService emailService = new EmailService();
    public static String getConstrainField(DataIntegrityViolationException ex){
        String exceptionMessage = ex.getMessage();
        int startIndex = exceptionMessage.indexOf("(") + 1;
        int endIndex = exceptionMessage.indexOf(")");

        if (endIndex != -1) {
            String fieldAndValue = exceptionMessage.substring(startIndex, endIndex);
            String[] parts = fieldAndValue.split("=");
            String field = Arrays.toString(parts).replace("[", "").replace("]", "").replace("_", "");
            return field.equals("customerName") ? "Nome" : field.substring(0, 1).toUpperCase() + field.substring(1);
        }
        return "Campo";
    }

    public static String encryptPasswords(String plaintext) {
        byte[] keyBytes = Arrays.copyOf(KEY.getBytes(), 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        byte[] encryptedBytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedBytes = cipher.doFinal(plaintext.getBytes());
        } catch (Exception e) {
            String msg = "General Service (Encrypt Password) - Data integrity error: "+ e.getMessage() +" - OS: " + System.getProperty("os.name") + "\n Stacktrace: " + e;
            logger.error(msg);
//            emailService.sendEmail("General Service (Encrypt Password) - Encrypt error", msg);
        }
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
