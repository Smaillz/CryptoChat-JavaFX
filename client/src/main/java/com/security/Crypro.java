package com.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Crypro {

    public static synchronized String encrypt(String secretkey, String msg) {
        try{
            byte[] key = secretkey.getBytes();
            String IV = "12345678";

            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(IV.getBytes()));
            byte[] encoding = cipher.doFinal(msg.getBytes());

            return DatatypeConverter.printBase64Binary(encoding);
        }catch (Exception e){
            e.printStackTrace();
            return "error encrypt";
        }
    }

    public static synchronized String decrypt(String secretkey, String msg) {
        try{
            byte[] key = secretkey.getBytes();
            String IV = "12345678";

            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");

            byte[] ciphertext = DatatypeConverter.parseBase64Binary(msg);

            // Decrypt
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(IV.getBytes()));
            byte[] message = cipher.doFinal(ciphertext);

            return new String(message);
        }catch (Exception e){
            e.printStackTrace();
            return msg;
        }
    }
}
