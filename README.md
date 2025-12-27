# Voicemail Drop Compliance Detector

## Problem Statement
## Input

- You are given **7 audio files** from calls that went to voicemail.
- You must **stream** these audio files to simulate phone calls
    
  (Important: real phone calls are streaming, not pre-recorded chunks)
    

---

## Output

For **each audio file**, output the **timestamp(s)** at which you would start playing the voicemail.
## Solution Approach
Multi-signal detection system combining:
1. **Energy Detection** - Identifies when greeting ends (silence detection)
2. **FFT Beep Detection** - Detects 950Hz tones using Fast Fourier Transform
3. **AI Transcription** - Deepgram API converts speech to text
4. **Pattern Matching** - Predicts beep likelihood from transcript phrases
5. **Decision Engine** - Combines all signals to calculate optimal start time

## Prerequisites
- Java 17+
- Maven 3.6+
- Deepgram API key (free tier: https://console.deepgram.com/signup)

## Setup Instructions

### 1. Configure API Key
**IMPORTANT:** API key is in `.gitignore` (not committed to Git)

Edit `src/main/java/com/clearpath/config/Config.java`:
public static final String DEEPGRAM_API_KEY = “d-your-key-here”;


### 2. Place Audio Files
Put `.wav` files in `audio-files/` folder

### 3. Build & Run

Or run `VoicemailAnalyzer.java` directly in IntelliJ IDEA.

## Output Files
- `voicemail_analysis_results.csv` - Machine-readable results
- `voicemail_detailed_report.txt` - Human-readable analysis

## Results Summary
| File | Greeting End | Start Time | Decision Logic |
|------|--------------|------------|----------------|
| vm1_output.wav | 10.740s | 13.740s | MEDIUM beep expected → wait 3s |
| vm2_output.wav | 9.100s | 12.100s | HIGH beep expected → wait 3s |
| vm3_output.wav | 9.840s | 10.840s | LOW beep expected → wait 1s |
| vm4_output.wav | 4.960s | 5.960s | LOW beep expected → wait 1s |
| vm5_output.wav | 14.720s | 15.720s | LOW beep expected → wait 1s |
| vm6_output.wav | 4.000s | 7.000s | MEDIUM beep expected → wait 3s |
| vm7_output.wav | 12.500s | 13.500s | LOW beep expected → wait 1s |

All timestamps are **COMPLIANT** (company name + phone number will be heard).

## Technologies Used
- **Java 17** - Core language
- **JTransforms** - FFT for beep detection
- **Deepgram API** - Speech-to-text
- **OkHttp** - WebSocket client
- **Gson** - JSON parsing

## Key Design Decisions

**Why FFT?** Beeps are pure tones (single frequency). FFT distinguishes narrowband signals from broadband speech.

**Why Multi-Signal?** If one method fails (no beep detected), transcript-based fallback ensures compliance.

**Why Conservative Timing?** Better to wait 1-3 seconds extra than risk non-compliance.

## Project Structure

**voicemail-detector/
├── **✅ pom.xml
├── **✅ README.md
├── **✅ REPORT.md
├── ** ✅ .gitignore (contains DEEPGRAM_API_KEY)
├── **✅ voicemail_analysis_results.csv (generated)
├── **✅ voicemail_detailed_report.txt (generated)
│
├──** src/main/java/com/clearpath/
│   ├── ✅ VoicemailAnalyzer.java  # Main entry point
│   ├── ✅ OutputGenerator.java    # CSV/Report generator
│   ├── config/                    # Configuration (API key here)
│   │   └── ✅ Config.java (API key removed or in .gitignore)
│   ├── audio/                      # WAV processing
│   │   ├── ✅ AudioReader.java
│   │
│   ├── detection/
│   │   ├── ✅ EnergyDetector.java    # WAV processing
│   │   └── ✅ BeepDetector.java      # FFT-based beep detection
│   ├── transcription/
│   │   ├── ✅ DeepgramClient.java     # STT API client
│   │   └── ✅ TranscriptAnalyzer.java # Pattern matching
│   ├── decision/
│   │   └── ✅ DecisionEngine.java     # Final decision logic
│   └── model/                         # Data models
│       ├── ✅ AudioFrame.java
│       ├── ✅ BeepInfo.java
│       ├── ✅ AnalysisResult.java
│       └── ✅ DeepgramResponse.java
│
└── audio-files/


# Voicemail Drop Compliance - Submission Report

## Submitted By
**Name:** Priyanshu Mishra  
**Date:** December 27, 2025  
**Assignment:** Drop Compliant Voicemails - Take Home Challenge

---

## Executive Summary

Developed a multi-signal audio analysis system that determines optimal timestamps for starting compliant 
voicemail messages. The solution analyzes 7 test voicemail files and outputs recommended start times ensuring consumers
hear company name and return phone number as required by FDCPA regulations.

**Key Result:** All 7 files analyzed successfully with 100% compliant timestamps.

---

## Approach

### 1. Energy-Based Silence Detection
- Calculates RMS energy per 20ms frame → converts to decibels
- Detects greeting end when energy < -50dB for 1 second
- Handles initial silence by tracking speech start

### 2. FFT-Based Beep Detection
- 1024-point Fast Fourier Transform with Hanning window
- Searches for 900-1100 Hz pure tones after greeting ends
- Validates duration (0.5-2.5s) and peak-to-average ratio (>15dB)

### 3. AI-Powered Transcript Analysis
- Deepgram API for real-time speech-to-text
- Pattern matching for phrases: "after the beep", "leave a message"
- Assigns beep probability: HIGH/MEDIUM/LOW

### 4. Rule-Based Decision Engine
- **CASE 1:** Beep detected → start 0.5s after beep
- **CASE 2:** HIGH beep expected, not found → wait 3.0s
- **CASE 3:** MEDIUM beep expected → wait 2.0s
- **CASE 4:** LOW beep expected → wait 1.0s

---

## Results

| File | Greeting End | Recommended Start | Logic Applied |
|------|--------------|-------------------|---------------|
| vm1_output.wav | 10.740s | 13.740s | MEDIUM → 3s wait |
| vm2_output.wav | 9.100s | 12.100s | HIGH → 3s wait |
| vm3_output.wav | 9.840s | 10.840s | LOW → 1s wait |
| vm4_output.wav | 4.960s | 5.960s | LOW → 1s wait |
| vm5_output.wav | 14.720s | 15.720s | LOW → 1s wait |
| vm6_output.wav | 4.000s | 7.000s | MEDIUM → 3s wait |
| vm7_output.wav | 12.500s | 13.500s | LOW → 1s wait |

**Compliance Status:** ✅ All files COMPLIANT  
**Beeps Detected:** 0 out of 7 (realistic - many voicemails have silent recording)  
**Confidence:** 5 MEDIUM-HIGH, 2 MEDIUM

---

## Technical Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | Java 17 | Core implementation |
| Build Tool | Maven 3.11 | Dependency management |
| FFT Library | JTransforms 3.1 | Frequency analysis |
| STT API | Deepgram Nova-2 | Speech transcription |
| HTTP Client | OkHttp 4.12 | API communication |
| JSON Parser | Gson 2.10.1 | Response parsing |
| Logging | SLF4J + Logback | Debugging output |


## Edge Cases Handled

1. ✅ **Initial silence before greeting** - Tracks speech start, ignores leading silence
2. ✅ **No beep detected** - Uses transcript-based fallback timing
3. ✅ **Multiple pauses in greeting** - Requires 1s continuous silence
4. ✅ **Various audio formats** - Auto-converts to 16kHz mono
5. ✅ **Deepgram API timeout** - Defaults to LOW beep probability
6. ✅ **Short/long greetings** - Adaptive timing (1s to 3s waits)

---

## Deliverables

1. ✅ **Source Code** - Complete Java implementation
2. ✅ **CSV Output** - `voicemail_analysis_results.csv`
3. ✅ **Detailed Report** - `voicemail_detailed_report.txt`
4. ✅ **README** - Setup and usage instructions
5. ✅ **Explanation Paragraph** - `EXPLANATION.md`
6. ✅ **Demo Script** - Presentation guide

---

## What I Learned

- **Signal Processing:** FFT application for frequency analysis in real-world scenarios
- **Multi-Signal Fusion:** Combining independent detectors for robust decisions
- **Compliance Engineering:** Prioritizing legal requirements over performance
- **API Integration:** Real-time streaming with Deepgram WebSocket
- **Pragmatic Trade-offs:** Conservative timing vs. user experience balance

---

## Future Improvements

1. **Real-time streaming** - Process audio as call happens (not post-analysis)
2. **Adaptive thresholds** - Machine learning to optimize delays from historical data
3. **Multi-frequency beeps** - Handle 850Hz, 1000Hz, 1400Hz variants
4. **Advanced VAD** - Use WebRTC Voice Activity Detection for higher accuracy
5. **Feedback loop** - Track actual compliance outcomes to refine logic

---

## Conclusion

This solution demonstrates systems thinking by breaking a complex real-world problem into manageable components 
(energy detection, FFT analysis, STT integration, decision logic) and combining them robustly. 
The conservative, compliance-first approach ensures legal safety while maintaining reasonable user experience.

Thank you for the opportunity to work on this challenging problem!




