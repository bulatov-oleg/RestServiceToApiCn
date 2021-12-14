package ru.dmv.lk.dto;

import org.springframework.stereotype.Component;


public class MyAppConst {

    //инициализация из proprerties файла в ApplicationStatup.class
    //theamlife прощает запись без get вида MyAppConst.nameApp

    public static String nameApp;
    public static String yearDeveloping;

    public void setNameApp(String nameApp) {
        MyAppConst.nameApp = nameApp;
    }

    public void setYearDeveloping(String yearDeveloping) {
        MyAppConst.yearDeveloping = yearDeveloping;
    }

    public String getNameApp() {
        return nameApp;
    }

    public static String getYearDeveloping() {
        return yearDeveloping;
    }
}
