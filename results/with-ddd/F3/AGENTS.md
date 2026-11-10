# AGENTS.md — Tic-Tac-Toe

Read this first. The documentation in `docs/domain/` drives the development: it says what the domain is, how it is designed and where each piece lives. The code implements the docs; it does not explain them.

## Golden rules

1. **The docs are the source of truth.** `docs/domain/` describes the whole domain: terms, classes, responsibilities, invariants, states, packages and past decisions. If the code and the docs disagree, the docs win and you fix the code in the same task.
2. **Understand the domain from the docs, not from the code.** Do not read the code to learn how the game works or how it is designed: that is in the docs. Read a code file only when you are about to change it or a class it calls directly, using the file map in `docs/domain/modules.md`.
3. **Change the docs first.** A new feature or a change to the rules starts in `docs/domain/`: update the glossary, the model, the invariants, the game states and the decision log, then the code and the tests, all in the same task.
4. **Every design choice goes into the decision log.** When you decide something the docs did not say (a new class, a method, a name, a structure), add it to `docs/domain/decisions.md` and, if it adds a class or a method, to `model.puml` and `modules.md`. The next session must find it in the docs without reading the code.
5. **Name everything with the glossary.** Name every class, method and variable with the terms in `docs/domain/glossary.md`. A discarded term in the code is a bug. If you need a new term, add it to the glossary first.
6. **Tests cite invariants.** Unit tests cite the invariant they protect in their name, for example `I3_putOnOccupiedSquareIsRejected`.
7. **The domain does no input or output.** Only `tictactoe.console` reads input or prints output. `tictactoe.domain` never does; `ArchitectureTest` enforces it.
8. **Extend at the variation points.** Prefer adding a class at a variation point (a new kind of Player) over editing existing domain classes. No class in any package asks which kind of Player it has.
9. **Only `TicTacToe` changes a game** (invariant I10). Everything else reads it through `TicTacToe`.
10. **Finish with docs and code in agreement.** Before you finish, check that every class and public method in the code is in `model.puml`, that every package and file is in the file map of `modules.md`, and that every name is in the glossary.

## How to work on a task

1. Read this file, then `docs/domain/` in this order: `glossary.md`, `model.puml`, `invariants.md`, `game-states.md`, `modules.md`, `decisions.md`.
2. Decide what changes, in terms of the docs: which terms, classes, invariants, states and files.
3. Update the docs.
4. Open only the code files that the file map in `modules.md` names for those classes, and change them. Add new files where the file map says they go.
5. Write or update the tests, run `mvn -q verify`, and check rule 10.

## Map

| File | What it holds |
|---|---|
| `REQUIREMENTS.md` | Rules of the game, console contract, build, out of scope |
| `docs/domain/glossary.md` | Ubiquitous language: term, meaning, name in code, discarded terms |
| `docs/domain/model.puml` | Domain model: classes, their responsibilities and relationships (PlantUML) |
| `docs/domain/invariants.md` | Numbered rules (I1, I2…) that must always hold, and who enforces each |
| `docs/domain/game-states.md` | States of a game and the allowed transitions |
| `docs/domain/modules.md` | Packages, the dependency rule, responsibilities, the file map, and where each kind of change goes |
| `docs/domain/decisions.md` | Append-only decision log |
| `src/test/java/tictactoe/ArchitectureTest.java` | Executable architecture rules |

## Build and run

- `mvn -q verify` compiles and runs the unit and architecture tests. It must pass before you finish.
- `mvn -q -DskipTests package && java -jar target/tictactoe.jar` plays the game.
