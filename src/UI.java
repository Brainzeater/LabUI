import java.util.HashMap;
import java.util.List;

/**
 * Основной класс программы
 */

public class UI {
    /* Создание экземпляра класса FileHelper для чтения файла */
    static FileHelper fileHelper = new FileHelper();

    public static void main(String[] args) throws Exception {
        //wrongInput();
        validInput();
    }

    /* Метод для теста файла с ошибками */
    static void wrongInput() throws Exception {
        /* Считать файл с ошибками */
        fileHelper.testRead("wrong-test.input");
        /* Провести тест */
        process("wrong-test.input");
    }

    /* Метод для теста файла без ошибок */
    static void validInput() throws Exception {
        /* Считать файл без ошибок */
        fileHelper.testRead("valid-test.input");
        /* Провести тест */
        process("struct-test.input");
//        process("cycle-test.input");
//        process("valid-test.input");
    }

    /* Процедура теста */
    static void process(String fileName) throws Exception {
        /* Создать экземпляр лексера */
        Lexer lexer = new Lexer();
        /* Преобразовать входной поток символов в список токенов */
        lexer.processInput(fileName);
        List<Token> tokens = lexer.getTokens();

        HashMap<String, Integer> myVarTable = new HashMap<String, Integer>();
        HashMap<String, HashMap<String, Integer>> structTable = new HashMap<String, HashMap<String, Integer>>();
        /* Создать экземпляр парсера с имеющимся списком токенов */
        Parser parser = new Parser(tokens, myVarTable, structTable);
        /* Провести разбор списка токенов и сформировать выходную
         * последовательность в постфиксной записи */
        parser.lang();
        /* Получить список постфиксных токенов */
        List<Token> postfixToken = parser.getPostfixToken();
        /* Отобразить список постфиксных токенов */
        for (Token val : postfixToken) {
            System.out.print(val.getValue());
        }
        /* Отобразить таблицу переменных */
        System.out.println(myVarTable);
        System.out.println(structTable);
    }
}

