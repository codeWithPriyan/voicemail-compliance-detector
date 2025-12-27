package com.clearpath.transcription;

import com.clearpath.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscriptAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(TranscriptAnalyzer.class);
    private final DeepgramClient deepgramClient = new DeepgramClient();

    public String getTranscript(String audioFilePath) {
        try {
            return deepgramClient.transcribeFile(audioFilePath);
        } catch (Exception e) {
            logger.error("Transcription failed: {}", e.getMessage());
            return "Transcription failed";
        }
    }

    public BeepExpectation analyzeBeepExpectation(String transcript) {
        String lower = transcript.toLowerCase();
        double score = 0.0;

        // High confidence beep indicators
        if (lower.contains("after the beep") || lower.contains("at the tone") ||
                lower.contains("after the tone") || lower.contains("wait for the beep")) {
            score = 0.95;
            logger.info("HIGH beep probability: explicit beep phrase detected");
        }
        // Medium confidence
        else if (lower.contains("leave a message") || lower.contains("leave your message") ||
                lower.contains("leave me a message")) {
            score = 0.60;
            logger.info("MEDIUM beep probability: message phrase detected");
        }
        // Low confidence
        else {
            score = 0.30;
            logger.info("LOW beep probability: no beep indicators");
        }

        if (score >= Config.HIGH_BEEP_PROBABILITY) {
            return BeepExpectation.HIGH;
        } else if (score >= Config.MEDIUM_BEEP_PROBABILITY) {
            return BeepExpectation.MEDIUM;
        } else {
            return BeepExpectation.LOW;
        }
    }

    public enum BeepExpectation {
        HIGH("HIGH - Beep expected (wait 3s)"),
        MEDIUM("MEDIUM - Possible beep (wait 2s)"),
        LOW("LOW - No beep expected (wait 1s)");

        private final String description;
        BeepExpectation(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}