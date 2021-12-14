package ru.dmv.cnNovosibirsk.dto.BalanceDetail;

import lombok.Data;

@Data
public class Balance {

    private ServiceInfo serviceInfo; //информация по услуге
    private double saldoin;     //    Сальдо на начало месяца, руб
    //ONLINE_BY_EPD=нет - входящее сальдо с основного ФС по услуге
    //ONLINE_BY_EPD=да Debt - Set со строки счета на оплату
    private double setSum;         //Сумма корректировок сальдо по услуге в месяце
    //ONLINE_BY_EPD=нет - сумма оборотов Дт и Кт минус сумма начислений, макетов
    // и оплат из архива оборотов основного ФС
    //ONLINE_BY_EPD=да - Set со строки счета на оплату
    private double charge;      // Начислено, руб
    //ONLINE_BY_EPD=нет - колонка "Начислено" из архива оборотов основного ФС
    //ONLINE_BY_EPD=да - Charge со строки счета на оплату
    private double maket;       //Перерасчеты, руб
    //ONLINE_BY_EPD=нет - колонка "Макеты" из архива оборотов основного ФС
    //ONLINE_BY_EPD=да - RECALC со строки счета на оплату
    private double payment;     //    Оплачено, руб.
    //ONLINE_BY_EPD=нет - колонка "Оплаты" из архива оборотов основного ФС
    //ONLINE_BY_EPD=да
    //PO_CN.INNER.LK_EPD_PAYS_MODE=да - 0
    //PO_CN.INNER.LK_EPD_PAYS_MODE=нет - колонка Prepays со строки счета на оплату
    private double futurePays;  //ONLINE_BY_EPD=нет- платежи, проведенные в месяце после отчетного
    //ONLINE_BY_EPD=да - futurePays со строки счета на оплату
    private double saldoout;    //  Сальдо на конец месяца, руб.
    private double forPaymentPast;  // Сумма к оплате за прошедшие периоды
    //ONLINE_BY_EPD=нет saldoIn+maket+payment+futurePays
    //ONLINE_BY_EPD=да - DEBT со строки счета на оплату

    private double forPayment;  //  Рекомендованная сумма к оплате
    //ONLINE_BY_EPD=нет - поле не заполняется
    //ONLINE_BY_EPD=да - поле For_payment со строки счета на оплату
    private double saldoInFine; //  Сальдо пени на начало месяца, руб
    //ONLINE_BY_EPD=нет - входящее сальдо с пеневого ФС по услуге
    //ONLINE_BY_EPD=нет - поле Debt_fine со строки счета на оплату

    private double fine;        //Начислено пени
    //ONLINE_BY_EPD=нет - сумма оборотов Дт и Кт с пеневого ФС по услуге
    //ONLINE_BY_EPD=да - поле Fine со строки счета на оплату
    private double fineCalc;    //ONLINE_BY_EPD=нет - рассчитанные пени на текущую дату/конец ОМ
    //ONLINE_BY_EPD=да - поле отсутствует
    private double prepaysFine; //ONLINE_BY_EPD=нет - поле отсутствует
    //ONLINE_BY_EPD=да - поле prepays_fine со строки счета на оплату
    private double saldooutFine;    //Сальдо пени на конец месяца, руб. saldoInFine+fine


}
