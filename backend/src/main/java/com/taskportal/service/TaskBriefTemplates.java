package com.taskportal.service;

import com.taskportal.entity.TaskPriority;

import java.util.Locale;

/**
 * Builds task-specific preparation briefs (not generic boilerplate).
 */
final class TaskBriefTemplates {

    private TaskBriefTemplates() {}

    record BriefResult(String description, TaskPriority priority, String effort) {}

    static BriefResult build(String title) {
        String lower = title.toLowerCase(Locale.ROOT);
        TaskCategory category = detectCategory(lower);

        return switch (category) {
            case PRESENTATION -> presentationBrief(title, lower);
            case DEVELOPMENT -> developmentBrief(title, lower);
            case MEETING -> meetingBrief(title, lower);
            case STUDY -> studyBrief(title, lower);
            case EMAIL -> emailBrief(title, lower);
            case RESEARCH -> researchBrief(title, lower);
            case DESIGN -> designBrief(title, lower);
            case INTERVIEW -> interviewBrief(title, lower);
            case MARKETING -> marketingBrief(title, lower);
            default -> contextualBrief(title, lower);
        };
    }

    private enum TaskCategory {
        PRESENTATION, DEVELOPMENT, MEETING, STUDY, EMAIL,
        RESEARCH, DESIGN, INTERVIEW, MARKETING, GENERAL
    }

    private static TaskCategory detectCategory(String lower) {
        if (matchesAny(lower, "presentation", "pitch", "deck", "slides", "demo day")) return TaskCategory.PRESENTATION;
        if (matchesAny(lower, "code", "develop", "implement", "api", "bug", "fix", "deploy", "spring", "react", "database")) return TaskCategory.DEVELOPMENT;
        if (matchesAny(lower, "meeting", "standup", "sync", "workshop", "brainstorm")) return TaskCategory.MEETING;
        if (matchesAny(lower, "exam", "study", "assignment", "homework", "course", "learn", "revision")) return TaskCategory.STUDY;
        if (matchesAny(lower, "email", "mail", "newsletter", "outreach")) return TaskCategory.EMAIL;
        if (matchesAny(lower, "research", "analysis", "report", "survey", "market study", "investigate")) return TaskCategory.RESEARCH;
        if (matchesAny(lower, "design", "ui", "ux", "wireframe", "mockup", "figma", "prototype")) return TaskCategory.DESIGN;
        if (matchesAny(lower, "interview", "hire", "onboard", "resume", "cv")) return TaskCategory.INTERVIEW;
        if (matchesAny(lower, "marketing", "campaign", "seo", "social media", "content plan", "ads")) return TaskCategory.MARKETING;
        return TaskCategory.GENERAL;
    }

    private static boolean matchesAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private static BriefResult presentationBrief(String title, String lower) {
        boolean clientFacing = lower.contains("client") || lower.contains("stakeholder");
        String description = """
                Preparation brief: %s

                Context:
                You are building a presentation that must persuade and inform in a limited time slot. %s

                Content prep (do this first):
                • Define one core message: what should the audience remember after 24 hours?
                • Outline 5–7 slides max for a short pitch, or 10–14 for a full review — each slide answers one question only.
                • Collect proof: metrics, before/after examples, customer quotes, or a live demo script.
                • Draft speaker notes with transition lines ("So far we saw X; next we solve Y").

                Practice plan:
                • Rehearse aloud twice — once for timing, once for difficult Q&A.
                • Prepare answers for: budget, timeline, risks, and "why now?"
                • Test screen sharing, fonts, and backup PDF export.

                Day-of checklist:
                • Arrive/setup 15 minutes early; water, clicker, charger.
                • Open with the problem in 60 seconds, not company history.

                Success looks like:
                Audience agrees on next step (approval, pilot, or follow-up meeting) with owners and dates.
                """.formatted(
                title,
                clientFacing
                        ? "This is client-facing — tone must be confident, evidence-based, and respectful of executive time."
                        : "Clarify whether this is internal alignment or external selling; adjust depth and jargon accordingly."
        );
        return new BriefResult(description.trim(), TaskPriority.HIGH, "6-10 hours");
    }

