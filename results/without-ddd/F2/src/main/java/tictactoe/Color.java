package tictactoe;

enum Color {
    X, O;

    Color other() {
        return this == X ? O : X;
    }
}
