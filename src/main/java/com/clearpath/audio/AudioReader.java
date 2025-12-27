package com.clearpath.audio;

import com.clearpath.config.Config;
import com.clearpath.model.AudioFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioReader {
    private static final Logger logger = LoggerFactory.getLogger(AudioReader.class);

    /**
     * Read audio file and convert to frames
     */
    public List<AudioFrame> readAudioFile(String filePath) throws IOException, UnsupportedAudioFileException {
        logger.info("Reading audio file: {}", filePath);

        File audioFile = new File(filePath);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

        // Get audio format info
        AudioFormat format = audioStream.getFormat();
        logger.info("Original format: {} Hz, {} channels, {} bits",
                format.getSampleRate(), format.getChannels(), format.getSampleSizeInBits());

        // Convert to our target format if needed (16kHz, mono, 16-bit)
        AudioFormat targetFormat = new AudioFormat(
                Config.SAMPLE_RATE,  // 16000 Hz
                16,                   // 16 bits
                1,                    // Mono
                true,                 // Signed
                false                 // Little endian
        );

        // Convert if formats don't match
        if (!format.matches(targetFormat)) {
            logger.info("Converting to target format: 16kHz mono 16-bit");
            audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
            format = targetFormat;
        }

        // Read all audio data
        byte[] audioBytes = audioStream.readAllBytes();
        audioStream.close();

        logger.info("Read {} bytes of audio data", audioBytes.length);

        // Convert bytes to samples (16-bit signed integers)
        int numSamples = audioBytes.length / 2;  // 2 bytes per sample
        double[] allSamples = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            // Combine two bytes into 16-bit signed integer
            short sample = (short) ((audioBytes[i * 2 + 1] << 8) | (audioBytes[i * 2] & 0xFF));
            // Normalize to -1.0 to +1.0 range
            allSamples[i] = sample / 32768.0;
        }

        double durationSec = numSamples / (double) Config.SAMPLE_RATE;
        logger.info("Audio duration: {} seconds", String.format("%.2f", durationSec));
        // Split into frames
        List<AudioFrame> frames = splitIntoFrames(allSamples);
        logger.info("Split into {} frames", frames.size());

        return frames;
    }

    /**
     * Split audio samples into fixed-size frames
     */
    private List<AudioFrame> splitIntoFrames(double[] allSamples) {
        List<AudioFrame> frames = new ArrayList<>();
        int frameSize = Config.FRAME_SIZE_SAMPLES;  // 320 samples = 20ms

        int frameIndex = 0;
        for (int i = 0; i + frameSize <= allSamples.length; i += frameSize) {
            double[] frameSamples = new double[frameSize];
            System.arraycopy(allSamples, i, frameSamples, 0, frameSize);

            double timestamp = i / (double) Config.SAMPLE_RATE;
            frames.add(new AudioFrame(frameSamples, timestamp, frameIndex));
            frameIndex++;
        }

        return frames;
    }
}
