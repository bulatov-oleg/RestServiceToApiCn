package ru.dmv.lk.utils;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Component
@Scope("prototype") //при каждом new будет создан новый экземпляр класса
public class MyDate {

    public MyDate() {
    }

    public synchronized String dtocShort(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        return localDate.format(formatter);
    }

    public synchronized String dtocLong(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return localDate.format(formatter);
    }

    public synchronized String dtocShort(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        return dateFormat.format(date);
    }

    public synchronized String dtocLong(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(date);
    }

    public synchronized LocalDate ctodShort(String stringDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        return LocalDate.parse(stringDate, formatter);
    }

    public synchronized LocalDate ctodLong(String stringDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.parse(stringDate, formatter);
    }

    public synchronized LocalDate today() {
        return LocalDate.now();
    }

    public synchronized Date nowDate() {
        return new Date(System.currentTimeMillis());
    }

    //Последний день предыдущего месяца от текущей даты
    public synchronized Date getPreviousMonthLastDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);

        int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, max);

        return calendar.getTime();

    }

    //первый день месяца
    public synchronized Date getFirstDayMonth(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public synchronized Date StringYyyyMmDdToDate(int year, int month, int day) throws Exception {
        if (!(1 <= month || month <= 12)) throw new Exception("Неверные данные месяца в StringYyyyMmDdToDate()");
        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    public synchronized Date DateMinusMonthes(Date date, int monthes) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -3);
        return c.getTime();
    }
}