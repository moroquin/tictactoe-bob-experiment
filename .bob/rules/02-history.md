# Session history (mandatory)

Every session ends by writing `history/session.md`. Create it even if the folder does not exist, and describe only this session. Follow the template below exactly: the histories of different sessions are compared automatically.

- Run `date -u +%Y-%m-%dT%H:%M:%SZ` once before you change any file, and once more just before you write the history. Copy both outputs into Timing.
- Do not estimate tokens, cost or duration: you cannot see them. The tool that runs this session measures them and adds them to the history afterwards.
- Every decision you made on your own goes in the decisions table: each point where the documentation did not tell you what to do, so you would have asked someone if you could.
- Be complete and honest. A history with no decisions is only correct if the documentation really answered everything.

## Template

```markdown
# Session history

## Task
One sentence: what you were asked to do.

## Timing
- Start: <first output of date -u +%Y-%m-%dT%H:%M:%SZ>
- End: <second output of date -u +%Y-%m-%dT%H:%M:%SZ>

## Result
One of `done`, `partially done` or `blocked`, followed by one sentence.

## Decisions I made on my own
| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | ambiguity | ... | ... | ... | `src/...` |

## Pending
| ID | What is pending | Why | Impact |
|---|---|---|---|
| P1 | ... | ... | ... |

## Documentation I relied on
- `FILE`: what it told you, most important file first.

## Verification
The last command you ran to check your work, and its outcome. For example: `mvn -q verify` passed, 14 tests.
```

Number decisions A1, A2… and pending items P1, P2…. If a table has no rows, write `None.` under its heading instead of the table.

The decision types are:

| Type | Use it when |
|---|---|
| `ambiguity` | The documentation allowed more than one reading, and you chose one. |
| `gap` | The documentation said nothing, and you filled the gap. |
| `design` | You chose a structure, pattern or responsibility that the documentation did not prescribe. |
| `naming` | You chose a name that the documentation did not give you. |
| `scope` | You did, or did not do, something that the task did not mention. |
| `deviation` | You deliberately departed from the documentation or changed an existing test. |
| `workaround` | You worked around a problem with a tool or the build. |
