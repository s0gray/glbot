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
    static Locale locale = new Locale("en");
    static ResourceBundle bundle = ResourceBundle.getBundle("bot", locale);

    public static String getString(String key) {
        return bundle.getString(key);
    }



}
