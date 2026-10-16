package com.secure.pro.tele_prompter.utils;

public class TextMetricsHelper {

    // Standard average speech rate for teleprompter narration (Words Per Minute)
    public static final int WORDS_PER_MINUTE = 130;

    public static int calculateWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String trimmed = text.trim();
        String[] words = trimmed.split("\\s+");
        return words.length;
    }

    public static int calculateReadingTimeSeconds(int wordCount) {
        if (wordCount <= 0) return 0;
        return (int) Math.ceil((wordCount / (double) WORDS_PER_MINUTE) * 60.0);
    }

    public static String formatDurationFriendly(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        if (minutes == 0) {
            return seconds + "s";
        } else if (seconds == 0) {
            return minutes + "m";
        } else {
            return minutes + "m " + seconds + "s";
        }
    }
}
