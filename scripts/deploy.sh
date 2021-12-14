#!/bin/bash
# Это просто заметки. До настоящего скрипта не дошли руки.ыы
mvn clean package

echo 'Coping file to server'
#Запуск ОБЯЗАТЕЛЬНО в catalog /var/www/dmvCnSinkApp там есть стартер
#Login guk Pass 9207451
#scp target/dmvAppSinkCn-1.0-SNAPSHOT.jar guk@10.2.1.9:/var/www/dmvCnSinkApp/

scp target/dmvAppSinkCn-1.0-SNAPSHOT.jar guk@78.85.24.79:/var/www/dmvCnSinkApp/
scp -r  doc/* guk@10.2.1.9:/var/www/dmvCnSinkApp/doc/

scp -r privateForProject/*  guk@78.85.24.79:/var/www/dmvCnSinkApp/privateForProject/
scp properties/*.* guk@78.85.24.79:/var/www/dmvCnSinkApp/properties/

echo 'Restart server app'
#ssh guk@10.2.1.9 << EOF

#нужно остановить текущее приложение. для этого вызвать api для остановки
#если java app на сервере единственное,то можно так
# иначе sudo netstat -natp  найти приложение и sudo kill 9393 например
#su guk-admin pass WSX321rfv456
#sudo netstat -natp | :::8081
#sudo kill 9393

/var/www/dmvCnSinkApp/start_dmvCnSinkApp

#Запускаем приложение
nohup java -jar -Xmx2g /var/www/dmvCnSinkApp/dmvAppSinkCn-1.0-SNAPSHOT.jar > /var/www/dmvCnSinkApp/log.txt &

#Url 78.85.24.79:1726 проброшен на Mikrotik на 10.2.1.9:8081 по dstnat и srcnat
echo 'Ок'

exit 0