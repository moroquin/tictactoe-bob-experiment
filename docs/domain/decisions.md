# Decision log

Append new decisions at the end. Never rewrite or delete an entry; when a decision changes, add a new entry that says which one it replaces.

| # | Decision | Why |
|---|---|---|
| D1 | Version 1 is console only. | It is the smallest interface the acceptance tests can drive. |
| D2 | Coordinates are 1-based everywhere, in the console and in the domain. | No off-by-one translation between what a person types and the model. |
| D3 | Color EMPTY is a Null Object; no Square ever returns `null`. | Fewer null checks, and the Board can always be printed. |
| D4 | A rule change starts in `docs/domain/`, then the code, in the same task. | The docs stay the source of truth for people and for agents. |
