# cnTrustStore.p12 создан keytool -import -file ./CAsign.pem  -keystore cnTrustStore.p12
# CAsign.pem  получен из ЦН как корневой доверенный сертификат
# trustStorePassword = IJN456_ijn456
#
#Берем из ЦН сгенеренный сертификат priv_key.pfx и sign.cer
#Преобразовать sign.cer в sign.crt (открытый ключ)
#   Запускаем kse. Examinate a certificat. Кнопка PEM. Copy. Создать и вставить в файл sign.crt
#Получили из ЦН корневой сертификат CAsing.pem
#Складываем сертификаты в privateForProject/keystore/sourceWork
#Формируем новую пару открытый и закрытый ключ в одном файле
#
#   Преобразовать pfx в pem:
echo openssl pkcs12 -in prv_key.pfx -nocerts -out prv_key.pem -nodes
#   keyStorePassword  IJN456_ijn456 для prv_key.pfx использован для генерации сертификата в ЦН ранее при запросе сертификата
#
openssl pkcs12 -in prv_key.pfx -nocerts -out prv_key.pem -nodes
#
#   Получить пару ключей в одном файле:
#   keyPairPassword IJN123, устанавливаем для доступа к pairKeysCnWork.pfx
echo  openssl pkcs12 -export -out pairKeysCnWork.pfx -inkey prv_key.pem -in sign.crt -certfile CAsign.pem
#pairKeysCnWork.pfx - файл с парой ключей для ЦН боевой. Меняем при новой генерации
#
openssl pkcs12 -export -out pairKeysCnWork.pfx -inkey prv_key.pem -in sign.crt -certfile CAsign.pem
#
#   kse       Программа для работы с разными типами хранилищ, сертификатами и тд https://keystore-explorer.org/downloads.html
#   Создаем НОВЫЙ файл keyStore - в нашем случае cnKeyStoreWork.pfx. Устанавливаем для него пароль доступа password IJN456_ijn456
#   Для каждой пары создаем новый keystore, потому что какие-то проблемы с дополнительной парой в keystore в сервисе
#   Импортирум пару pairKeysCnWork.pfx. Устанавливаем  Decryption password ijn123, который далее используется в сервисе
#   Сохраняем cnKeyStoreWork.pfx в privateForProject/keystore/sourceWork
#
kse
   
