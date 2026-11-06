# AGENTS.md — Tic-Tac-Toe

Read this first. It says what exists and where; the detail lives in `docs/domain/`.

## Golden rules

1. `docs/domain/` is the source of truth for the domain. If the code and the docs disagree, the docs win and you fix the code in the same task.
2. Name every class, method and variable with the terms in `docs/domain/glossary.md`. A discarded term in the code is a bug.
3. A change to the rules starts in the docs: update the glossary, the model, the invariants, the game states and the decision log first, then the code and the tests, all in the same task.
4. Unit tests cite the invariant they protect in their name, for example `I3_putOnOccupiedSquareIsRejected`.
5. Only `tictactoe.console` reads input or prints output. `tictactoe.domain` never does; `ArchitectureTest` enforces it.
6. Prefer adding a class at a variation point (a new kind of Player, a new Goal) over editing existing domain classes.

## Map

| File | What it holds |
|---|---|
| `REQUIREMENTS.md` | Rules of the game, console contract, build, out of scope |
| `docs/domain/glossary.md` | Ubiquitous language: term, meaning, name in code, discarded terms |
| `docs/domain/model.puml` | Domain model: classes and relationships (PlantUML) |
| `docs/domain/invariants.md` | Numbered rules (I1, I2…) that must always hold |
| `docs/domain/game-states.md` | States of a game and the allowed transitions |
| `docs/domain/modules.md` | Packages, the dependency rule, and where each kind of change goes |
| `docs/domain/decisions.md` | Append-only decision log |
| `src/test/java/tictactoe/ArchitectureTest.java` | Executable architecture rules |

## Build and run

- `mvn -q verify` compiles and runs the unit and architecture tests. It must pass before you finish.
- `mvn -q -DskipTests package && java -jar target/tictactoe.jar` plays the game.
