package com.clearpath.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    // Audio Processing
    public static final int SAMPLE_RATE = 16000;
    public static final int FRAME_SIZE_MS = 20;
    public static final int FRAME_SIZE_SAMPLES = (SAMPLE_RATE * FRAME_SIZE_MS) / 1000;

    // Energy Detection (Silence)
    public static final double SILENCE_THRESHOLD_DB = -50.0;
    public static final double SPEECH_THRESHOLD_DB = -40.0;
    public static final double SILENCE_DURATION_SEC = 1.0;

    // Beep Detection
    public static final int FFT_SIZE = 1024;
    public static final double BEEP_MIN_FREQ_HZ = 900.0;
    public static final double BEEP_MAX_FREQ_HZ = 1100.0;
    public static final double BEEP_MIN_DURATION_SEC = 0.5;
    public static final double BEEP_MAX_DURATION_SEC = 2.5;
    public static final double BEEP_PEAK_RATIO_DB = 15.0;

    // Decision Timing
    public static final double POST_BEEP_DELAY_SEC = 0.5;
    public static final double NO_BEEP_SHORT_DELAY_SEC = 1.0;
    public static final double NO_BEEP_LONG_DELAY_SEC = 3.0;
    public static final double BEEP_WAIT_TIMEOUT_SEC = 3.0;

    // Pattern Matching
    public static final double HIGH_BEEP_PROBABILITY = 0.75;
    public static final double MEDIUM_BEEP_PROBABILITY = 0.50;

    // Deepgram API - Loaded from properties file
    public static final String DEEPGRAM_API_KEY = loadDeepgramKey();
    public static final String DEEPGRAM_WS_URL = "wss://api.deepgram.com/v1/listen?punctuate=true&model=nova-2";

    /**
     * Load Deepgram API key from application.properties
     */
    private static String loadDeepgramKey() {
        try (InputStream input = Config.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                System.err.println("ERROR: application.properties not found!");
                System.err.println("Please copy application.properties.example to application.properties");
                System.err.println("and add your Deepgram API key.");
                return "YOUR_API_KEY_HERE";
            }

            Properties prop = new Properties();
            prop.load(input);

            String apiKey = prop.getProperty("deepgram.api.key");

            if (apiKey == null || apiKey.equals("YOUR_DEEPGRAM_API_KEY_HERE")) {
                System.err.println("ERROR: Deepgram API key not configured!");
                System.err.println("Edit src/main/resources/application.properties");
                return "YOUR_API_KEY_HERE";
            }

            return apiKey;

        } catch (IOException e) {
            System.err.println("Error loading application.properties: " + e.getMessage());
            return "YOUR_API_KEY_HERE";
        }
    }
}
