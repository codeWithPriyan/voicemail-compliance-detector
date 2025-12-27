package com.clearpath.detection;

import com.clearpath.config.Config;
import com.clearpath.model.AudioFrame;
import com.clearpath.model.BeepInfo;
import org.jtransforms.fft.DoubleFFT_1D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BeepDetector {
    private static final Logger logger = LoggerFactory.getLogger(BeepDetector.class);

    /**
     * Detect beep after greeting ends
     */
    public BeepInfo detectBeep(List<AudioFrame> frames, double greetingEndTime) {
        logger.info("Detecting beep after greeting end ({:.3f}s)...", String.format("%.3f", greetingEndTime));

        // Only analyze frames AFTER greeting ends
        int startFrameIndex = (int) (greetingEndTime / (Config.FRAME_SIZE_MS / 1000.0));

        // Limit search window to 5 seconds after greeting (beep should be within this)
        int endFrameIndex = Math.min(frames.size(),
                startFrameIndex + (int)(5.0 / (Config.FRAME_SIZE_MS / 1000.0)));

        logger.info("Analyzing frames {} to {} ({}s to {}s)",
                startFrameIndex, endFrameIndex,
                String.format("%.3f", frames.get(startFrameIndex).getTimestamp()),
                String.format("%.3f", frames.get(Math.min(endFrameIndex-1, frames.size()-1)).getTimestamp()));

        // Track beep state
        boolean inBeep = false;
        double beepStartTime = -1;
        double beepFrequency = 0;
        int consecutiveBeepFrames = 0;

        for (int i = startFrameIndex; i < endFrameIndex; i++) {
            AudioFrame frame = frames.get(i);

            // Get FFT result for this frame
            FrequencyAnalysis analysis = analyzeFrequency(frame);

            // Check if this frame contains a beep
            if (isBeepFrame(analysis)) {
                if (!inBeep) {
                    // Beep started
                    inBeep = true;
                    beepStartTime = frame.getTimestamp();
                    beepFrequency = analysis.dominantFreq;
                    consecutiveBeepFrames = 1;
                    logger.debug("Beep candidate started at {}s ({}Hz)",
                            String.format("%.3f", beepStartTime),
                            String.format("%.0f", beepFrequency));
                } else {
                    consecutiveBeepFrames++;
                }
            } else {
                if (inBeep) {
                    // Beep ended, check if valid
                    double beepEndTime = frame.getTimestamp();
                    double duration = beepEndTime - beepStartTime;

                    logger.debug("Beep candidate ended at {}s (duration: {}s)",
                            String.format("%.3f", beepEndTime),
                            String.format("%.2f", duration));

                    // Validate beep duration
                    if (duration >= Config.BEEP_MIN_DURATION_SEC &&
                            duration <= Config.BEEP_MAX_DURATION_SEC) {

                        logger.info("✓ Valid beep detected: {}Hz, {}s duration",
                                String.format("%.0f", beepFrequency),
                                String.format("%.2f", duration));

                        return new BeepInfo(true, beepStartTime, beepEndTime,
                                beepFrequency, 0.9);
                    } else {
                        logger.debug("✗ Invalid beep duration: {}s (expected {}-{}s)",
                                String.format("%.2f", duration),
                                Config.BEEP_MIN_DURATION_SEC,
                                Config.BEEP_MAX_DURATION_SEC);
                    }

                    // Reset
                    inBeep = false;
                    consecutiveBeepFrames = 0;
                }
            }
        }

        logger.info("No valid beep detected in analyzed window");
        return BeepInfo.noBeep();
    }

    /**
     * Analyze frequency content of audio frame using FFT
     */
    private FrequencyAnalysis analyzeFrequency(AudioFrame frame) {
        double[] samples = frame.getSamples();

        // Need to work with FFT_SIZE samples
        double[] fftInput = new double[Config.FFT_SIZE];

        // Copy available samples (pad with zeros if needed)
        System.arraycopy(samples, 0, fftInput, 0, Math.min(samples.length, Config.FFT_SIZE));

        // Apply Hanning window to reduce spectral leakage
        applyHanningWindow(fftInput);

        // Perform FFT
        DoubleFFT_1D fft = new DoubleFFT_1D(Config.FFT_SIZE);
        fft.realForward(fftInput);

        // Calculate magnitude spectrum
        double[] magnitudes = new double[Config.FFT_SIZE / 2];
        for (int i = 0; i < magnitudes.length; i++) {
            double real = fftInput[2 * i];
            double imag = fftInput[2 * i + 1];
            magnitudes[i] = Math.sqrt(real * real + imag * imag);
        }

        // Find dominant frequency
        int maxBin = 0;
        double maxMagnitude = magnitudes[0];
        for (int i = 1; i < magnitudes.length; i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                maxBin = i;
            }
        }

        double dominantFreq = maxBin * (Config.SAMPLE_RATE / (double) Config.FFT_SIZE);

        // Calculate average magnitude (excluding DC component)
        double avgMagnitude = 0;
        for (int i = 1; i < magnitudes.length; i++) {
            avgMagnitude += magnitudes[i];
        }
        avgMagnitude /= (magnitudes.length - 1);

        // Peak-to-average ratio in dB
        double peakToAvgRatio = 20 * Math.log10(maxMagnitude / (avgMagnitude + 1e-10));

        return new FrequencyAnalysis(dominantFreq, maxMagnitude, avgMagnitude, peakToAvgRatio);
    }

    /**
     * Apply Hanning window to reduce spectral leakage
     */
    private void applyHanningWindow(double[] data) {
        int n = data.length;
        for (int i = 0; i < n; i++) {
            double window = 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (n - 1)));
            data[i] *= window;
        }
    }

    /**
     * Check if FFT analysis indicates a beep
     */
    private boolean isBeepFrame(FrequencyAnalysis analysis) {
        boolean freqInRange = (analysis.dominantFreq >= Config.BEEP_MIN_FREQ_HZ &&
                analysis.dominantFreq <= Config.BEEP_MAX_FREQ_HZ);

        boolean strongPeak = (analysis.peakToAvgRatio >= Config.BEEP_PEAK_RATIO_DB);

        return freqInRange && strongPeak;
    }

    /**
     * Helper class to hold frequency analysis results
     */
    private static class FrequencyAnalysis {
        double dominantFreq;
        double maxMagnitude;
        double avgMagnitude;
        double peakToAvgRatio;

        FrequencyAnalysis(double dominantFreq, double maxMagnitude,
                          double avgMagnitude, double peakToAvgRatio) {
            this.dominantFreq = dominantFreq;
            this.maxMagnitude = maxMagnitude;
            this.avgMagnitude = avgMagnitude;
            this.peakToAvgRatio = peakToAvgRatio;
        }
    }
}
