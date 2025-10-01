package com.example.calc;

import com.example.calc.Expr.Binary;
import com.example.calc.Expr.Call;
import com.example.calc.Expr.NumberLit;
import com.example.calc.Expr.Unary;

public final class Evaluator {
    private Evaluator() { throw new AssertionError("No instanciable"); }

    public static double eval(Expr e) {
        return switch (e) {

            // Caso para manejar el resultado previo
            case Expr.Ans a -> throw new IllegalArgumentException("No hay valor previo");
            
            case NumberLit n -> n.value();
            case Unary u -> {
                double v = eval(u.expr());
                yield (u.op() == '-') ? -v : +v;
            }
            case Binary b -> {
                double l = eval(b.left());
                double r = eval(b.right());
                yield switch (b.op()) {
                    case '+' -> l + r;
                    case '-' -> l - r;
                    case '*' -> l * r;
                    case '/' -> l / r;
                    case '^' -> Math.pow(l, r);
                    default -> throw new IllegalStateException("Operador no soportado: " + b.op());
                };
            }
            case Call c -> {
                double x = eval(c.arg());
                yield switch (c.name()) {
                    case "sin" -> Math.sin(x);
                    case "cos" -> Math.cos(x);
                    case "tan" -> Math.tan(x);
                    case "sqrt" -> Math.sqrt(x);
                    default -> throw new IllegalArgumentException("Función no soportada: " + c.name());
                };
            }
        };
    }
}
