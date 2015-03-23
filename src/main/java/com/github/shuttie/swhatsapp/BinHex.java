package com.github.shuttie.swhatsapp;

/**
 * Created by shutty on 3/23/15.
 */
public class BinHex {
    public static String bin2hex(byte[] bin) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bin.length ;i++)
        {
            sb.append(Integer.toString((bin[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
