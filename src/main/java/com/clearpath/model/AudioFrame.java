package com.clearpath.model;

public class AudioFrame {
    private final double[] samples;  // Audio samples (normalized -1 to +1)
    private final double timestamp;  // Time in seconds from start
    private final int frameIndex;

    public AudioFrame(double[] samples, double timestamp, int frameIndex) {
        this.samples = samples;
        this.timestamp = timestamp;
        this.frameIndex = frameIndex;
    }

    public double[] getSamples() {
        return samples;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public int getLength() {
        return samples.length;
    }
}
