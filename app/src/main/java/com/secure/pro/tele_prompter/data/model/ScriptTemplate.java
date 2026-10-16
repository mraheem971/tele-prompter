package com.secure.pro.tele_prompter.data.model;

import java.util.ArrayList;
import java.util.List;

public class ScriptTemplate {
    private final String title;
    private final String category;
    private final String description;
    private final String content;
    private final int estimatedDurationSeconds;

    public ScriptTemplate(String title, String category, String description, String content, int estimatedDurationSeconds) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.content = content;
        this.estimatedDurationSeconds = estimatedDurationSeconds;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public int getEstimatedDurationSeconds() { return estimatedDurationSeconds; }

    public static List<ScriptTemplate> getDefaultTemplates() {
        List<ScriptTemplate> list = new ArrayList<>();

        list.add(new ScriptTemplate(
                "YouTube Tech Review & Intro",
                "YouTube",
                "Standard high-retention video formula with hook, overview, deep-dive, and call-to-action.",
                "Hey everyone, welcome back to the channel!\n\n" +
                "[HOOK]\n" +
                "Today we are breaking down one of the most exciting tools of the year—and whether it is actually worth your hard-earned money.\n\n" +
                "[THE PROBLEM]\n" +
                "Most creators struggle with keeping eye contact while reading long scripts. Looking down at notes breaks connection with your audience.\n\n" +
                "[SOLUTION & DEMO]\n" +
                "That is where TelePrompt Pro changes the workflow. With crystal clear scrolling, smart mirror mode, and direct studio camera recording, you look straight into the camera lens with zero distraction.\n\n" +
                "[VERDICT & CTA]\n" +
                "If you found this breakdown valuable, smash that subscribe button, hit the bell notification, and let me know your thoughts down in the comments below!",
                75
        ));

        list.add(new ScriptTemplate(
                "60-Second Investor Pitch",
                "Pitch",
                "Fast, persuasive elevator pitch crafted for venture capitalists and pitch competitions.",
                "Good morning everyone.\n\n" +
                "[THE PROBLEM]\n" +
                "Over eighty percent of video creators and public speakers waste hours in post-production simply doing retakes due to forgotten lines or missed cues.\n\n" +
                "[OUR SOLUTION]\n" +
                "We engineered TelePrompt Pro: an offline-first, studio-grade teleprompter that overlays real-time scrolling text directly above the camera lens. No awkward glance deviations. Seamless recording in Full HD.\n\n" +
                "[MARKET & TRACTION]\n" +
                "The creator economy is now a 250-billion-dollar market. In our private pilot, content production turnaround improved by 400%.\n\n" +
                "[THE ASK]\n" +
                "We are seeking strategic partners to bring studio-level teleprompting into every classroom, boardroom, and mobile studio worldwide. Let's build the future of confident speaking together.",
                60
        ));

        list.add(new ScriptTemplate(
                "Keynote Presentation Opening",
                "Keynote",
                "Command the stage with an inspiring, story-driven keynote introduction.",
                "[PAUSE - LOOK AT THE AUDIENCE]\n\n" +
                "Three years ago, I stood on a stage just like this one, completely terrified that I would forget the single most important message of my career.\n\n" +
                "[WARM SMILE]\n" +
                "What I realized that day changed how I approach communication forever: it is not about memorizing words—it is about sharing genuine conviction.\n\n" +
                "Today, I want to explore three shifts reshaping our industry:\n" +
                "First, why authentic connection beats polished perfection every time.\n" +
                "Second, how autonomous intelligent tools are freeing us to focus on real creativity.\n" +
                "And finally, what this means for the leaders in this room.\n\n" +
                "Let's dive in.",
                90
        ));

        list.add(new ScriptTemplate(
                "Viral Reel / TikTok Hook",
                "Shorts / Reels",
                "Fast-paced, punchy, high-energy script for short-form video algorithms.",
                "[FAST PACE - ENERGETIC]\n" +
                "Stop scrolling! If you are still memorizing scripts before filming videos, you are doing it all wrong.\n\n" +
                "Here is the creator secret that saves me 5 hours every single week:\n\n" +
                "1. Paste your script into TelePrompt Pro.\n" +
                "2. Turn on the floating camera overlay right next to your front camera.\n" +
                "3. Record in one take without looking away once.\n\n" +
                "Save this video right now before you film your next clip!",
                30
        ));

        list.add(new ScriptTemplate(
                "Executive Interview Intro",
                "Interview",
                "Structured executive summary: who you are, past achievements, and future impact.",
                "Thank you so much for the opportunity to speak with you today.\n\n" +
                "[PRESENT ROLE]\n" +
                "Currently, I lead product engineering initiatives focused on scalable, resilient mobile architectures.\n\n" +
                "[PROVEN TRACK RECORD]\n" +
                "Over the past five years, my teams have shipped consumer platforms reaching millions of active users while cutting latency by 45%.\n\n" +
                "[FUTURE VISION & FIT]\n" +
                "What excites me most about this role is the opportunity to tackle complex distributed challenges while mentoring world-class talent.\n\n" +
                "I am eager to discuss how my background directly aligns with your strategic goals for the coming year.",
                60
        ));

        return list;
    }
}
