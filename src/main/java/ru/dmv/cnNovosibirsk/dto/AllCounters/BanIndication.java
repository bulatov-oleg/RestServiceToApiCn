package ru.dmv.cnNovosibirsk.dto.AllCounters;

import lombok.Data;

@Data
public class BanIndication {
    private boolean isBanned; //  Флаг наличия запрета
    private String reason; //  Сообщение о запрете ввода показаний. Заполнено, если действует запрет(isBanned = true)

}
