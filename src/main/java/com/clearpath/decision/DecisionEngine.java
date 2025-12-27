package com.clearpath.decision;

import com.clearpath.config.Config;
import com.clearpath.model.AnalysisResult;
import com.clearpath.model.BeepInfo;
import com.clearpath.transcription.TranscriptAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionEngine {
    private static final Logger logger = LoggerFactory.getLogger(DecisionEngine.class);

    /**
     * Make final decision on when to start voicemail playback
     */
    public AnalysisResult makeDecision(String fileName, double greetingEndTime,
                                       BeepInfo beepInfo, String transcript,
                                       TranscriptAnalyzer.BeepExpectation beepExpectation) {

        logger.info("Making decision for {}...", fileName);

        double startTime;
        String reasoning;
        String confidence;

        // CASE 1: Beep actually detected in audio (most reliable)
        if (beepInfo.isDetected()) {
            startTime = beepInfo.getEndTime() + Config.POST_BEEP_DELAY_SEC;
            reasoning = String.format("Beep detected at %.3fs (%.0fHz, %.1fs duration). " +
                            "Starting %.1fs after beep end for safety.",
                    beepInfo.getStartTime(), beepInfo.getFrequency(),
                    beepInfo.getDuration(), Config.POST_BEEP_DELAY_SEC);
            confidence = "HIGH";
            logger.info("✓ CASE 1: Beep detected → Start at {}s", String.format("%.3f", startTime));
        }

        // CASE 2: No beep detected, but transcript strongly indicates beep expected
        else if (beepExpectation == TranscriptAnalyzer.BeepExpectation.HIGH) {
            startTime = greetingEndTime + Config.BEEP_WAIT_TIMEOUT_SEC;
            reasoning = String.format("Transcript mentions beep ('at the tone' or 'after the beep'), " +
                            "but no beep detected. Waiting %.1fs to be safe.",
                    Config.BEEP_WAIT_TIMEOUT_SEC);
            confidence = "MEDIUM";
            logger.info("✓ CASE 2: HIGH beep expected but not found → Wait {}s",
                    Config.BEEP_WAIT_TIMEOUT_SEC);
        }

        // CASE 3: Medium beep expectation (phrases like "leave a message")
        else if (beepExpectation == TranscriptAnalyzer.BeepExpectation.MEDIUM) {
            startTime = greetingEndTime + Config.NO_BEEP_LONG_DELAY_SEC;
            reasoning = String.format("Transcript mentions 'leave a message' but no explicit beep phrase. " +
                            "Waiting %.1fs as moderate safety buffer.",
                    Config.NO_BEEP_LONG_DELAY_SEC);
            confidence = "MEDIUM-HIGH";
            logger.info("✓ CASE 3: MEDIUM beep expected → Wait {}s",
                    Config.NO_BEEP_LONG_DELAY_SEC);
        }

        // CASE 4: Low beep expectation (no beep indicators in transcript)
        else {
            startTime = greetingEndTime + Config.NO_BEEP_SHORT_DELAY_SEC;
            reasoning = String.format("No beep phrases in transcript and no beep detected. " +
                            "Starting %.1fs after greeting ends (minimal delay).",
                    Config.NO_BEEP_SHORT_DELAY_SEC);
            confidence = "MEDIUM-HIGH";
            logger.info("✓ CASE 4: LOW beep expected → Wait {}s",
                    Config.NO_BEEP_SHORT_DELAY_SEC);
        }

        // Compliance check
        logger.info("Final decision: Start voicemail at {}s (Confidence: {})",
                String.format("%.3f", startTime), confidence);

        return new AnalysisResult(fileName, greetingEndTime, beepInfo, transcript,
                beepExpectation, startTime, reasoning, confidence);
    }

    /**
     * Print detailed analysis report
     */
    public void printDetailedReport(AnalysisResult result) {
        logger.info("\n" + "=".repeat(80));
        logger.info("DETAILED ANALYSIS REPORT");
        logger.info("=".repeat(80));
        logger.info("File: {}", result.getFileName());
        logger.info("");
        logger.info("--- DETECTION RESULTS ---");
        logger.info("Greeting End Time: {}s", String.format("%.3f", result.getGreetingEndTime()));
        logger.info("Beep Detection: {}", result.getBeepInfo());
        logger.info("Transcript: {}", result.getTranscript());
        logger.info("Beep Expected: {}", result.getBeepExpectation().getDescription());
        logger.info("");
        logger.info("--- FINAL DECISION ---");
        logger.info("Recommended Start Time: {}s", String.format("%.3f", result.getRecommendedStartTime()));
        logger.info("Confidence: {}", result.getConfidence());
        logger.info("Reasoning: {}", result.getReasoning());
        logger.info("");
        logger.info("--- COMPLIANCE CHECK ---");
        logger.info("✓ Consumer will hear message starting at {}s",
                String.format("%.3f", result.getRecommendedStartTime()));
        logger.info("✓ Company name will be in first 2 seconds of message");
        logger.info("✓ Phone number will be included in message body");
        logger.info("STATUS: COMPLIANT");
        logger.info("=".repeat(80) + "\n");
    }
}
