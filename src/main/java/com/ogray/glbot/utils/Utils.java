package com.ogray.glbot.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.json.JsonParser;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


@Slf4j
public class Utils {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static JsonParser jsonParser = new BasicJsonParser();

    public static Map<String, Object> parseJson(String json) throws JsonParseException {
        Map<String, Object> map = jsonParser.parseMap(json);
        return map;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

    public static int getChatIdFromUid(String pseudoUid) throws Exception {
        log.info("getChatIdFromUid() uid="+pseudoUid);
        byte[] uid = Utils.hexStringToByteArray(pseudoUid);
        return getChatIdFromUid(uid);
    }

    public static int getChatIdFromUid(byte[] pseudoUid) throws Exception {
        if (pseudoUid == null || pseudoUid.length != 8) {
            log.error("getChatIdFromUid bad input: " + pseudoUid);
            throw new Exception("format error");
        }
        byte[] chatId = new byte[4];
        System.arraycopy(pseudoUid, 4, chatId, 0, 4);
        ;
        ByteBuffer wrapped = ByteBuffer.wrap(chatId); // big-endian by default
        return wrapped.getInt();
    }

    public static Vector<String> tokenizeString(String value, String delim) {
        StringTokenizer st = new StringTokenizer(value, delim);
        Vector<String> res = new Vector<String>();

        while (st.hasMoreTokens()) {
            res.add(st.nextToken());
        }
        return res;
    }

    public static Long dateToLong(Date date) {
        if (date == null) {
            return new Long(0);
        }
        return new Long(date.getTime());
    }

    static Locale locale = new Locale("ru");//JiveGlobals.getLocale();
    static ResourceBundle bundle = ResourceBundle.getBundle("bot", locale);

    public static String getString(String key) {
        return bundle.getString(key);
    }

    public static String getMinutesString(int mm) {
        if(mm<10) {
            return "0" + mm;
        }
        return "" + mm;
    }

    public static String getDateString(java.util.Date date) {
        return date.getDate() + "-" + (date.getMonth()+1) + "-" + (date.getYear()+1900);
    }
}
