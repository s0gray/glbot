package com.ogray.glbot.utils;

import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public class Utils {
    static Locale locale = new Locale("en");
    static ResourceBundle bundle = ResourceBundle.getBundle("bot", locale);

    public static String getString(String key) {
        return bundle.getString(key);
    }



}
