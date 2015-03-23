package com.github.shuttie.swhatsapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by shutty on 3/23/15.
 */
public class Pbkdf2 {

    public static byte[] encrypt(String algo, byte[] password,
                            byte[] salt, int iterations, int length, boolean raw) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        if (iterations <= 0 || length <= 0) {
            throw new InvalidKeySpecException("PBKDF2 ERROR: Invalid parameters.");
        }

        int hash_length = 20; //hash(algo, "", true).length();
        double block_count = Math.ceil(length / hash_length);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = 1; i <= block_count; i++) {
            ByteArrayOutputStream last = new ByteArrayOutputStream();
            last.write(salt);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(i);
            last.write(buffer.array());
            byte[] lastBuf = last.toByteArray();
            byte[] xorsum = KeyStream.hash_hmac(lastBuf, password);
            byte[] xorsum2 = xorsum;
            for (int j = 1; j < iterations; j++) {
                xorsum2 = KeyStream.hash_hmac(xorsum2, password);
                last.reset();
                int k=0;
                for(byte b : xorsum) {
                    last.write(b ^ xorsum2[k++]);
                }
                xorsum = last.toByteArray();
            }
            output.write(xorsum);
        }
        if(raw) {
            return output.toByteArray();
        }
        return toHex(output.toByteArray()).getBytes();
    }

    public static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

}
