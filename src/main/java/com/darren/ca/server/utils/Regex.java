package com.darren.ca.server.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Regex {
    private Regex() {
    }

    public static String extractFileBytes(String request) {
        String regex = "\\[(?<fileBytes>.*?)\\]";
        String group = "fileBytes";
        return getRegexMatch(request, regex, group);
    }

    public static String extractPassword(String request) {
        String regex = "<(?<username>.*?)><(?<password>.*?)>";
        String group = "password";
        return getRegexMatch(request, regex, group);
    }

    public static String extractUsername(String request) {
        String regex = "<(?<username>.*?)><";
        String group = "username";
        return getRegexMatch(request, regex, group);
    }

    public static String extractFilename(String request) {
        String regex = "\\{(?<filename>.*?)\\}";
        String group = "filename";
        return getRegexMatch(request, regex, group);
    }

    public static String extractfileLength(String request) {
        String regex = "\\[(?<fileLength>.*?)]";
        String group = "fileLength";
        return getRegexMatch(request, regex, group);
    }

    public static String extractfileData(String request) {
        String regex = "\\[\\d*\\](?<data>(?s).*?.*)";
        String group = "data";
        return getRegexMatch(request, regex, group);
    }

    private static String getRegexMatch(String request, String regex, String group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(request);
        return matcher.find() ? matcher.group(group) : "no match";
    }
}
