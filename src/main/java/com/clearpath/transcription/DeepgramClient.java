package com.clearpath.transcription;

import com.clearpath.config.Config;
import com.clearpath.model.DeepgramResponse;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeepgramClient {
    private static final Logger logger = LoggerFactory.getLogger(DeepgramClient.class);
    private static final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    public String transcribeFile(String audioFilePath) throws IOException {
        logger.info("Transcribing with Deepgram...");

        byte[] audioBytes = Files.readAllBytes(Paths.get(audioFilePath));

        RequestBody requestBody = RequestBody.create(
                audioBytes,
                MediaType.parse("audio/wav")
        );

        Request request = new Request.Builder()
                .url("https://api.deepgram.com/v1/listen?punctuate=true&model=nova-2")
                .addHeader("Authorization", "Token " + Config.DEEPGRAM_API_KEY)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String error = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Deepgram API error " + response.code() + ": " + error);
            }

            String responseBody = response.body().string();
            DeepgramResponse deepgramResponse = gson.fromJson(responseBody, DeepgramResponse.class);

            String transcript = deepgramResponse.results.channels[0].alternatives[0].transcript;
            logger.info("Transcript: '{}'", transcript);

            return transcript;
        }
    }
}
