import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Лексер - проводит лексический анализ и формирует список токенов
 */
public class Lexer {
    List<Token> tokens = new ArrayList<Token>();    // список токенов
    String accum = "";                              // накопитель

    /* Паттерны */
    Pattern semicolPattern = Pattern.compile("^;$");                    //  ;
    Pattern varKeywordPattern = Pattern.compile("^var$");               //  var
    Pattern subOpPattern = Pattern.compile("^\\-$");                    //  -
    Pattern addOperationPattern = Pattern.compile("^\\+$");             //  +
    Pattern assignOperationPattern = Pattern.compile("^=$");            //  =
    Pattern digitPattern = Pattern.compile("^(0)||([1-9]{1}[0-9]*)$");  //  числа
    Pattern variableNamePattern = Pattern.compile("^([a-zA-Z]+)$");     //  названия переменных
    Pattern wsPattern = Pattern.compile("^\\s*$");                      //  пробел
    Pattern multOpPattern = Pattern.compile("^\\*$");                   //  умножение
    Pattern divOpPattern = Pattern.compile("^/$");                      //  деление
    Pattern bracketOpenOpPattern = Pattern.compile("^[(]$");            //  (
    Pattern bracketCloseOpPattern = Pattern.compile("^[)]$");           //  )
    Pattern curlyBracketOpenPattern = Pattern.compile("[{]");           //  {
    Pattern curlyBracketClosePattern = Pattern.compile("[}]");          //  }
    Pattern logicEqualsPattern = Pattern.compile("^==$");               //  ==
    Pattern logicMorePattern = Pattern.compile("^>$");                  //  >
    Pattern logicLessPattern = Pattern.compile("^<$");                  //  <
    Pattern logicNotEqualsPattern = Pattern.compile("^!=$");            //  !=
    Pattern logicMoreOrEqualsPattern = Pattern.compile("^>=$");         //  >=
    Pattern logicLessOrEqualsPattern = Pattern.compile("^<=$");         //  <=
    Pattern whileKeywordPattern = Pattern.compile("^while$");           //  while
    Pattern structKeywordPattern = Pattern.compile("^struct$");         //  struct
    Pattern dotPatten = Pattern.compile("^[.]$");                       //  .

    Map<String, Pattern> keyWords = new HashMap<String, Pattern>();     //  хэшмэп для ключевых слов
    Map<String, Pattern> regularTerminals = new HashMap<String, Pattern>(); // хэшмэп для терминалов

    private String currentLucky = null;     // хранит текущий найденный токен
    private int i;                          // счётчик

    /* В конструкторе Лексера заполняются хэшмэпы для ключевых слов и терминалов */
    public Lexer() {
        keyWords.put("VAR_KW", varKeywordPattern);
        keyWords.put("WHILE_KW", whileKeywordPattern);
        keyWords.put("STRUCT_KW", structKeywordPattern);
        regularTerminals.put("ASSIGN_SUB", subOpPattern);
        regularTerminals.put("SM", semicolPattern);
        regularTerminals.put("ASSIGN_ADD", addOperationPattern);
        regularTerminals.put("ASSIGN_OP", assignOperationPattern);
        regularTerminals.put("DIGIT", digitPattern);
        regularTerminals.put("VAR_NAME", variableNamePattern);
        regularTerminals.put("WS", wsPattern);
        regularTerminals.put("ASSIGN_MULT", multOpPattern);
        regularTerminals.put("ASSIGN_DIV", divOpPattern);
        regularTerminals.put("BRACKET_OPEN", bracketOpenOpPattern);
        regularTerminals.put("BRACKET_CLOSE", bracketCloseOpPattern);
        regularTerminals.put("C_BRACKET_OPEN", curlyBracketOpenPattern);
        regularTerminals.put("C_BRACKET_CLOSE", curlyBracketClosePattern);
        regularTerminals.put("EQUALS", logicEqualsPattern);
        regularTerminals.put("MORE", logicMorePattern);
        regularTerminals.put("LESS", logicLessPattern);
        regularTerminals.put("NOT_EQUALS", logicNotEqualsPattern);
        regularTerminals.put("MORE_EQUALS", logicMoreOrEqualsPattern);
        regularTerminals.put("LESS_EQUALS", logicLessOrEqualsPattern);
        regularTerminals.put("DOT", dotPatten);
    }

