# Session history

## Task
Implement CR-1: add a PC Player (program-controlled) so the game can be started with `--vs-pc`, with a deterministic five-rule strategy (Win, Block, Center, Corner, Any); without the flag, two Human Players still work as before.

## Timing
- Start: 2026-11-10T22:51:51Z
- End: 2026-11-10T22:59:21Z

## Token use
Agent Mode · 118.9k / 270.0k (44%) · 5.91 🅞

## Result
Done. `mvn -q verify` passes with 66 tests (0 failures, 0 skipped). The `--vs-pc` flag works and the two-human default still works.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | How should `Player` be extended without `instanceof`? | Make `Player` abstract with a new `chooseCoordinate(TicTacToe)` method; `HumanPlayer` delegates to `CoordinateSupplier`; `PcPlayer` computes directly. | `modules.md` says "no class asks which kind of Player it has"; a shared abstract method is the cleanest polymorphic solution. | `src/main/java/tictactoe/domain/Player.java`, decisions D16 |
| A2 | design | How to break the circular dependency between `HumanPlayer` (needs `CoordinateSupplier`) and `ConsoleGame` (needs `TicTacToe`, which needs players)? | Add a `ForwardingSupplier` inner class in `Main` that wraps a `CoordinateSupplier` field wired after construction. | Avoids redesigning the constructors; wiring happens entirely in `Main` where it belongs (D19). | `src/main/java/tictactoe/Main.java` |
| A3 | design | Where should the "occupied square retry" loop live now that `ConsoleGame.run()` calls `chooseCoordinate`? | The outer loop in `ConsoleGame.run()` still checks `game.put()` return value; invalid input now re-enters `chooseCoordinate`, which calls `CoordinateSupplier.get()` in a retry loop. | Keeps the `ConsoleGame` loop's structure simple and preserves the I9 contract (invalid format → message → retry; occupied → message → retry). | `src/main/java/tictactoe/console/ConsoleGame.java` |
| A4 | design | How to test `PcPlayer`'s rule 5 (Any) in isolation? | Rule 5 cannot be exercised via standard game play when PC is O: with 4 X pieces always at least one X two-in-a-line exists (pigeonhole), so rule 2 fires first. Documented this as a known gap; rule 5 code is a trivial linear scan verified by code review and the integration test `pcChoosesValidSquareEveryTurn`. | Game geometry makes an isolated rule-5 unit test infeasible without bypassing `TicTacToe.put()`. | `src/test/java/tictactoe/domain/PcPlayerTest.java` |
| A5 | naming | What to name the domain interface for supplying Coordinates? | `CoordinateSupplier`, consistent with the Java naming convention for single-method interfaces and with the glossary term added in this session. | Glossary-first rule (AGENTS rule 5). | `src/main/java/tictactoe/domain/CoordinateSupplier.java` |

## Pending

None.

## Documentation I relied on

- `AGENTS.md`: golden rules for doc-first development, extension at variation points, no instanceof.
- `docs/domain/modules.md`: package responsibilities, file map, "A new kind of Player" rule, and "A new way to start the game: change Main only".
- `docs/domain/glossary.md`: existing terms and naming conventions; extended with Human Player, PC Player, Coordinate Supplier, and the five PC rules.
- `docs/domain/decisions.md`: existing decisions D1–D15 for context; appended D16–D19.
- `docs/domain/invariants.md`: I3, I8, I9, I10 enforced unchanged.
- `REQUIREMENTS.md`: console contract, `--vs-pc` flag specification, five PC rules.

## Verification
`mvn -q verify` passed: 66 tests (9 new in `PcPlayerTest`), 0 failures, 0 skipped.