    private static BriefResult developmentBrief(String title, String lower) {
        boolean isBug = lower.contains("bug") || lower.contains("fix") || lower.contains("hotfix");
        String description = isBug ? """
                Bug-fix prep: %s

                Reproduce first:
                • Document exact steps, browser/OS, user role, and sample input that triggers the issue.
                • Capture logs, stack trace, and last known good version or commit.

                Investigation:
                • Locate the failing layer (UI, API, DB, integration) before changing code.
                • Write a minimal failing test or script that proves the bug.

                Fix & verify:
                • Apply the smallest change that fixes root cause — avoid unrelated refactors.
                • Run unit/integration tests; test regression on adjacent features.
                • Prepare PR description: cause, fix, how verified, rollout risk.

                Ship:
                • Note monitoring to watch after deploy and rollback plan if needed.
                """.formatted(title) : """
                Development prep: %s

                Scope clarity:
                • List acceptance criteria in plain language (what "done" means for this feature).
                • Identify APIs, DB tables, and UI screens touched — draw a quick flow if helpful.

                Technical plan:
                • Break into commits: data model → service layer → API → frontend → tests.
                • Note security (auth, validation) and edge cases (empty input, timeouts).

                Build sequence:
                • Scaffold with tests where possible; implement happy path, then errors.
                • Self-review against acceptance criteria before opening PR.

                Handoff:
                • Update README or Swagger if endpoints changed; mention env vars or migrations.
                """.formatted(title);

        TaskPriority p = isBug ? TaskPriority.HIGH : TaskPriority.MEDIUM;
        String effort = isBug ? "2-5 hours" : "1-3 days";
        return new BriefResult(description.trim(), p, effort);
    }

    private static BriefResult meetingBrief(String title, String lower) {
        String description = """
                Meeting prep: %s

                Before sending the invite:
                • Purpose in one sentence: decision, update, or problem-solving?
                • Only invite people who can contribute or decide — everyone else gets notes.

                Agenda (share 24h ahead):
                1. Objective & desired outcome (5 min)
                2. Key discussion topics with owner per topic (timed)
                3. Decisions needed today
                4. Actions: who / what / when

                Materials to prepare:
                • Pre-read doc or metrics dashboard link
                • List open questions you need answered
                • Parking lot for off-topic items

                During the meeting:
                • Start on time; capture decisions live, not just discussion.
                • End with read-back of action items.

                After:
                • Send summary within 2 hours: decisions, owners, deadlines.
                """.formatted(title);
        return new BriefResult(description.trim(), TaskPriority.MEDIUM, "45-90 minutes");
    }

    private static BriefResult studyBrief(String title, String lower) {
        String description = """
                Study / learning prep: %s

                Understand the goal:
                • What will you be tested on or graded for — concepts, problems, or essays?
                • Gather syllabus, past papers, or rubric if available.

                Study plan:
                • Split topics into weak / medium / strong — spend 60%% time on weak areas.
                • Use active recall: close notes and explain aloud, don't only highlight.
                • Block 25–40 min focus sessions with 5 min breaks (Pomodoro).

                Resources:
                • One primary source (textbook/video) + practice questions.
                • Create a one-page cheat sheet of formulas, definitions, or frameworks.

                Review cadence:
                • Day 1: learn · Day 2: practice · Day 3: timed mock · Day before: light review only.

                Success:
                You can solve typical problems without notes and explain core ideas simply.
                """.formatted(title);
        return new BriefResult(description.trim(), TaskPriority.MEDIUM, "4-8 hours");
    }

    private static BriefResult emailBrief(String title, String lower) {
        String description = """
                Email / communication prep: %s

                Strategy:
                • Who is the reader and what do they care about most?
                • Single call-to-action: reply, approve, click, or schedule?

                Draft structure:
                • Subject line: specific + outcome-oriented (not "Quick question")
                • Opening: context in 1–2 lines — why you're writing now
                • Body: bullets for facts; bold dates or amounts
                • Close: clear next step + deadline if any

                Tone check:
                • Remove filler words; shorten sentences over 20 words.
                • Read once for warmth, once for clarity — adjust for senior vs peer.

                Before send:
                • Verify names, attachments, links; send test to yourself if HTML.
                • Schedule send for reader's morning if async.
                """.formatted(title);
        return new BriefResult(description.trim(), TaskPriority.LOW, "20-45 minutes");
    }

