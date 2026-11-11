# Architecture

The same rules for every version of the system.

## Packages

| Package | Holds |
|---|---|
| `enrollment` | `Main`: wiring only. It creates the objects and starts the console. |
| `enrollment.console` | Reading the input, parsing each instruction, writing the responses. It is the only package that reads standard input or writes to standard output or standard error. |
| any other package under `enrollment` | The rules of the system, organized as you see fit. They read no input and write no output: they receive values and return results. |

## Rules

- **The console is an adapter.** It turns a line into a call and a result into response lines; the rules of `REQUIREMENTS.md` (validations, references, permissions, choosing a section) are not decided there.
- **Nothing depends on the console** except `Main`.
- `src/test/java/enrollment/ArchitectureTest.java` enforces these rules. Keep it passing; do not weaken it.
