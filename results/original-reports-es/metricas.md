# Métricas medidas

Generado por `python3 medir.py`. Cada fase es la foto guardada con `medir.py foto F<n>`, compilada en una carpeta temporal.
Costo, contexto, duración y lecturas vienen de la base de datos de Bob Shell (`~/.bob/db/bob.db`) cuando está disponible; si no, de la línea pegada al final del historial. Las decisiones y los pendientes vienen del historial que escribe Bob.

## F1

| Métrica | without-ddd | with-ddd |
|---|---|---|
| Resultado (según Bob) | Done — all tests pass, `mvn -q verify` exits with no output (success), and the j | Done. `mvn -q verify` passes with 54 tests and the jar follows the console contr |
| Duración (s, Timing del historial) | 183 | 235 |
| Contexto al final (k tokens) | 25.4 | 46.7 |
| Contexto usado (% de la ventana) | — | 17 |
| Costo (Bobcoins) | 1.06 | 2.12 |
| Fuente del costo | bob.db | bob.db |
| Sesiones de Bob en la fase | 1 | 1 |
| Llamadas al modelo | 27 | 31 |
| Builds con mvn | 1 | 3 |
| Lecturas: docs · código · tests | 1 · 0 · 0 | 7 · 0 · 1 |
| Antes de la primera edición: docs leídos · código leído | 1 · 0 | 7 · 1 |
| Primera edición en | codigo | codigo |
| ¿Docs editados antes que el código? | False | False |
| mvn -q verify | ok | ok |
| Tests unitarios | 28 | 54 |
| Aceptación (escenarios ocultos) | 0/9 | 9/9 |
| Lógica correcta aunque falle el formato | 9/9 | 9/9 |
| Aceptación F1 (regresiones) | 0/9 | 9/9 |
| Escenarios fallidos | F1-01, F1-02, F1-03, F1-04, F1-05, F1-06, F1-07, F1-08, F1-09 | ninguno |
| Mutation score total (%) | 79 | 71 |
| Mutantes muertos/generados | 65/82 | 86/121 |
| Mutation score sin la consola (%) | 96 | 90 |
| Mutantes sin la consola | 65/68 | 86/96 |
| Clases de producción | 3 | 12 |
| Paquetes | 1 | 3 |
| Líneas de producción | 113 | 317 |
| Líneas de test | 201 | 409 |
| Acoplamiento eferente Ce (mediana · máx) | 1 · 2 | 1.0 · 7 |
| Acoplamiento aferente Ca (máx) | 2 | 7 |
| Cohesión LCOM* mediana, sin getters (menor = mejor) | 0.42 | 0.5 |
| Clases con campos partibles (LCOM4 > 1) | 0 | 5 |
| Clases con E/S de consola | Main | Main, console.ConsoleGame |
| Encapsulamiento: campos de instancia privados | 4/4 | 16/16 |
| Encapsulamiento: métodos públicos (%) | 100 | 82 |
| Encapsulamiento: métodos públicos que cambian estado | Board.place, Game.tryMove | domain.TicTacToe.put |
| Encapsulamiento: estado mutable expuesto (getter devuelve array u objeto modificable) | Game.board() devuelve Board | ninguno |
| Ciclos entre paquetes | ninguno | ninguno |
| Foto reconstruida desde bob.db | True | True |
| Decisiones propias (total) | 3 | 3 |
|   · ambiguity | 0 | 0 |
|   · gap | 2 | 1 |
|   · design | 1 | 2 |
|   · naming | 0 | 0 |
|   · scope | 0 | 0 |
|   · deviation | 0 | 0 |
|   · workaround | 0 | 0 |
| Pendientes | 0 | 0 |
| Documentos en los que se apoyó | 3 | 8 |
| Términos descartados en el código | 3 | 0 |
| Términos del glosario presentes | 6/17 | 17/17 |

### Acoplamiento y cohesión por clase

**without-ddd** — dependencias entre paquetes: ninguna

