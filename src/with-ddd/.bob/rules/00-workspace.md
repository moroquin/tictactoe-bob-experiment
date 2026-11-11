# Workspace rules (mandatory)

- Work only inside this workspace folder. Do not read, list or write files outside it.
- Temporary files, such as the board log of a manual test, go inside the folder under `target/` (for example `TICTACTOE_BOARD_LOG=target/test.log`), never in `/tmp` or anywhere else outside it.
- Read and list files only with your file tools. Shell commands that read or list files (`cat`, `ls`, `grep`, `head`, `find`…) are denied; the only commands you may run are `mvn`, `java` and `date`.
- Do not run git commands. Version control is handled outside this session.
- Do not use the network beyond what Maven needs to resolve the build: no curl, no wget, no web searches.
- Do not read or print environment variables.
- Do not keep any memory for future sessions: no notes, memory banks, plans, scratch files or summaries meant for a later session, inside or outside the folder. The only record of this session is `history/session-N.md` (see `02-history.md`); everything else you write is the code, the tests and the documentation the task needs.
