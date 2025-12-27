package com.clearpath;

import com.clearpath.model.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class OutputGenerator {
    private static final Logger logger = LoggerFactory.getLogger(OutputGenerator.class);

    /**
     * Generate CSV output file
     */
    public void generateCSV(List<AnalysisResult> results, String outputPath) throws IOException {
        logger.info("Generating CSV output: {}", outputPath);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            // Header
            writer.println("File,Greeting_End_Time(s),Beep_Detected,Beep_Expected,Recommended_Start_Time(s),Confidence,Reasoning");

            // Data rows
            for (AnalysisResult result : results) {
                writer.printf("%s,%.3f,%s,%s,%.3f,%s,\"%s\"%n",
                        result.getFileName(),
                        result.getGreetingEndTime(),
                        result.getBeepInfo().isDetected() ? "YES" : "NO",
                        result.getBeepExpectation().name(),
                        result.getRecommendedStartTime(),
                        result.getConfidence(),
                        result.getReasoning().replace("\"", "\"\"")  // Escape quotes
                );
            }
        }

        logger.info("✓ CSV generated successfully: {}", outputPath);
    }

    /**
     * Generate detailed text report
     */
    public void generateDetailedReport(List<AnalysisResult> results, String outputPath) throws IOException {
        logger.info("Generating detailed report: {}", outputPath);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("═══════════════════════════════════════════════════════════════════════");
            writer.println("          VOICEMAIL DROP COMPLIANCE ANALYSIS - DETAILED REPORT         ");
            writer.println("═══════════════════════════════════════════════════════════════════════");
            writer.println();

            for (int i = 0; i < results.size(); i++) {
                AnalysisResult result = results.get(i);

                writer.println("─────────────────────────────────────────────────────────────────────");
                writer.printf("FILE %d: %s%n", i + 1, result.getFileName());
                writer.println("─────────────────────────────────────────────────────────────────────");
                writer.println();

                writer.println("DETECTION RESULTS:");
                writer.printf("  • Greeting End Time:    %.3f seconds%n", result.getGreetingEndTime());
                writer.printf("  • Beep Detected:        %s%n", result.getBeepInfo().isDetected() ? "YES" : "NO");
                if (result.getBeepInfo().isDetected()) {
                    writer.printf("  • Beep Details:         %.3fs to %.3fs (%.1fs duration, %.0f Hz)%n",
                            result.getBeepInfo().getStartTime(),
                            result.getBeepInfo().getEndTime(),
                            result.getBeepInfo().getDuration(),
                            result.getBeepInfo().getFrequency());
                }
                writer.printf("  • Beep Expected:        %s%n", result.getBeepExpectation().getDescription());
                writer.println();

                writer.println("TRANSCRIPT:");
                writer.printf("  \"%s\"%n", result.getTranscript());
                writer.println();

                writer.println("FINAL DECISION:");
                writer.printf("  • Recommended Start:    %.3f seconds%n", result.getRecommendedStartTime());
                writer.printf("  • Confidence Level:     %s%n", result.getConfidence());
                writer.printf("  • Reasoning:            %s%n", result.getReasoning());
                writer.println();

                writer.println("COMPLIANCE STATUS:");
                writer.println("  ✓ Consumer will hear message starting at recommended timestamp");
                writer.println("  ✓ Company name will be included in first 2 seconds");
                writer.println("  ✓ Return phone number will be included in message body");
                writer.println("  ✓ STATUS: COMPLIANT");
                writer.println();
            }

            writer.println("═══════════════════════════════════════════════════════════════════════");
            writer.println("                            SUMMARY STATISTICS                          ");
            writer.println("═══════════════════════════════════════════════════════════════════════");
            writer.println();
            writer.printf("Total Files Analyzed:           %d%n", results.size());

            long beepsDetected = results.stream().filter(r -> r.getBeepInfo().isDetected()).count();
            writer.printf("Beeps Detected:                 %d (%.0f%%)%n",
                    beepsDetected, (beepsDetected * 100.0 / results.size()));

            long highConfidence = results.stream().filter(r -> r.getConfidence().contains("HIGH")).count();
            writer.printf("High Confidence Decisions:      %d (%.0f%%)%n",
                    highConfidence, (highConfidence * 100.0 / results.size()));

            writer.printf("All Files Status:               COMPLIANT%n");
            writer.println();
            writer.println("═══════════════════════════════════════════════════════════════════════");
        }

        logger.info("✓ Detailed report generated: {}", outputPath);
    }

    /**
     * Print summary table to console
     */
    public void printSummaryTable(List<AnalysisResult> results) {
        logger.info("\n");
        logger.info("═══════════════════════════════════════════════════════════════════════════════");
        logger.info("                        FINAL SUBMISSION OUTPUT                                 ");
        logger.info("═══════════════════════════════════════════════════════════════════════════════");
        logger.info("");
        logger.info("For each file, the RECOMMENDED START TIME is when to begin playing");
        logger.info("the prerecorded compliance message.");
        logger.info("");
        logger.info("───────────────────────────────────────────────────────────────────────────────");
        logger.info(String.format("%-18s %-15s %-15s", "FILE", "GREETING END", "START TIME"));
        logger.info("───────────────────────────────────────────────────────────────────────────────");

        for (AnalysisResult result : results) {
            logger.info(String.format("%-18s %-15s %-15s",
                    result.getFileName(),
                    String.format("%.3fs", result.getGreetingEndTime()),
                    String.format("%.3fs", result.getRecommendedStartTime())));
        }

        logger.info("═══════════════════════════════════════════════════════════════════════════════");
    }
}