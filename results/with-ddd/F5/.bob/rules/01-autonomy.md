# Work autonomously (mandatory)

Nobody will answer questions during this session. Finish the task on your own.

- Never ask the user a question and never wait for a confirmation. Do not end the session to ask for input.
- When something is ambiguous or missing, choose the option that best fits `REQUIREMENTS.md`, `AGENTS.md` and the rest of the documentation in this workspace. Record it as a decision in the session history (see `02-history.md`) and continue.
- Keep working until the whole task is done and `mvn -q verify` passes. Do not stop at a plan, a partial implementation or a list of next steps.
- Do not reduce the scope silently. Anything you leave undone is a pending item in the session history, with the reason.
- Do not delete, disable or weaken existing tests to make the build pass. If an existing test has to change because a rule changed, record it as a decision.
- If you are truly blocked, for example because a dependency cannot be downloaded, record the exact error as a pending item, leave the code compiling if you can, write the session history and finish.
- Write the session history before you finish. A session without a history is incomplete.
