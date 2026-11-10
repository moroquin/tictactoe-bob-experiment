# Resumen ejecutivo

Bob construyó el mismo Tic-Tac-Toe dos veces con los mismos prompts: **without-ddd** solo con los requisitos, **with-ddd** con los requisitos más la documentación DDD (glosario, modelo, invariantes, estados y módulos). Cada fase fue una sesión nueva sin memoria: F1 juego base, F2 contra el PC, F3 tres piezas que se mueven.

> Una ejecución por carpeta: las diferencias son observaciones de esta ejecución, no una prueba. El detalle de cada número está en `metricas.md`.

## Marcador

En negrita, el mejor valor de cada fase. Sin negrita: empate o métrica descriptiva.

| Métrica | F1 sin DDD | F1 con DDD | F2 sin DDD | F2 con DDD | F3 sin DDD | F3 con DDD |
|---|---|---|---|---|---|---|
| **Exactitud** | | | | | | |
| Partidas ocultas correctas | 0/9 | **9/9** | 0/12 | **12/12** | 0/16 | **16/16** |
|   · con la lógica correcta, aunque falle el formato | 9/9 | 9/9 | 11/12 | **12/12** | 15/16 | **16/16** |
| Regresiones de F1 | 0/9 | **9/9** | 0/9 | **9/9** | 0/9 | **9/9** |
| Mutation score sin la consola (%) | **96** | 90 | **94** | 88 | **93** | 90 |
| **Costo del agente** | | | | | | |
| Tiempo (s) | **183** | 235 | **175** | 428 | **110** | 351 |
| Contexto al final (k tokens) | **25.4** | 46.7 | **35.4** | 94.8 | **34.4** | 68.1 |
| Costo (Bobcoins) | **1.06** | 2.12 | **1.58** | 5.05 | **1.0** | 4.09 |
| **Cómo trabajó Bob** | | | | | | |
| Docs leídos antes de la primera edición | 1 | 7 | 1 | 8 | 1 | 8 |
| Archivos de código leídos antes de la primera edición | **0** | 1 | **4** | 9 | **5** | 12 |
| Lecturas de código en toda la sesión | 0 | 0 | **4** | 7 | **3** | 11 |
| Llamadas al modelo | **27** | 31 | **29** | 47 | **18** | 44 |
| **Autonomía** | | | | | | |
| Decisiones propias | 3 | 3 | **4** | 6 | **4** | 6 |
|   · por huecos o ambigüedades de la documentación | 2 | **1** | 1 | **0** | 2 | 2 |
| **Diseño** | | | | | | |
| Clases · líneas de producción | 3 · 113 | 12 · 317 | 4 · 168 | 14 · 384 | 5 · 279 | 16 · 583 |
| Ce máx (clases que usa la más acoplada) | **2** | 7 | **3** | 7 | **4** | 8 |
| LCOM* mediana (menor = más cohesiva) | **0.42** | 0.5 | **0.5** | 0.58 | **0.58** | 0.6 |
| Métodos públicos que cambian estado | 2 | **1** | 2 | **1** | 3 | 3 |
| Getters que exponen estado modificable | 1 | **0** | 1 | **0** | 2 | **0** |
| Clases con E/S de consola | 1 | 2 | 1 | 2 | 1 | 3 |
| **Cambio** | | | | | | |
| Clases existentes modificadas | — | — | **1** | 3 | **1** | 4 |
| Líneas cambiadas en clases existentes | — | — | **20** | 60 | 90 | **59** |
| Tests existentes tocados | — | — | **0** | 2 | 0 | 0 |
| **Lenguaje** | | | | | | |
| Términos descartados en el código | 3 | **0** | 4 | **1** | 5 | **1** |
| Términos del glosario presentes | 6/17 | **17/17** | 9/19 | **19/19** | 11/21 | **21/21** |

## Costo acumulado

| | without-ddd | with-ddd | with-ddd ÷ without-ddd |
|---|---|---|---|
| Tiempo (s) | 468 | 1014 | 2.17× |
| Costo (Bobcoins) | 3.64 | 11.26 | 3.09× |

Fases incluidas: F1, F2, F3.

## Hallazgos

### F1
**without-ddd**
- Partidas ocultas que fallan: F1-01 (X wins on row 1); F1-02 (O wins on column 2); F1-03 (X wins on the diagonal); F1-04 (O wins on the inverse diagonal); F1-05 (X wins on column 3); F1-06 (Draw); F1-07 (Occupied, out of range and garbage are rejected; X wins); F1-08 (Win on the 9th put beats Draw); F1-09 (Wrong amount of numbers and zero are rejected).
- Con la lógica del juego correcta: 9/9. El resto falla por formato: la salida no cumple el contrato de consola (por ejemplo, un prompt sin salto de línea pegado a la primera fila del tablero).
- Huecos que Bob tuvo que decidir solo: REQUIREMENTS.md says "any other output … is allowed before the final board" — what to print as prompts? / What should happen if stdin ends before the game is over (e.g. in a script that supplies too few moves)?
- Estado expuesto: Game.board() devuelve Board.
- Métodos públicos que cambian estado: Board.place, Game.tryMove.
- Términos descartados en el código: Status (Status, status), move (tryMove), place (place).
- La foto de esta fase se reconstruyó desde bob.db (no se tomó a tiempo) y se validó contra las lecturas de la fase siguiente.

