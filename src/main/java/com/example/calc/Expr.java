package com.example.calc;

// AST moderno con sealed + records: inmutable y conciso.
public sealed interface Expr permits Expr.NumberLit, Expr.Unary, Expr.Binary, Expr.Call, Expr.Ans {
    record NumberLit(double value) implements Expr {}
    record Unary(char op, Expr expr) implements Expr {}
    record Binary(Expr left, char op, Expr right) implements Expr {}
    record Call(String name, Expr arg) implements Expr {}
    record Ans() implements Expr {} // Implementada la expresión Ans para manejar el resultado previo
}