| Clase | Ce (usa) | Ca (la usan) | Inestabilidad Ce/(Ca+Ce) | Métodos | Campos | LCOM* | LCOM4 |
|---|---|---|---|---|---|---|---|
| Board | 0 | 2 | 0.0 | 7 | 1 | 0.5 | 1 |
| Game | 1 | 1 | 0.5 | 3 | 3 | 0.33 | 1 |
| Main | 2 | 0 | 1.0 | 2 | 0 | — | — |

**with-ddd** — dependencias entre paquetes: {'(raíz)': ['tictactoe.console', 'tictactoe.domain'], 'tictactoe.console': ['tictactoe.domain']}

| Clase | Ce (usa) | Ca (la usan) | Inestabilidad Ce/(Ca+Ce) | Métodos | Campos | LCOM* | LCOM4 |
|---|---|---|---|---|---|---|---|
| Main | 4 | 0 | 1.0 | 2 | 0 | — | — |
| console.ConsoleGame | 4 | 1 | 0.8 | 5 | 2 | 0.5 | 1 |
| domain.Board | 5 | 1 | 0.83 | 7 | 1 | 0.67 | 2 |
| domain.Color | 0 | 7 | 0.0 | 2 | 3 | 1.0 | 2 |
| domain.Coordinate | 0 | 5 | 0.0 | 5 | 2 | 0.25 | 2 |
| domain.Line | 1 | 1 | 0.5 | 8 | 1 | 0.71 | 2 |
| domain.Piece | 1 | 3 | 0.25 | 1 | 1 | — | 1 |
| domain.Player | 1 | 3 | 0.25 | 1 | 1 | — | 1 |
| domain.Round | 1 | 1 | 0.5 | 2 | 3 | 0.0 | 1 |
| domain.Square | 3 | 1 | 0.75 | 4 | 2 | 0.33 | 1 |
| domain.State | 0 | 2 | 0.0 | 3 | 5 | 0.7 | 2 |
| domain.TicTacToe | 7 | 2 | 0.78 | 4 | 3 | 0.44 | 1 |

### Vocabulario, Open/Closed y preguntas

**without-ddd**
- Términos descartados encontrados: {'Status': ['Status', 'status'], 'move': ['tryMove'], 'place': ['place']}
- Términos del glosario que faltan: Color, Column, Coordinate, Diagonal, InverseDiagonal, Put, Round, Square, State, TicTacToe, Win
- Preguntas que Bob habría hecho:
  - Should game logic live in one class or be split?
  - REQUIREMENTS.md says "any other output … is allowed before the final board" — what to print as prompts?
  - What should happen if stdin ends before the game is over (e.g. in a script that supplies too few moves)?

**with-ddd**
- Términos descartados encontrados: ninguno
- Términos del glosario que faltan: ninguno
- Preguntas que Bob habría hecho:
  - What text should the `Invalid` line say for bad format vs. occupied square?
  - Should `Square` and `Board` constructors be package-private or public?
  - The test for I6 "win checked before draw" had a move sequence where O actually won before X; the test was wrong

## F2

