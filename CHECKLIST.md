Efficient Prompt Engineering for Cost-Effective Development

1. Plan Before You Prompt

 Write down the feature in plain English.

 Break into small, atomic tasks (UI / service / test).

 Decide if this is a new file or update to existing file.

2. Use Structured Prompts

Always include in your request:

Goal → what to build.

Scope → file or package to touch.

Constraints → Java 21, Gradle 9, JavaFX, PDFBox, BouncyCastle, Tess4J, offline-only.

Output → full file / patch / method only.

3. Build in Iterations

 First ask for skeleton/stub code.

 Then refine one method/class at a time.

 Use continue from here only if output cuts off.

4. Reuse JSON Milestone Prompts

 Copy prompts from prompts.json milestone by milestone.

 Don’t rewrite long instructions inside Cursor.

5. Keep Explanations Separate

 Use ChatGPT (free) for explanations.

 Use Cursor Pro only for code output.

6. Patch Instead of Rewrite

 Use diff/patch mode in Cursor.

 Avoid “rewrite whole class.”

 Ask: “Only modify method X, leave the rest intact.”

7. Write Tests First

 Generate JUnit stubs before implementation.

 Then prompt Cursor: “Make this test pass.”

8. Cache Boilerplate

 Keep a snippets/ folder for:

Gradle configs

Logging utils

Security setup (BouncyCastle, AES)

 Reuse instead of regenerating.

9. Batch Small Fixes

 Collect 2–3 related changes in one structured prompt.

Example:

“In Toolbar.java add Zoom In/Out buttons. In PDFService.java add zoom(float factor) method.”

10. Track Credit Usage

 Monitor Cursor credits.

 If a feature costs >10 credits → split into smaller parts.

 Save all generated outputs locally to avoid repeats.

⚡ Following this checklist = fewer tokens, fewer retries, and faster development.