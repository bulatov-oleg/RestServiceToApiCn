package ru.dmv.lk.utils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SimpleEmailValidator {
    private Pattern pattern;
    private Matcher matcher;

    private static final String EMAIL_PATTERN ="/^((([0-9A-Za-z]{1}[-0-9A-z\\.]{1,}[0-9A-Za-z]{1})|([0-9А-Яа-я]{1}[-0-9А-я\\.]{1,}[0-9А-Яа-я]{1}))@([-A-Za-z]{1,}\\.){1,2}[-A-Za-z]{2,})$/u"; // см https://habr.com/post/175375/ :-)))
/*
"/
        ^( - параметр что маска начинается с начала текста
            (
        (  - этот блок отвечает за логин латиницей
                    [0-9A-Za-z]{1} - 1й символ только цифра или буква
            [-0-9A-z\.]{1,} - в середине минимум один символ (буква, цифра, _, -, .) (не менее 1 символа)
            [0-9A-Za-z]{1} - последний символ только цифра или буква
        )
                | - параметр "или/или" выбирает блок "латиница" или "кирилица"
            (  - этот блок отвечает за логин кирилицей
            [0-9А-Яа-я]{1} - 1й символ только цифра или буква
            [-0-9А-я\.]{1,} - в середине минимум один символ (буква, цифра, _, -, .) (не менее 1 символа)
            [0-9А-Яа-я]{1} - последний символ только цифра или буква
        )
                )
    @ - обазятельное наличие значка разделяющего логин от домена
            (
        [-0-9A-Za-z]{1,} - блок может состоять из "-", цифр и букв (не менее 1 символа)
        \. - наличие точки в конце блока
    ){1,2} - допускается от 1 до 2 блоков по вышеукащанной маске (mail. , ru.mail.)
            [-A-Za-z]{2,} - блок описывайющий домен вехнего уровня (ru, com, net, aero etc) (не менее 2 символов)
            )$ - параметр что маска заканчивается в конце текста
/u - параметр позволяющий работать с кирилицей
"
 */
public SimpleEmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    public boolean validate(final String hex) {
        matcher = pattern.matcher(hex);
        return matcher.matches();
    }

    public boolean simpleValidate(final String hex) {
        //без регулярных выражений Проверяем наличие @ и .
        int index1=hex.indexOf("@");
        int index2=hex.indexOf(".");
        if((index1 != -1)&(index2 != -1)) {
            return true;
        }else{
            return false;
        }
    }

}

