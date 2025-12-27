package com.clearpath.detection;

import com.clearpath.config.Config;
import com.clearpath.model.AudioFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EnergyDetector {
    private static final Logger logger = LoggerFactory.getLogger(EnergyDetector.class);

    public double detectGreetingEnd(List<AudioFrame> frames) {
        logger.info("Detecting greeting end using energy analysis...");

        int consecutiveSilentFrames = 0;
        int requiredSilentFrames = (int) (Config.SILENCE_DURATION_SEC / (Config.FRAME_SIZE_MS / 1000.0));
        boolean speechStarted = false;  // NEW: Track if we've seen speech yet

        logger.info("Looking for {} consecutive silent frames ({} second)",
                requiredSilentFrames, Config.SILENCE_DURATION_SEC);

        for (int i = 0; i < frames.size(); i++) {
            AudioFrame frame = frames.get(i);
            double energyDB = calculateEnergyDB(frame.getSamples());

            // NEW: Check if speech has started
            if (!speechStarted && energyDB > Config.SPEECH_THRESHOLD_DB) {
                speechStarted = true;
                logger.info("Speech started at {}s", String.format("%.3f", frame.getTimestamp()));
            }

            // Only look for silence AFTER speech has started
            if (speechStarted && energyDB < Config.SILENCE_THRESHOLD_DB) {
                consecutiveSilentFrames++;

                if (consecutiveSilentFrames >= requiredSilentFrames) {
                    double greetingEndTime = frame.getTimestamp() - Config.SILENCE_DURATION_SEC;
                    logger.info("Greeting end detected at {}s (energy dropped below {}dB)",
                            String.format("%.3f", greetingEndTime), Config.SILENCE_THRESHOLD_DB);
                    return greetingEndTime;
                }
            } else if (speechStarted) {  // Only reset if speech has started
                if (consecutiveSilentFrames > 0) {
                    logger.debug("Reset silence counter at {}s (energy: {}dB)",
                            String.format("%.3f", frame.getTimestamp()),
                            String.format("%.1f", energyDB));
                }
                consecutiveSilentFrames = 0;
            }

            if (i % 50 == 0) {
                logger.debug("Frame {} at {}s: energy = {}dB, speech_started = {}, silent frames = {}",
                        i,
                        String.format("%.3f", frame.getTimestamp()),
                        String.format("%.1f", energyDB),
                        speechStarted,
                        consecutiveSilentFrames);
            }
        }

        logger.warn("No clear greeting end detected (no sustained silence after speech)");
        return frames.get(frames.size() - 1).getTimestamp();
    }

    public double calculateEnergyDB(double[] samples) {
        double sumSquares = 0.0;
        for (double sample : samples) {
            sumSquares += sample * sample;
        }
        double rms = Math.sqrt(sumSquares / samples.length);

        if (rms < 1e-10) {
            return -100.0;
        }

        double db = 20 * Math.log10(rms);
        return db;
    }

    public void printEnergyProfile(List<AudioFrame> frames, int sampleInterval) {
        logger.info("\n=== Energy Profile ===");
        logger.info("Time(s)\tEnergy(dB)\tStatus");
        logger.info("------\t----------\t------");

        for (int i = 0; i < frames.size(); i += sampleInterval) {
            AudioFrame frame = frames.get(i);
            double energyDB = calculateEnergyDB(frame.getSamples());
            String status;

            if (energyDB > Config.SPEECH_THRESHOLD_DB) {
                status = "SPEECH";
            } else if (energyDB < Config.SILENCE_THRESHOLD_DB) {
                status = "SILENCE";
            } else {
                status = "UNCERTAIN";
            }

            logger.info("{}\t{}\t\t{}",
                    String.format("%.2f", frame.getTimestamp()),
                    String.format("%.1f", energyDB),
                    status);
        }
        logger.info("===================\n");
    }
}
