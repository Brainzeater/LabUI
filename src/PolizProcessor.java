import java.util.*;

/**
 * Процессор постфиксных токенов - составляет таблицу переменных.
 */
public class PolizProcessor {

    /* Таблица переменных */
    private HashMap<String, Integer> varTable;
    private HashMap<String, HashMap<String, Integer>> structTable;
    /* Входной список постфиксных токенов */
    private List<Token> postfixToken;
    /* Номер текущего токена в списке */
    private int currentTokenNumber;
    /* Стек токенов - хранит числа и токены со значениями
     * - операнды арифметических операций */
    private Stack<Token> tokenStack;
    private Token current;

    /* Инициализация полей в конструкторе */
    public PolizProcessor(List<Token> postfixToken, HashMap<String, Integer> vT,
                          HashMap<String, HashMap<String, Integer>> structTable) {
        varTable = vT;
        this.postfixToken = postfixToken;
        this.structTable = structTable;
        currentTokenNumber = 0;
        tokenStack = new Stack<Token>();
    }

    public void setPostfixToken(List<Token> postfix) {
        this.postfixToken = postfix;
    }

    /* Запуск процессора */
    public void go() {
        currentTokenNumber = 0;
        /* Повторять до конца файла */
        while (currentTokenNumber < postfixToken.size()) {
            step();
            /* Перейти к следующему токену из списка токенов */
            currentTokenNumber++;
        }
    }

    private void step() {
            /* Текущий токен из списка токенов */
        current = postfixToken.get(currentTokenNumber);
        /* Установить последовательность действий для поступившего токена */
        switch (current.getName()) {
                /* Токен со значением */
            case "VAR_NAME":
                    /* Если в таблице переменных отсутствует переменная
                     * с именем пришедшего токена, то объявить её с нулевым значением */
                if (!varTable.containsKey(current.getValue())) {
                    declare(current);
                }
                    /* Добавить в стек токенов текущий токен для
                     * дальнейших операций */
                tokenStack.push(current);
                break;
            case "STRUCT_TOKEN":
                tokenStack.push(current);
                break;
                /* Число */
            case "DIGIT":
                    /* Добавить в стек токенов */
                tokenStack.push(current);
                break;
                /* Присвоить */
            case "ASSIGN_OP":
                    /* Присвоить значение последнего токена иа
                     стека токенов предпоследнему */
                assign(tokenStack.pop(), tokenStack.pop());
                break;
                /* Сложение */
            case "ASSIGN_ADD":
                    /* Добавить в список токенов новый токен с именем DIGIT
                     * и со значением, равным сумме последних двух токенов
                     * из стека токенов */
                tokenStack.push(new Token("DIGIT",
                        String.valueOf(getTokenValue(tokenStack.pop())
                                + getTokenValue(tokenStack.pop()))));
                break;
                /* Вычитание */
            case "ASSIGN_SUB":
                    /* Вычитаемое */
                Token subtrahend = tokenStack.pop();
                    /* Уменьшаемое */
                Token minuend = tokenStack.pop();
                    /* Добавить в список токенов новый токен с именем DIGIT
                     * и со значением, равным разности последних двух токенов
                     * из стека токенов */
                tokenStack.push(new Token("DIGIT",
                        String.valueOf(getTokenValue(minuend)
                                - getTokenValue(subtrahend))));
                break;
                /* Произведение */
            case "ASSIGN_MULT":
                    /* Добавить в список токенов новый токен с именем DIGIT
                     * и со значением, равным произведению последних двух токенов
                     * из стека токенов */
                tokenStack.push(new Token("DIGIT",
                        String.valueOf(getTokenValue(tokenStack.pop())
                                * getTokenValue(tokenStack.pop()))));
                break;
                /* Деление */
            case "ASSIGN_DIV":
                    /* Делитель */
                Token divider = tokenStack.pop();
                    /* Делимое */
                Token dividend = tokenStack.pop();
                    /* Добавить в список токенов новый токен с именем DIGIT
                     * и со значением, равным частному последних двух токенов
                     * из стека токенов */
                tokenStack.push(new Token("DIGIT",
                        String.valueOf(getTokenValue(dividend)
                                / getTokenValue(divider))));
                break;
        }
    }

    /* Объявление переменной */
    private void declare(Token declaredToken) {
        /* Добавить в таблицу переменных новую переменную
         * с именем токена и нулевым значением */
        varTable.put(declaredToken.getValue(), 0);
    }

    /* Присваивание */
    private void assign(Token what, Token where) {
        if (where.getName().equals("VAR_NAME")) {
        /* Если переменная с именем токена уже присутствует
         в таблице переменных, то удалить её (её значение будет обновлено) */
            if (varTable.containsKey(where.getValue())) {
                varTable.remove(where.getValue());
            /* Иначе создать новую переменную в
            таблице переменных с нулевым значение */
            } else {
                declare(where);
            }
        /* Поместить в таблицу переменных новую переменную */
            varTable.put(where.getValue(), Integer.valueOf(what.getValue()));
            /* Иначе для переменной структуры */
        } else if (where.getName().equals("STRUCT_TOKEN")) {
            if (structTable.get(where.getStructName()).containsKey(where.getValue())) {
                structTable.get(where.getStructName()).remove(where.getValue());
            }
            structTable.get(where.getStructName()).put(where.getValue(), Integer.valueOf(what.getValue()));
        }
    }

    public boolean conditionIsTrue() {
        currentTokenNumber = 0;
        step();
        currentTokenNumber++;
        step();
        Token secondToken = tokenStack.pop();
        Token firstToken = tokenStack.pop();
        currentTokenNumber++;
        String operation = postfixToken.get(currentTokenNumber).getValue();
        int a = getTokenValue(firstToken);
        int b = getTokenValue(secondToken);
        switch (operation) {
            case "<":
                if (a < b) {
                    return true;
                }
                break;
            case ">":
                if (a > b) {
                    return true;
                }
                break;
            case "<=":
                if (a <= b) {
                    return true;
                }
                break;
            case ">=":
                if (a >= b) {
                    return true;
                }
                break;
            case "==":
                if (a == b) {
                    return true;
                }
                break;
            case "!=":
                if (a != b) {
                    return true;
                }
                break;
        }
        return false;
    }

    /* Получить значение токена */
    private int getTokenValue(Token token) {
        /* Если токен со значением */
        if (token.getName().equals("VAR_NAME")) {
            /* Если в таблице переменных уже существует переменная
             * с именем токена, то вернуть её значение */
            if (varTable.containsKey(token.getValue())) {
                return varTable.get(token.getValue());
                /* Иначе объявить токен с нулевым значением
                 * и вернуть его значение (нулевое) */
            } else {
                declare(token);
                return varTable.get(token.getValue());
            }
        }
        /* Если токен - число, то вернуть его значение */
        if (token.getName().equals("DIGIT")) {
            return Integer.valueOf(token.getValue());
        }
        if (token.getName().equals("STRUCT_TOKEN")) {
            return structTable.get(token.getStructName()).get(token.getValue());
        }
        /* При поступлении любого другого токена вернуть -1 */
        return -1;
    }

    public void setVarTable(HashMap<String, Integer> varTable) {
        this.varTable = varTable;
    }

    public void setStructTable(HashMap<String, HashMap<String, Integer>> structTable) {
        this.structTable = structTable;
    }
}
