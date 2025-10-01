# Retos de comprensión

En esta parte se da respuestas a los retos de comprensión sobre el proyecto **DWES-JAVA-CALCULADORA**, una calculadora implementada en Java con un *Lexer*, *Parser* y *Evaluator*.  

---

## 1. ¿Qué es un Token?

Un **token** es una unidad mínima de significado que el **lexer** (analizador léxico) extrae del texto fuente.  

Cada token tiene:
- **Tipo** → categoría (ejemplo: número, operador, paréntesis).  
- **Lexema** → el texto exacto que apareció en la entrada.  
- **Posición** → índice dentro de la cadena original.  

### Ejemplo con la expresión `3 + 5 * 2`

El **lexer** transforma la cadena en la siguiente lista de tokens:

```
Token(NUMBER, "3", 0)
Token(PLUS, "+", 2)
Token(NUMBER, "5", 4)
Token(STAR, "*", 6)
Token(NUMBER, "2", 8)
Token(EOF, "", 9)
```

---

## 2. Diferencia entre Lexer y Parser

### Lexer (analizador léxico)
- Se encarga de **leer el texto carácter por carácter** y generar tokens.  
- Reconoce símbolos (`+`, `*`, `^`), números (`3.14`) o identificadores (`sin`, `cos`).  
- No entiende reglas de precedencia ni jerarquía.  

Ejemplo: convierte `3 + 5 * 2` en la lista de tokens mostrada arriba.  

### Parser (analizador sintáctico)
- Usa esos tokens para construir un **árbol sintáctico abstracto (AST)**.  
- Aplica las reglas de la gramática (precedencia de operaciones, paréntesis, asociatividad, etc.).  

Ejemplo: a partir de `3 + 5 * 2` construye este AST:

```
     (+)
    /   \
 (3)     (*)
        /   \
      (5)   (2)
```

---

## 3. ¿Qué significa que el parser sea recursivo?

Un parser es **recursivo** cuando sus funciones **se llaman a sí mismas** para manejar expresiones anidadas.  

### Ejemplo en el código (`power()`):

```java
private Expr power() {
    Expr base = unary();
    if (match(CARET)) {
        Expr exponent = power(); // ← llamada recursiva
        return new Binary(base, '^', exponent);
    }
    return base;
}
```

Aquí `power()` se llama a sí mismo para procesar potencias anidadas.  
Esto permite que la expresión `2 ^ 3 ^ 2` se interprete correctamente como:  

```
2 ^ (3 ^ 2)
```

y no como:

```
(2 ^ 3) ^ 2
```

---

## Resumen

- Un **Token** es la unidad léxica (ejemplo: `NUMBER "3"`).  
- El **Lexer** convierte texto en tokens.  
- El **Parser** organiza los tokens en un AST aplicando reglas gramaticales.  
- El parser es **recursivo** porque usa llamadas a sí mismo para manejar expresiones anidadas como potencias y paréntesis.  

---

# Retos de depuración

En esta parte se da respuesta a los retos de depuración del código

---

## 1. Entrada: `2 + 3 * 4`

**Paso a paso:**

1. **Lexer** convierte la cadena en tokens:

```
[NUMBER(2), PLUS(+), NUMBER(3), STAR(*), NUMBER(4), EOF]
```

2. **Parser** construye el AST respetando precedencia (`*` > `+`):

```text
Binary(
    left = NumberLit(2),
    op = '+',
    right = Binary(
        left = NumberLit(3),
        op = '*',
        right = NumberLit(4)
    )
)
```

3. **Evaluator** evalúa recursivamente:

- Subárbol derecho: `3 * 4 = 12`
- Árbol principal: `2 + 12 = 14`

**Resultado:**  

```
14.0
```

> La calculadora respeta la precedencia estándar de operadores.

---

## 2. Entrada no válida: `2 + *`

**Qué ocurre:**

1. Lexer produce tokens:

```
[NUMBER(2), PLUS(+), STAR(*), EOF]
```

2. Parser intenta construir `expr()`:

- `expr()` espera un `term()` a la derecha de `+`.
- `term()` llama a `factor()` → `power()` → `unary()` → `primary()`.
- En `primary()`, el token `*` **no es un número, paréntesis ni identificador**.

**Resultado:**  

Lanza excepción en `primary()`:

```text
Error: Token inesperado: STAR en pos 4
```

> La calculadora no se bloquea, solo informa el error de forma clara.

---

## 3. Entrada: `(2 + 3) ^ 2`

**Paso a paso:**

1. Lexer:

```
[LPAREN, NUMBER(2), PLUS, NUMBER(3), RPAREN, CARET, NUMBER(2), EOF]
```

2. Parser construye AST respetando paréntesis y asociatividad a la derecha de `^`:

```text
Binary(
    left = Binary(
        left = NumberLit(2),
        op = '+',
        right = NumberLit(3)
    ),
    op = '^',
    right = NumberLit(2)
)
```

