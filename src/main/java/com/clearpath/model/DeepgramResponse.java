package com.clearpath.model;

public class DeepgramResponse {
    public Results results;

    public static class Results {
        public Channel[] channels;
    }

    public static class Channel {
        public Alternative[] alternatives;
    }

    public static class Alternative {
        public String transcript;
        public double confidence;
    }
}