    /* Метод для обработки входной последовательности символов
        и преобразования её в список токенов */
    public void processInput(String fileName) throws Exception {
        /* Чтение входного потока символов */
        File file = new File(fileName);
        Reader reader = new FileReader(file);
        BufferedReader breader = new BufferedReader(reader);
        String line;
        /* Обработать входной поток до конца файла по строкам */
        while ((line = breader.readLine()) != null) {
            /* Обработать текущую строку */
            processLine(line);
        }
        System.out.println("TOKEN("
                + currentLucky
                + ") recognized with value : "
                + accum);
        /* Добавить в список токенов */
        tokens.add(new Token(currentLucky, accum));
        /* Отобразить список токенов */
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /* Обработка строки */
    private void processLine(String line) throws Exception {
        /* Последовательно добавлять в накопитель
            по одному символу до конца строки
            и при каждой итерации проверять, найден ли токен */
        for (i = 0; i < line.length(); i++) {
            accum = accum + line.charAt(i);
            processAccum();
            /* Если к конце строки в аккумуляторе больше одного символа,
             * то сообщить об ошибке. */
            if (i == (line.length() - 1) && accum.length() > 1) {
                System.out.println(accum);
                throw new Exception("There is an unknown symbol or sequence of symbols");
            }
        }
    }

    /* Проверка на совпадение текущего значения накопителя
        с каким-либо паттерном */
    public void processAccum() {
        boolean found = false;
        /* Для всех элементов хэшмэпа терминалов провести проверку */
        for (String regExpName : regularTerminals.keySet()) {
            /* Шаблон текущего терминала */
            Pattern currentPattern = regularTerminals.get(regExpName);
            /* Найти совпадения */
            Matcher m = currentPattern.matcher(accum);
            /* Если шаблон текущего терминала совпал со значением накопителя */
            if (m.matches()) {
                /* Запомнить значение терминала */
                currentLucky = regExpName;
                found = true;
            } else {

            }
        }
        /* Данное условие выполняется в том случае, если терминал был обнаружен
            в предыдущей итерации, а при добавлении нового символа совпадений
            с другими треминалами обнаружено не было. */
        if (currentLucky != null && !found) {
            System.out.println("TOKEN("
                    + currentLucky
                    + ") recognized with value : "
                    + accum.substring(0, accum.length() - 1)
            );
            /* Добавить найденный терминал с накопителем без последнего символа
                в качестве значения в список токенов */
            tokens.add(new Token(currentLucky, accum.substring(0, accum.length() - 1)));
            /* Вернуться на один символ назад, обнулить накопитель и значение текущего терминала */
            i--;
            accum = "";
            currentLucky = null;
        }

        /* Аналогичная проверка для хэшмэпа ключевых слов */
        for (String regExpName : keyWords.keySet()) {
            Pattern currentPattern = keyWords.get(regExpName);
            Matcher m = currentPattern.matcher(accum);
            if (m.matches()) {
                currentLucky = regExpName;
                found = true;
            }
        }
        if (currentLucky != null && !found) {
            System.out.println("TOKEN("
                    + currentLucky
                    + ") recognized with value : "
                    + accum.substring(0, accum.length() - 1)
            );

            tokens.add(new Token(currentLucky, accum.substring(0, accum.length() - 1)));
            i--;
            accum = "";
            currentLucky = null;
        }
    }

    /* Получить список токенов */
    public List<Token> getTokens() {
        return tokens;
    }
}