3. Evaluator:

- Eval del subárbol: `(2 + 3) = 5`
- Eval del árbol principal: `5 ^ 2 = 25`

**Resultado:**  

```
25.0
```

> Los paréntesis se respetan correctamente y la potencia es asociativa a la derecha.

---

## Conclusión

- La calculadora respeta **precedencia de operadores** y **paréntesis**.
- Las expresiones no válidas generan errores claros sin bloquear el programa.
- Funciones (`sin`, `cos`) y operadores básicos (`+ - * / ^`) se evaluan correctamente.

# Retos de predicción

Esta parte da respuesta a las preguntas sobre la evaluación de expresiones.  

---

## 1. ¿Qué resultado debería devolver la expresión `cos(0) + sin(90)`?

**Importante**: las funciones trigonométricas en Java (`Math.sin`, `Math.cos`) esperan **radianes**, no grados.  

- `cos(0)` = `1.0`  
- `sin(90)` → aquí 90 está en **radianes**, no en grados.  
  - En radianes, `sin(90)` ≈ `0.8939966636`  
  - (porque 90 radianes = 90 × 180/π ≈ 5157 grados).  

Entonces:
```
cos(0) + sin(90) ≈ 1.0 + 0.8939966636 = 1.8939966636
```  

Si se esperaran grados, habría que convertir (90° = π/2 rad), pero **el código no hace la conversión**.  

---

## 2. ¿Cuál es el resultado de `2 ^ 3 ^ 2`?

El parser implementa la potencia (`^`) con **asociatividad a la derecha** (gracias a la recursión en la función `power()`).  

Esto significa que la expresión se interpreta como:  

```
2 ^ (3 ^ 2)
```

- `3 ^ 2 = 9`  
- `2 ^ 9 = 512`  

Resultado: **512**.  

Si fuera asociatividad a la izquierda, se calcularía `(2 ^ 3) ^ 2 = 8 ^ 2 = 64`, pero el código no lo hace así.  

---

## 3. ¿Qué devuelve la calculadora con `(2 + 3) * (4 + 5)`?

La calculadora respeta la precedencia y los paréntesis:  

1. `(2 + 3)` = `5`  
2. `(4 + 5)` = `9`  
3. `5 * 9 = 45`  

Resultado: **45**.  

---


# Retos de diseño

Esta parte da respuesta a las preguntas sobre las decisiones de diseño en la implementación de la calculadora.  

---

## 1. Ventaja de usar un parser recursivo frente a bucle + pila manual

El **parser recursivo** tiene varias ventajas:  

- El código es **más simple y legible**, ya que las llamadas recursivas reflejan directamente la estructura de la gramática (ejemplo: `expr → term → factor → primary`).  
- Maneja de forma **natural** la precedencia y la asociatividad de los operadores (por ejemplo, la potencia `^` con asociatividad a la derecha).  
- Facilita la extensión de la gramática, añadiendo nuevas reglas con funciones adicionales.  

Con un bucle y pila manual, habría que implementar toda la lógica de precedencia y asociaciones manualmente, lo que vuelve el código más largo, más propenso a errores y menos intuitivo.  

---

## 2. ¿Por qué separar en fases lexer → parser → evaluator?

Separar en fases ofrece varias ventajas:  

- **Claridad y modularidad**: cada fase tiene una única responsabilidad (lexer convierte texto en tokens, parser tokens en AST, evaluator AST en resultado).  
- **Reutilización**: se puede usar el mismo lexer/parser con diferentes evaluadores (por ejemplo, para imprimir, optimizar, o compilar la expresión).  
- **Depuración**: es más fácil identificar errores (ejemplo: ¿el problema está en el análisis léxico, en la sintaxis o en la evaluación?).  
- **Extensibilidad**: se pueden añadir nuevas operaciones o funciones sin modificar todo el sistema.  

Si todo se hiciera en un solo método, el código sería **complejo, difícil de leer, de depurar y de mantener**.  

---

## 3. ¿Dónde añadir soporte para variables (`x = 5`, `y = 2 * x`)?

Para añadir variables, habría que extender el sistema en **dos partes** principales:  

1. **En el parser**  
   - Detectar una asignación con el operador `=` (por ejemplo, `IDENT '=' expr`).  

2. **En el evaluator**  
   - Mantener un **entorno de variables** (un `Map<String, Double>`).  
   - Cuando se evalúe una `Assign`, guardar el valor en el mapa.  
   - Cuando se evalúe un `Call` o un `Variable`, buscar el valor en el mapa.  

Así, la lógica de variables queda separada y consistente:  
- El **lexer** no necesita cambiar (ya reconoce `IDENT`).  
- El **parser** entiende la sintaxis `x = ...`.  
- El **evaluator** gestiona el almacenamiento y recuperación de valores.  

---