**with-ddd**
- Todas las partidas ocultas correctas (9/9).
- Huecos que Bob tuvo que decidir solo: The test for I6 "win checked before draw" had a move sequence where O actually won before X; the test was wrong
- Métodos públicos que cambian estado: domain.TicTacToe.put.
- La foto de esta fase se reconstruyó desde bob.db (no se tomó a tiempo) y se validó contra las lecturas de la fase siguiente.

### F2
**without-ddd**
- Partidas ocultas que fallan: F1-01 (X wins on row 1); F1-02 (O wins on column 2); F1-03 (X wins on the diagonal); F1-04 (O wins on the inverse diagonal); F1-05 (X wins on column 3); F1-06 (Draw); F1-07 (Occupied, out of range and garbage are rejected; X wins); F1-08 (Win on the 9th put beats Draw); F1-09 (Wrong amount of numbers and zero are rejected); F2-01 (PC takes the center, a corner, then wins); F2-02 (Human wins with a fork); F2-03 (Square taken by the PC is rejected; Draw).
- Con la lógica del juego correcta: 11/12. El resto falla por formato: la salida no cumple el contrato de consola (por ejemplo, un prompt sin salto de línea pegado a la primera fila del tablero).
- Huecos que Bob tuvo que decidir solo: Which piece does the PC play in `--vs-pc` mode?
- Estado expuesto: Game.board() devuelve Board.
- Métodos públicos que cambian estado: Board.place, Game.tryMove.
- Términos descartados en el código: Cell (cell), Status (Status, status), move (chooseMove, findWinningMove, move, tryMove), place (place).
- Clases existentes modificadas: Main.

**with-ddd**
- Todas las partidas ocultas correctas (12/12).
- Métodos públicos que cambian estado: domain.TicTacToe.put.
- Términos descartados en el código: Index (readingOrderIndex).
- Clases existentes modificadas: Main, ConsoleGame, Player.
- Bob modificó REQUIREMENTS.md, que debía quedar igual en las dos carpetas.
- Docs del dominio actualizados: decisions.md, glossary.md, model.puml, modules.md.

### F3
**without-ddd**
- Partidas ocultas que fallan: F1-01 (X wins on row 1); F1-02 (O wins on column 2); F1-03 (X wins on the diagonal); F1-04 (O wins on the inverse diagonal); F1-05 (X wins on column 3); F1-06 (Draw); F1-07 (Occupied, out of range and garbage are rejected; X wins); F1-08 (Win on the 9th put beats Draw); F1-09 (Wrong amount of numbers and zero are rejected); F2-01 (PC takes the center, a corner, then wins); F2-02 (Human wins with a fork); F2-03 (Square taken by the PC is rejected; Draw); F3-01 (Win while putting the third piece); F3-02 (X wins by moving a piece); F3-03 (Invalid moves are rejected; X wins); F3-04 (O wins by moving a piece).
- Con la lógica del juego correcta: 15/16. El resto falla por formato: la salida no cumple el contrato de consola (por ejemplo, un prompt sin salto de línea pegado a la primera fila del tablero).
- Huecos que Bob tuvo que decidir solo: What should `isPutPhase()` report when both players are in different phases (e.g. X placed 3, O placed 2)? / The CR says a line with the wrong number of tokens is "also invalid" — should the put-phase check for 4 tokens (a move command) be rejected with "Invalid"?
- Estado expuesto: Game.board() devuelve Board, ThreePiecesGame.board() devuelve Board.
- Métodos públicos que cambian estado: Board.place, Game.tryMove, ThreePiecesGame.tryPut.
- Términos descartados en el código: Cell (cell), Index (playerIndex), Phase (isPutPhase), Status (Status, status, updateStatusOrSwitch), place (place).
- Clases existentes modificadas: Main.

**with-ddd**
- Todas las partidas ocultas correctas (16/16).
- Huecos que Bob tuvo que decidir solo: The sequence "after both players have 0 pieces remaining, who moves first?" / The CR says move input is "fromRow fromColumn toRow toColumn" — should the console also validate that from ≠ to?
- Métodos públicos que cambian estado: domain.ThreePiecesTicTacToe.move, domain.ThreePiecesTicTacToe.put, domain.TicTacToe.put.
- Términos descartados en el código: Index (readingOrderIndex).
- Clases existentes modificadas: Main, Board, Square, State.
- Bob modificó REQUIREMENTS.md, que debía quedar igual en las dos carpetas.
- Docs del dominio actualizados: decisions.md, game-states.md, glossary.md, invariants.md, model.puml, modules.md.

## Cómo leer las métricas

- **Ce (acoplamiento eferente):** cuántas clases del proyecto usa una clase. Menor es mejor.
- **Ca (acoplamiento aferente):** cuántas clases la usan. Solo está bien que sea alto en clases estables, como un enum.
- **LCOM\*:** falta de cohesión. 0 significa que todos los métodos usan todos los campos; 1 o más, que la clase mezcla responsabilidades. Se excluyen los getters.
- **Mutation score:** PIT introduce pequeños errores en el código y cuenta cuántos detectan los tests. Se muestra sin la consola, que ninguna carpeta testea.
- **Encapsulamiento:** los métodos públicos que cambian estado son las puertas para modificar el juego; un getter que expone estado modificable deja saltarse las reglas.
- **Open/Closed:** un cambio bien absorbido agrega clases y toca pocas líneas de las existentes.
- **Contexto:** lo que ocupaba la ventana de Bob al terminar, no el total consumido; el costo en Bobcoins refleja mejor el gasto.
