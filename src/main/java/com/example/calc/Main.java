package com.example.calc;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String HELP = """
        === Calc21 ===
        Operaciones: +  -  *  /  ^    Funciones: sin(x), cos(x)
        Precedencia: ^ (derecha), luego * /, luego + -
        Ejemplos:
          1 + 2*3
          (1 + 2) * 3 ^ 2
          -2 ^ 3
          sin(3.14159/2) + cos(0)
        Escribe 'exit' para salir.
        """;

    public static void main(String[] args) {
        System.out.println(HELP);
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.equalsIgnoreCase("exit")) break;
            if (line.isBlank()) continue;

            try {
                List<Token> tokens = new Lexer(line).lex();
                Expr ast = new Parser(tokens).parse();
                double result = Evaluator.eval(ast);
                System.out.println(result);
            } catch (IllegalArgumentException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Error inesperado: " + ex);
            }
        }
        System.out.println("Adiós!");
        sc.close();
    }
}
