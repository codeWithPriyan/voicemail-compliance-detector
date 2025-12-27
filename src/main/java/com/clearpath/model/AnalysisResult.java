package com.clearpath.model;

import com.clearpath.transcription.TranscriptAnalyzer;

public class AnalysisResult {
    private final String fileName;
    private final double greetingEndTime;
    private final BeepInfo beepInfo;
    private final String transcript;
    private final TranscriptAnalyzer.BeepExpectation beepExpectation;
    private final double recommendedStartTime;
    private final String reasoning;
    private final String confidence;

    public AnalysisResult(String fileName, double greetingEndTime, BeepInfo beepInfo,
                          String transcript, TranscriptAnalyzer.BeepExpectation beepExpectation,
                          double recommendedStartTime, String reasoning, String confidence) {
        this.fileName = fileName;
        this.greetingEndTime = greetingEndTime;
        this.beepInfo = beepInfo;
        this.transcript = transcript;
        this.beepExpectation = beepExpectation;
        this.recommendedStartTime = recommendedStartTime;
        this.reasoning = reasoning;
        this.confidence = confidence;
    }

    // Getters
    public String getFileName() { return fileName; }
    public double getGreetingEndTime() { return greetingEndTime; }
    public BeepInfo getBeepInfo() { return beepInfo; }
    public String getTranscript() { return transcript; }
    public TranscriptAnalyzer.BeepExpectation getBeepExpectation() { return beepExpectation; }
    public double getRecommendedStartTime() { return recommendedStartTime; }
    public String getReasoning() { return reasoning; }
    public String getConfidence() { return confidence; }
}