    private static BriefResult researchBrief(String title, String lower) {
        String description = """
                Research prep: %s

                Question framing:
                • Turn the title into a research question: "What do we need to know to decide X?"
                • Define scope boundaries — time period, geography, audience.

                Source plan:
                • Mix: primary data (if any), industry reports, internal metrics, expert interviews.
                • Track sources in a table: claim | source | date | confidence.

                Analysis approach:
                • Group findings by theme, not by source order.
                • Separate facts from assumptions; flag gaps explicitly.

                Output:
                • Executive summary (half page) + detailed appendix.
                • Recommend 2–3 options with pros/cons and a clear recommendation.

                Quality bar:
                A skeptical reader should trust your numbers and see logical next steps.
                """.formatted(title);
        return new BriefResult(description.trim(), TaskPriority.MEDIUM, "1-2 days");
    }

    private static BriefResult designBrief(String title, String lower) {
        String description = """
                Design prep: %s

                Discovery:
                • User persona + main job-to-be-done for this screen/flow
                • List constraints: brand colors, accessibility (WCAG), devices (mobile first?)

                Exploration:
                • Low-fi wireframes — 2–3 layout options before high fidelity
                • Map user flow: entry → actions → success/error states

                Visual system:
                • Typography scale, spacing grid (8px), primary/secondary actions
                • States: default, hover, disabled, loading, empty

                Validation:
                • Walk through with 1–2 users or teammates; note confusion points
                • Handoff: specs for dev (margins, tokens, component names)

                Deliverable:
                Figma (or similar) with annotated frames + export assets if needed.
                """.formatted(title);
        return new BriefResult(description.trim(), TaskPriority.MEDIUM, "1-3 days");
    }

    private static BriefResult interviewBrief(String title, String lower) {
        String description = """
                Interview / hiring prep: %s

                Role alignment:
                • Re-read job description; list top 5 skills they emphasize.
                • Prepare 3 STAR stories (Situation, Task, Action, Result) mapped to those skills.

                Company research:
                • Recent news, product, culture values — one genuine question per value.

                Logistics:
                • Confirm format (video / onsite), dress code, panel names on LinkedIn.
                • Test camera, mic, lighting; have resume and notes visible but not reading.

                Your questions for them:
                • Team structure, success in 90 days, biggest challenge in the role.

                Follow-up:
                • Thank-you email within 24h referencing a specific discussion point.
                """.formatted(title);
        return new BriefResult(description.trim(), TaskPriority.HIGH, "3-5 hours");
    }

    private static BriefResult marketingBrief(String title, String lower) {
        String description = """
                Marketing / campaign prep: %s

                Objective & KPI:
                • Pick one primary metric: signups, leads, traffic, engagement rate.
                • Set target number and campaign window.

                Audience:
                • Segment: who, pain point, where they spend time online.

                Message:
                • One headline promise + proof point + CTA.
                • Channel plan: which 2 channels first (don't spread thin on day one).

                Assets & timeline:
                • Copy variants, visuals, landing page, tracking (UTM, pixels).
                • Calendar: draft → review → publish → measure → iterate.

                Compliance:
                • Brand guidelines, legal disclaimers if offers/pricing mentioned.
                """.formatted(title);
        return new BriefResult(description.trim(), TaskPriority.MEDIUM, "2-4 days");
    }

    private static BriefResult contextualBrief(String title, String lower) {
        String[] words = title.split("\\s+");
        String topicHint = words.length > 2
                ? String.join(", ", java.util.Arrays.copyOfRange(words, 0, Math.min(4, words.length)))
                : title;

        String description = """
                Custom prep plan: %s

                What this task is about:
                The title points to work involving: %s. Treat this as a dedicated work package — not a vague reminder.

                Phase 1 — Understand (today):
                • Write down what "finished" looks like in one paragraph.
                • List people, tools, or documents you need access to.
                • Estimate realistic deadline and block calendar time.

                Phase 2 — Plan (30 min):
                • Split into 3–5 concrete chunks; order by dependency.
                • Identify the riskiest part and tackle it early.

                Phase 3 — Execute:
                • Work chunk-by-chunk; note blockers as they appear.
                • Save drafts or checkpoints so you can resume easily.

                Phase 4 — Close:
                • Compare result against your "finished" definition.
                • Document outcome and any follow-ups specific to "%s".

                Tip: Rename subtasks in your tracker using verbs (e.g. "Draft", "Review", "Send") so progress is visible.
                """.formatted(title, topicHint, title);

        TaskPriority priority = lower.contains("urgent") || lower.contains("asap") || lower.contains("important")
                ? TaskPriority.HIGH : TaskPriority.MEDIUM;
        return new BriefResult(description.trim(), priority, "2-6 hours");
    }
}