| Métrica | without-ddd | with-ddd |
|---|---|---|
| Resultado (según Bob) | done — `mvn -q verify` passes with all 40 tests (28 original + 12 new PcPlayer t | Done. All 73 tests pass (`mvn -q verify`), docs and code are in agreement. |
| Duración (s, Timing del historial) | 175 | 428 |
| Contexto al final (k tokens) | 35.4 | 94.8 |
| Contexto usado (% de la ventana) | 13 | 35 |
| Costo (Bobcoins) | 1.58 | 5.05 |
| Fuente del costo | bob.db | bob.db |
| Sesiones de Bob en la fase | 1 | 1 |
| Llamadas al modelo | 29 | 47 |
| Builds con mvn | 3 | 5 |
| Lecturas: docs · código · tests | 1 · 4 · 2 | 9 · 7 · 5 |
| Antes de la primera edición: docs leídos · código leído | 1 · 4 | 8 · 9 |
| Primera edición en | codigo | docs |
| ¿Docs editados antes que el código? | False | True |
| mvn -q verify | ok | ok |
| Tests unitarios | 40 | 73 |
| Aceptación (escenarios ocultos) | 0/12 | 12/12 |
| Lógica correcta aunque falle el formato | 11/12 | 12/12 |
| Aceptación F1 (regresiones) | 0/9 | 9/9 |
| Aceptación F2 | 0/3 | 3/3 |
| Escenarios fallidos | F1-01, F1-02, F1-03, F1-04, F1-05, F1-06, F1-07, F1-08, F1-09, F2-01, F2-02, F2-03 | ninguno |
| Mutation score total (%) | 77 | 71 |
| Mutantes muertos/generados | 85/110 | 107/150 |
| Mutation score sin la consola (%) | 94 | 88 |
| Mutantes sin la consola | 85/90 | 107/121 |
| Clases de producción | 4 | 14 |
| Paquetes | 1 | 3 |
| Líneas de producción | 168 | 384 |
| Líneas de test | 310 | 584 |
| Acoplamiento eferente Ce (mediana · máx) | 1.0 · 3 | 3.0 · 7 |
| Acoplamiento aferente Ca (máx) | 3 | 9 |
| Cohesión LCOM* mediana, sin getters (menor = mejor) | 0.5 | 0.58 |
| Clases con campos partibles (LCOM4 > 1) | 1 | 6 |
| Clases con E/S de consola | Main | Main, console.ConsoleGame |
| Encapsulamiento: campos de instancia privados | 4/4 | 16/16 |
| Encapsulamiento: métodos públicos (%) | 93 | 79 |
| Encapsulamiento: métodos públicos que cambian estado | Board.place, Game.tryMove | domain.TicTacToe.put |
| Encapsulamiento: estado mutable expuesto (getter devuelve array u objeto modificable) | Game.board() devuelve Board | ninguno |
| Ciclos entre paquetes | ninguno | ninguno |
| Clases nuevas / modificadas / borradas | 1 / 1 / 0 | 2 / 3 / 0 |
| Open/Closed (nuevas ÷ (nuevas + modificadas)) | 0.5 | 0.4 |
| Líneas cambiadas en clases existentes | 20 | 60 |
| Tests existentes tocados | 0 | 2 |
| docs/domain actualizados | — | decisions.md, glossary.md, model.puml, modules.md |
| ¿Bob modificó REQUIREMENTS.md? | False | True |
| Foto reconstruida desde bob.db | False | False |
| Decisiones propias (total) | 4 | 6 |
|   · ambiguity | 0 | 0 |
|   · gap | 1 | 0 |
|   · design | 3 | 4 |
|   · naming | 0 | 1 |
|   · scope | 0 | 1 |
|   · deviation | 0 | 0 |
|   · workaround | 0 | 0 |
| Pendientes | 0 | 0 |
| Documentos en los que se apoyó | 3 | 5 |
| Términos descartados en el código | 4 | 1 |
| Términos del glosario presentes | 9/19 | 19/19 |

### Acoplamiento y cohesión por clase

**without-ddd** — dependencias entre paquetes: ninguna

| Clase | Ce (usa) | Ca (la usan) | Inestabilidad Ce/(Ca+Ce) | Métodos | Campos | LCOM* | LCOM4 |
|---|---|---|---|---|---|---|---|
| Board | 0 | 3 | 0.0 | 7 | 1 | 0.5 | 1 |
| Game | 1 | 1 | 0.5 | 3 | 3 | 0.33 | 1 |
| Main | 3 | 0 | 1.0 | 2 | 0 | — | — |
| PcPlayer | 1 | 1 | 0.5 | 3 | 2 | 0.75 | 2 |

**with-ddd** — dependencias entre paquetes: {'(raíz)': ['tictactoe.console', 'tictactoe.domain'], 'tictactoe.console': ['tictactoe.domain']}

| Clase | Ce (usa) | Ca (la usan) | Inestabilidad Ce/(Ca+Ce) | Métodos | Campos | LCOM* | LCOM4 |
|---|---|---|---|---|---|---|---|
| Main | 6 | 0 | 1.0 | 2 | 0 | — | — |
| console.ConsoleGame | 5 | 1 | 0.83 | 5 | 2 | 0.5 | 1 |
| domain.Board | 5 | 1 | 0.83 | 7 | 1 | 0.67 | 2 |
| domain.Color | 0 | 9 | 0.0 | 2 | 3 | 1.0 | 2 |
| domain.Coordinate | 0 | 8 | 0.0 | 5 | 2 | 0.25 | 2 |
| domain.HumanPlayer | 4 | 1 | 0.8 | 2 | 0 | — | — |
| domain.Line | 1 | 2 | 0.33 | 8 | 1 | 0.71 | 2 |
| domain.PcPlayer | 5 | 1 | 0.83 | 4 | 0 | — | — |
| domain.Piece | 1 | 3 | 0.25 | 1 | 1 | — | 1 |
| domain.Player | 3 | 6 | 0.33 | 2 | 1 | 1.0 | 2 |
| domain.Round | 1 | 1 | 0.5 | 2 | 3 | 0.0 | 1 |
| domain.Square | 3 | 1 | 0.75 | 4 | 2 | 0.33 | 1 |
| domain.State | 0 | 2 | 0.0 | 3 | 5 | 0.7 | 2 |
| domain.TicTacToe | 7 | 5 | 0.58 | 4 | 3 | 0.44 | 1 |

### Vocabulario, Open/Closed y preguntas

**without-ddd**
- Términos descartados encontrados: {'Cell': ['cell'], 'Status': ['Status', 'status'], 'move': ['chooseMove', 'findWinningMove', 'move', 'tryMove'], 'place': ['place']}
- Términos del glosario que faltan: Color, Column, Coordinate, Diagonal, HumanPlayer, InverseDiagonal, Put, Round, Square, TicTacToe
- Clases nuevas: tictactoe/PcPlayer.java; modificadas: tictactoe/Main.java; tests existentes tocados: ninguno
- Preguntas que Bob habría hecho:
  - Where should the PC move-selection logic live?
  - Should the Human player be extracted to its own class?
  - Which piece does the PC play in `--vs-pc` mode?
  - How to implement reading-order tie-break for rules 1 and 2?

**with-ddd**
- Términos descartados encontrados: {'Index': ['readingOrderIndex']}
- Términos del glosario que faltan: ninguno
- Clases nuevas: tictactoe/domain/HumanPlayer.java, tictactoe/domain/PcPlayer.java; modificadas: tictactoe/Main.java, tictactoe/console/ConsoleGame.java, tictactoe/domain/Player.java; tests existentes tocados: tictactoe/domain/RoundTest.java, tictactoe/domain/TicTacToeTest.java
- Preguntas que Bob habría hecho:
  - Should `chooseCoordinate` receive `Board` or `TicTacToe`?
  - Should `chooseCoordinate` signal "defer to console" via `null` return or a separate interface method?
  - Should `Player` be an abstract class or an interface?
  - What to name the two concrete subclasses?
  - How does PcPlayer implement reading order for Win/Block candidates?
  - Should REQUIREMENTS.md be updated to reflect CR-1?

## F3

| Métrica | without-ddd | with-ddd |
|---|---|---|
| Resultado (según Bob) | Done. `mvn -q verify` passes with 60 tests (0 failures). | done — `mvn -q verify` passes with 93 tests, 0 failures. |
| Duración (s, Timing del historial) | 110 | 351 |
| Contexto al final (k tokens) | 34.4 | 68.1 |
| Costo (Bobcoins) | 1.0 | 4.09 |
| Fuente del costo | bob.db | bob.db |
| Sesiones de Bob en la fase | 1 | 1 |
| Llamadas al modelo | 18 | 44 |
| Builds con mvn | 2 | 2 |
| Lecturas: docs · código · tests | 1 · 3 · 2 | 9 · 11 · 2 |
| Antes de la primera edición: docs leídos · código leído | 1 · 5 | 8 · 12 |
| Primera edición en | codigo | docs |
| ¿Docs editados antes que el código? | False | True |
| mvn -q verify | ok | ok |
| Tests unitarios | 60 | 93 |
| Aceptación (escenarios ocultos) | 0/16 | 16/16 |
| Lógica correcta aunque falle el formato | 15/16 | 16/16 |
| Aceptación F1 (regresiones) | 0/9 | 9/9 |
| Aceptación F2 | 0/3 | 3/3 |
| Aceptación F3 | 0/4 | 4/4 |
| Escenarios fallidos | F1-01, F1-02, F1-03, F1-04, F1-05, F1-06, F1-07, F1-08, F1-09, F2-01, F2-02, F2-03, F3-01, F3-02, F3-03, F3-04 | ninguno |
| Mutation score total (%) | 71 | 60 |
| Mutantes muertos/generados | 138/195 | 150/249 |
| Mutation score sin la consola (%) | 93 | 90 |
| Mutantes sin la consola | 138/149 | 150/167 |
| Clases de producción | 5 | 16 |
| Paquetes | 1 | 3 |
| Líneas de producción | 279 | 583 |
| Líneas de test | 459 | 747 |
| Acoplamiento eferente Ce (mediana · máx) | 1 · 4 | 3.5 · 8 |
| Acoplamiento aferente Ca (máx) | 4 | 11 |
| Cohesión LCOM* mediana, sin getters (menor = mejor) | 0.58 | 0.6 |
| Clases con campos partibles (LCOM4 > 1) | 1 | 6 |
| Clases con E/S de consola | Main | Main, console.ConsoleGame, console.ThreePiecesConsoleGame |
| Encapsulamiento: campos de instancia privados | 8/8 | 23/23 |
| Encapsulamiento: métodos públicos (%) | 80 | 67 |
| Encapsulamiento: métodos públicos que cambian estado | Board.place, Game.tryMove, ThreePiecesGame.tryPut | domain.ThreePiecesTicTacToe.move, domain.ThreePiecesTicTacToe.put, domain.TicTacToe.put |
| Encapsulamiento: estado mutable expuesto (getter devuelve array u objeto modificable) | Game.board() devuelve Board, ThreePiecesGame.board() devuelve Board | ninguno |
| Ciclos entre paquetes | ninguno | ninguno |
| Clases nuevas / modificadas / borradas | 1 / 1 / 0 | 2 / 4 / 0 |
| Open/Closed (nuevas ÷ (nuevas + modificadas)) | 0.5 | 0.33 |
| Líneas cambiadas en clases existentes | 90 | 59 |
| Tests existentes tocados | 0 | 0 |
| docs/domain actualizados | — | decisions.md, game-states.md, glossary.md, invariants.md, model.puml, modules.md |
| ¿Bob modificó REQUIREMENTS.md? | False | True |
| Foto reconstruida desde bob.db | False | False |
| Decisiones propias (total) | 4 | 6 |
|   · ambiguity | 0 | 1 |
|   · gap | 2 | 1 |
|   · design | 2 | 4 |
|   · naming | 0 | 0 |
|   · scope | 0 | 0 |
|   · deviation | 0 | 0 |
|   · workaround | 0 | 0 |
| Pendientes | 0 | 0 |
| Documentos en los que se apoyó | 3 | 8 |
| Términos descartados en el código | 5 | 1 |
| Términos del glosario presentes | 11/21 | 21/21 |

### Acoplamiento y cohesión por clase

**without-ddd** — dependencias entre paquetes: ninguna

| Clase | Ce (usa) | Ca (la usan) | Inestabilidad Ce/(Ca+Ce) | Métodos | Campos | LCOM* | LCOM4 |
|---|---|---|---|---|---|---|---|
| Board | 0 | 4 | 0.0 | 7 | 1 | 0.5 | 1 |
| Game | 1 | 1 | 0.5 | 3 | 3 | 0.33 | 1 |
| Main | 4 | 0 | 1.0 | 4 | 0 | — | — |
| PcPlayer | 1 | 1 | 0.5 | 3 | 2 | 0.75 | 2 |
| ThreePiecesGame | 1 | 1 | 0.5 | 7 | 5 | 0.67 | 1 |

**with-ddd** — dependencias entre paquetes: {'(raíz)': ['tictactoe.console', 'tictactoe.domain'], 'tictactoe.console': ['tictactoe.domain']}

| Clase | Ce (usa) | Ca (la usan) | Inestabilidad Ce/(Ca+Ce) | Métodos | Campos | LCOM* | LCOM4 |
|---|---|---|---|---|---|---|---|
| Main | 8 | 0 | 1.0 | 2 | 0 | — | — |
| console.ConsoleGame | 5 | 1 | 0.83 | 5 | 2 | 0.5 | 1 |
| console.ThreePiecesConsoleGame | 4 | 1 | 0.8 | 8 | 2 | 0.5 | 1 |
| domain.Board | 5 | 2 | 0.71 | 8 | 1 | 0.71 | 2 |
| domain.Color | 0 | 11 | 0.0 | 2 | 3 | 1.0 | 2 |
| domain.Coordinate | 0 | 10 | 0.0 | 5 | 2 | 0.25 | 2 |
| domain.HumanPlayer | 4 | 1 | 0.8 | 2 | 0 | — | — |
| domain.Line | 1 | 2 | 0.33 | 8 | 1 | 0.71 | 2 |
| domain.PcPlayer | 5 | 1 | 0.83 | 4 | 0 | — | — |
| domain.Piece | 1 | 4 | 0.2 | 1 | 1 | — | 1 |
| domain.Player | 3 | 7 | 0.3 | 2 | 1 | 1.0 | 2 |
| domain.Round | 1 | 2 | 0.33 | 2 | 3 | 0.0 | 1 |
| domain.Square | 3 | 1 | 0.75 | 5 | 2 | 0.25 | 1 |
| domain.State | 0 | 4 | 0.0 | 4 | 7 | 0.76 | 2 |
| domain.ThreePiecesTicTacToe | 7 | 2 | 0.78 | 8 | 6 | 0.71 | 1 |
| domain.TicTacToe | 7 | 5 | 0.58 | 4 | 3 | 0.44 | 1 |

### Vocabulario, Open/Closed y preguntas

**without-ddd**
- Términos descartados encontrados: {'Cell': ['cell'], 'Index': ['playerIndex'], 'Phase': ['isPutPhase'], 'Status': ['Status', 'status', 'updateStatusOrSwitch'], 'place': ['place']}
- Términos del glosario que faltan: Color, Column, Coordinate, Diagonal, HumanPlayer, InverseDiagonal, PiecesRemaining, Round, Square, TicTacToe
- Clases nuevas: tictactoe/ThreePiecesGame.java; modificadas: tictactoe/Main.java; tests existentes tocados: ninguno
- Preguntas que Bob habría hecho:
  - Should `ThreePiecesGame` extend or wrap `Game`, or be a standalone class?
  - Should the three-pieces game loop live in `Main.java` or a separate class?
  - What should `isPutPhase()` report when both players are in different phases (e.g. X placed 3, O placed 2)?
  - The CR says a line with the wrong number of tokens is "also invalid" — should the put-phase check for 4 tokens (a move command) be rejected with "Invalid"?

**with-ddd**
- Términos descartados encontrados: {'Index': ['readingOrderIndex']}
- Términos del glosario que faltan: ninguno
- Clases nuevas: tictactoe/console/ThreePiecesConsoleGame.java, tictactoe/domain/ThreePiecesTicTacToe.java; modificadas: tictactoe/Main.java, tictactoe/domain/Board.java, tictactoe/domain/Square.java, tictactoe/domain/State.java; tests existentes tocados: ninguno
- Preguntas que Bob habría hecho:
  - Should `ThreePiecesTicTacToe` extend `TicTacToe` or be a separate class?
  - Should the console handle both modes in one class or two?
  - How should `State` signal whether we are in the move phase?
  - How should `Board` support moving a Piece?
  - The sequence "after both players have 0 pieces remaining, who moves first?"
  - The CR says move input is "fromRow fromColumn toRow toColumn" — should the console also validate that from ≠ to?

