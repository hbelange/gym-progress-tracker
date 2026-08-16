# PUBLICATION PREP


## Phase 1: Clean up potentially leaked crendentials [COMPLETE]

- Step 1: Scan complete git history for secrets
- Step 2: Scrub git history clean of secrets
- Step 3: Any secrets that were leaked, rotate credentials for
- Step 4: Add the files that leaked the credentials to the .gitignore, so this never happens again. 

## Phase 2: Demo Reliability (resume link must work and app should not break in first 60s)

### Phase 2.1 - Demo Account [COMPLETE]
- Step 1: Demo access without signup
    - No one creates an account to poke around
    - Seed a demo user with realistic sample data. Trivial.
- Step 2: Scheduled Reset (Hourly? or Daily?)
    - @Scheduled
    - Deletes demo user's rows, re-inserts seeded data
    - Handles potential concurrent users cleanly?
- Step 3: Ensure no destructive actions can be taken on the account
    - No deleting the account
    - No changing email/password/username, etc.
    - Any user details that could prevent another user to use the demo account should be guarded. 
- Step 4: "Demo account - data resets houry/daily" banner on home page to set expectations

### Phase 2.2 - Cold-start / Uptime [COMPLETE]

- Step 1: Always on but self managed. Confirm it survives a reboot (systemd service, not a bare java -jar in a dead SSH session)
    - Verified: `enabled` + `WantedBy=multi-user.target`, and confirmed for real with an actual `sudo reboot` on 2026-08-16 — service came back up automatically (0 restarts needed, `ActiveEnterTimestamp` right after boot).
- Step 2: Smoke-test the live link logged-out, incognito, on mobile
    - This is exactly how a recruiter will hit it.
    - Verified live (post Phase 2.1 deploy + post-reboot): logged-out `/login` shows the demo hint, demo/demo login works, home page shows the "data resets hourly" banner, seeded data present (14 workouts / 84 measurements), username-change guard rail returns 403. Mobile viewport (390x844, iOS Safari UA) screenshotted against the live URL — login and home pages render cleanly, no overflow.

