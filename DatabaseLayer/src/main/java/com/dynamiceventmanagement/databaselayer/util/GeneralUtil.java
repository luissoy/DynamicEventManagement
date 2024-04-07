package com.dynamiceventmanagement.databaselayer.util;

import java.util.List;

public class GeneralUtil {
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNullOrEmpty(List<String> list) {
        return list == null || list.isEmpty();
    }

}
