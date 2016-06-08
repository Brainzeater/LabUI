import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Парсер - проводит синтаксический анализ, формирует список постфиксных токенов.
 */
public class Parser {
    /* Приоритеты операций */
    private final int BRACKET_PR = 5;
    private final int MULT_DIV_PR = 4;
    private final int ADD_SUB_PR = 3;
    private final int VAR_PR = 2;
    private final int ASSIGN_OP_PR = 1;
    //private final int NO_PR = -1;
    /* Список инфиксных токенов (входная последовательность парсера) */
    private List<Token> tokens;
    /* Список постфиксных токенов (выходная последовательность парсера)*/
    private List<Token> postfixTokenList;
    private Token currentToken; //текущий токен
    /* Номер текущего токена. Инкрементируется при соответствии ожидаемого токена
     * в проверяемом выражении текущему и декрементируетсяв обратном случае
     * - таким образом производится шаг назад по списку токена до посленего
     * верного токена. */
    private int currentTokenNumber = 0;
    private int closeBracketCounter;    //счётчик скобок
    private Stack<Token> operators;     //стек с операторами

    /* Инициализация списков инфиксиных и постфиксных токенов,
     а также стека операторов в конструкторе Парсера */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        postfixTokenList = new ArrayList<Token>();
        operators = new Stack<Token>();
    }

    /* Обработка входной последовательности */
    public void lang() throws Exception {
        boolean exist = false;
        /* Повторять до тех пор, пока не будет найдена ошибка
         * в выражении или пока не будет достигнут конец списка токенов */
        while (currentTokenNumber < tokens.size() && expr()) {
            exist = true;
        }
        /* Отобразить сообщение об ошибке при чтении списка токенов */
        if (!exist) {
            throw new Exception("error in lang " + currentTokenNumber);
        }
        /* Отобразить сообщение об успешном прочтении списка токенов */
        if (currentTokenNumber == tokens.size()) {
            System.out.println("Success");
        }
    }

    /* Проверка корректности выражения */
    private boolean expr() throws Exception {
        /* Проверка корректности выражения объявления переменной или
        * присваивания переменной значения */
        if (declare() || assign()) {
            return true;
            /* Иначе отобразить сообщение об ошибке */
        } else {
            throw new Exception("declare or assign expected, but "
                    + currentToken + "found. " + currentTokenNumber);
        }
    }

    /* Проверка корректности объявления переменной */
    private boolean declare() throws Exception {
        /* Производится последовательная проверка последовательности
         * выражения объявления: сначала идёт ключевое слово var, затем пробел,
         * за которым следует имя переменной. Строка заканчивается точкой с запятой.
         * Если какой-либо из элементов последовательности был пропущен, то
         * метод возвращает false - проверяемое выражение не соответствует
         * синтаксису declare*/
        if (varKw()) {
            /* Добавить текущий токен в выходной список
             с соответствующим приоритетом  */
            currentToken.setPriority(VAR_PR);
            postfixTokenList.add(currentToken);
            if (ws()) {
                if (!varName()) {
                    currentTokenNumber--;
                    return false;
                } else {
                    /* Добавить имя объявленной переменной в выходной список */
                    postfixTokenList.add(currentToken);
                }
            } else {
                currentTokenNumber--;
                return false;
            }
        } else {
            currentTokenNumber--;
            return false;
        }
        if (!sm()) {
            currentTokenNumber--;
            return false;
        } else {
            return true;
        }
    }

    /* Проверка корректности присваивания переменной значения */
    private boolean assign() throws Exception {
        /* Подобно declare, этот метод выдаёт ошибку при несоответствии
         * текущего токена ожидаемому. Последовательность присваивания:
         * имя переменной, знак присваивания, выражение. Строка завершается
         * точкой с запятой. Выражение проверяется на корректность применения скобок. */
        if (varName()) {
            /* Если токен является операндом, то добавить его в конец выходного списка */
            //currentToken.setPriority(NO_PR);
            /* Добавить имя переменной в список */
            postfixTokenList.add(currentToken);
            /* Пропустить пробел */
            ignoreWhitespace();
            if (assignOp()) {
                /* Если токен является оператором присваивания, то
                 * добваить его в стек операторов с соответствующим приоритетом. */
                currentToken.setPriority(ASSIGN_OP_PR);
                operators.push(currentToken);
                /* Пропустить пробел */
                ignoreWhitespace();
                if (!stmt()) {
                    throw new Exception("stmt  expected, but "
                            + currentToken + "found. " + currentTokenNumber);
                }
            } else {
                currentTokenNumber--;
                return false;
            }
        }
        if (closeBracketCounter == 0) {
            if (sm()) {
                /* Когда выражение обработано, добавить все оставшиеся
                 операторы в выходную последовательность */
                while (!operators.empty()) {
                    postfixTokenList.add(operators.pop());
                }
                return true;
            } else {
                currentTokenNumber--;
                return false;
            }
            /* В случае неверного количества скобок
             отобразить сообщение об ошибке */
        } else {
            throw new Exception("illegal number of brackets");
        }
    }

    /* Выражение (высказывание) - используется при присваивании переменной
     * нового значения. */
    private boolean stmt() throws Exception {
        /* Обнуление счётчика скобок при каждом новом выражении. */
        closeBracketCounter = 0;
        /* Проверка на скобки - выражение может начинаться с
         * бесконечного количества скобок. */
        /* Пропустить пробел */
        ignoreWhitespace();
        checkBrackets();
        /* Пропустить пробел */
        ignoreWhitespace();
        /* Независимо от количества скобок, выражение должно начинаться
         * с имени другой переменной или числа (или должно продолжаться ими
         * после оператора) */
        if (stmtUnit()) {
            /* Если токен является операндом, то добавить его в конец
             * выходной последовательности */
            postfixTokenList.add(currentToken);
            /* Пропустить пробел */
            ignoreWhitespace();
            /* Очередная проверка скобок */
            checkBrackets();
            /* Пропустить пробел */
            ignoreWhitespace();
            /* Логическая переменная продолжения цикла.
             * Цикл прекращается, если оператор из списка токенов не был
             * найден или если за ним не следовало имя другой переменной или число. */
            boolean goOn = true;
            while (goOn) {
                if (assignAdd()) {
                    /* Если токен является оператором, поместить его в
                     * стек операторов, но перед этим вытолкнуть любой
                     * из операторов, уже находящихся в стеке, если он
                     * имеет больший или равный приоритет, и добавить его
                     * в результирующий список */
                    currentToken.setPriority(ADD_SUB_PR);
                    checkPriority(ADD_SUB_PR);
                    operators.push(currentToken);
                    /* Проверка на скобки */
                    /* Пропустить пробел */
                    ignoreWhitespace();
                    checkBrackets();
                    /* Пропустить пробел */
                    ignoreWhitespace();
                    /* Если следующий токен не операнд, то отобразить
                     сообщение об ошибке. Иначе добавить токен в выходную
                     последовательность */
                    if (!stmtUnit()) {
                        throw new Exception("stmt_unit  expected, but "
                                + currentToken + "found. " + currentTokenNumber);
                    } else {
                        postfixTokenList.add(currentToken);
                    }
                } else {
                    currentTokenNumber--;
                    /* Аналогично предыдущему блоку */
                    if (assignSub()) {
                        currentToken.setPriority(ADD_SUB_PR);
                        checkPriority(ADD_SUB_PR);
                        operators.push(currentToken);
                        /* Пропустить пробел */
                        ignoreWhitespace();
                        /* Пропустить пробел */
                        ignoreWhitespace();
                        checkBrackets();
                        /* Пропустить пробел */
                        ignoreWhitespace();
                        if (!stmtUnit()) {
                            throw new Exception("stmt_unit  expected, but "
                                    + currentToken + "found. " + currentTokenNumber);
                        } else {
                            postfixTokenList.add(currentToken);
                        }
                    } else {
                        currentTokenNumber--;
                        if (assignMult()) {
                            currentToken.setPriority(MULT_DIV_PR);
                            checkPriority(MULT_DIV_PR);
                            operators.push(currentToken);
                            /* Пропустить пробел */
                            ignoreWhitespace();
                            checkBrackets();
                            /* Пропустить пробел */
                            ignoreWhitespace();
                            if (!stmtUnit()) {
                                throw new Exception("stmt_unit  expected, but "
                                        + currentToken + "found. " + currentTokenNumber);
                            } else {
                                postfixTokenList.add(currentToken);
                            }
                        } else {
                            currentTokenNumber--;
                            if (assignDiv()) {
                                currentToken.setPriority(MULT_DIV_PR);
                                checkPriority(MULT_DIV_PR);
                                operators.push(currentToken);
                                /* Пропустить пробел */
                                ignoreWhitespace();
                                checkBrackets();
                                /* Пропустить пробел */
                                ignoreWhitespace();
                                if (!stmtUnit()) {
                                    throw new Exception("stmt_unit  expected, but "
                                            + currentToken + "found. " + currentTokenNumber);
                                } else {
                                    postfixTokenList.add(currentToken);
                                }
                            } else {
                                /* В случае, если не было найдено ни одного
                                 * оператора, то закончить цикл */
                                currentTokenNumber--;
                                goOn = false;
                            }
                        }
                    }
                }
                /* Пропустить пробел */
                ignoreWhitespace();
                /* Проверить скобки */
                checkBrackets();
                /* Пропустить пробел */
                ignoreWhitespace();
            }
            return true;
        } else {
            /* Если выражение началось не с имени переменной или числа, то
             * отобразить сообщение об ошибке. */
            throw new Exception("stmt_unit  expected, but "
                    + currentToken + "found. " + currentTokenNumber);
        }
    }

    /* Проверка на наличие имени переменной или числа
     * (используется при присваивании переменной нового значения) */
    private boolean stmtUnit() throws Exception {
        boolean badTry = true;
        if (!digit()) {
            currentTokenNumber--;
            badTry = false;
            if (varName() && !badTry) {
                badTry = true;
            } else {
                currentTokenNumber--;
                badTry = false;
            }
        }
        return badTry;
    }

    /* Проверка бесконечного количества скобок.
     * Переменная closeBracketCounter хранит количество введённых
     * левых скобок (эту величину можно воспринимать и как количество
     * ожидаемых правых скобок). Переменная инкрементируется при
     * поступлении левой скобки и декрементируется при поступлении правой.*/
    private void checkBrackets() {
        while (bracketOpen()) {
            closeBracketCounter++;
            /* Если токен является левой скобкой, то
             * добавить его в стек операторов */
            currentToken.setPriority(BRACKET_PR);
            operators.push(currentToken);
            /* Пропустить пробел */
            ignoreWhitespace();
        }
        currentTokenNumber--;
        while (bracketClose()) {
            closeBracketCounter--;
            /* Если токен является правой скобкой, то выталкивать
             * элементы из стека операторов, пока не будет найдена
             * соответствующая левая скобка. Каждый оператор добавлять
             * в конец списка выходной последовательности */
            while (!operators.peek().getName().equals("BRACKET_OPEN")) {
                postfixTokenList.add(operators.pop());
            }
            /* Удаление левой скобки из стека операторов */
            operators.pop();
            /* Пропустить пробел */
            ignoreWhitespace();
        }
        currentTokenNumber--;
    }

    /* Проверка приоритетов при добавлении нового оператора */
    private void checkPriority(int priority) {
        /* Повторять, до первой скобки, пока стек операторов не пуст и приоритет
         * предыдущего оператора больше или равен приоритету текущего */
        while (operators.peek().getPriority() != BRACKET_PR && !operators.empty() &&
                (operators.peek().getPriority() >= priority)) {
            /* Если приоритет последнего оператора из стека больше или равен
             * приоритету текущего оператора, то извлечь его из стека и поместить
              * в выходную последовательность */
            if (operators.peek().getPriority() >= priority) {
                postfixTokenList.add(operators.pop());
            }
        }
    }

    /* Ниже приведены методы для проверки соответствия текущего
     * токена следующему из списка токенов. */
    private boolean sm() {
        match();
        return currentToken.getName().equals("SM");
    }

    private boolean varKw() {
        match();
        return currentToken.getName().equals("VAR_KW");
    }

    private boolean assignOp() {
        match();
        return currentToken.getName().equals("ASSIGN_OP");
    }

    private boolean assignAdd() {
        match();
        return currentToken.getName().equals("ASSIGN_ADD");
    }

    private boolean assignSub() {
        match();
        return currentToken.getName().equals("ASSIGN_SUB");
    }

    private boolean assignMult() {
        match();
        return currentToken.getName().equals("ASSIGN_MULT");
    }

    private boolean assignDiv() {
        match();
        return currentToken.getName().equals("ASSIGN_DIV");
    }

    private boolean digit() {
        match();
        return currentToken.getName().equals("DIGIT");
    }

    private boolean varName() {
        match();
        return currentToken.getName().equals("VAR_NAME");
    }

    private boolean ws() {
        match();
        return currentToken.getName().equals("WS");
    }

    private boolean bracketOpen() {
        match();
        return currentToken.getName().equals("BRACKET_OPEN");
    }

    private boolean bracketClose() {
        match();
        return currentToken.getName().equals("BRACKET_CLOSE");
    }

    /* Пропустить пробел */
    private void ignoreWhitespace() {
        do {
            match();
        } while (currentToken.getName().equals("WS"));
        currentTokenNumber--;
    }

    /* Для проведения проверки на соответствие присваиваем
     * текущему токену значение следующего токена из списка токенов */
    private boolean match() {
        /* Присвоение допустимо только в случае, если ещё не достигнут
         * конец списка токенов */
        if (currentTokenNumber < tokens.size()) {
            currentToken = tokens.get(currentTokenNumber);
            currentTokenNumber++;
            return true;
        } else {
            return false;
        }
    }

    /* Возвращает выходную последовательность парсера -
     * список постфиксных токенов */
    public List<Token> getPostfixToken() {
        return postfixTokenList;
    }
}
