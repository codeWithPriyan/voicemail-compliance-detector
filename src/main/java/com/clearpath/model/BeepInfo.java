package com.clearpath.model;

public class BeepInfo {
    private final boolean detected;
    private final double startTime;
    private final double endTime;
    private final double frequency;
    private final double confidence;

    public BeepInfo(boolean detected, double startTime, double endTime, double frequency, double confidence) {
        this.detected = detected;
        this.startTime = startTime;
        this.endTime = endTime;
        this.frequency = frequency;
        this.confidence = confidence;
    }

    // No beep detected constructor
    public static BeepInfo noBeep() {
        return new BeepInfo(false, -1, -1, 0, 0);
    }

    public boolean isDetected() { return detected; }
    public double getStartTime() { return startTime; }
    public double getEndTime() { return endTime; }
    public double getFrequency() { return frequency; }
    public double getConfidence() { return confidence; }
    public double getDuration() { return endTime - startTime; }

    @Override
    public String toString() {
        if (detected) {
            return String.format("Beep detected at %.3fs-%.3fs (%.0fHz, %.1fs duration)",
                    startTime, endTime, frequency, getDuration());
        }
        return "No beep detected";
    }
}
