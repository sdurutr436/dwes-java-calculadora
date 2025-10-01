package com.example.calc;

import java.util.List;

import com.example.calc.Expr.Ans;
import com.example.calc.Expr.Binary;
import com.example.calc.Expr.Call;
import com.example.calc.Expr.NumberLit;
import com.example.calc.Expr.Unary;
import static com.example.calc.TokenType.CARET;
import static com.example.calc.TokenType.EOF;
import static com.example.calc.TokenType.IDENT;
import static com.example.calc.TokenType.LPAREN;
import static com.example.calc.TokenType.MINUS;
import static com.example.calc.TokenType.NUMBER;
import static com.example.calc.TokenType.PLUS;
import static com.example.calc.TokenType.RPAREN;
import static com.example.calc.TokenType.SLASH;
import static com.example.calc.TokenType.STAR;

public final class Parser {
    private final List<Token> ts;
    private int i = 0;

    public Parser(List<Token> tokens) { this.ts = tokens; }

    public Expr parse() {
        Expr e = expr();
        expect(EOF, "Fin de expresión esperado");
        return e;
    }

    private Expr expr() {
        Expr left = term();
        while (match(PLUS) || match(MINUS)) {
            char op = prev().lexeme().charAt(0);
            Expr right = term();
            left = new Binary(left, op, right);
        }
        return left;
    }

    private Expr term() {
        Expr left = factor();
        while (match(STAR) || match(SLASH)) {
            char op = prev().lexeme().charAt(0);
            Expr right = factor();
            left = new Binary(left, op, right);
        }
        return left;
    }

    private Expr factor() { return power(); }

    private Expr power() {
        Expr base = unary();
        if (match(CARET)) {
            Expr exponent = power(); // asociatividad a derecha
            return new Binary(base, '^', exponent);
        }
        return base;
    }

    private Expr unary() {
        if (match(PLUS) || match(MINUS)) {
            char op = prev().lexeme().charAt(0);
            Expr right = unary();
            return new Unary(op, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(NUMBER)) return new NumberLit(Double.parseDouble(prev().lexeme()));
        if (match(IDENT)) {
            String name = prev().lexeme();
            if (name.equalsIgnoreCase("ans")) {
                return new Ans(); // caso especial para "Ans"
            }
            expect(LPAREN, "Se esperaba '(' tras función");
            Expr arg = expr();
            expect(RPAREN, "Se esperaba ')' tras argumento");
            return new Call(name, arg);
        }
        if (match(LPAREN)) {
            Expr inside = expr();
            expect(RPAREN, "Se esperaba ')'");
            return inside;
        }
        throw error("Token inesperado: " + peek().type() + " en pos " + peek().position());
    }


    private boolean match(TokenType t) { if (check(t)) { i++; return true; } return false; }
    private boolean check(TokenType t) { return peek().type() == t; }
    private Token peek() { return ts.get(i); }
    private Token prev() { return ts.get(i-1); }
    private void expect(TokenType t, String msg) { if (!match(t)) throw error(msg + " (pos " + peek().position() + ")"); }
    private IllegalArgumentException error(String msg) { return new IllegalArgumentException(msg); }
}
