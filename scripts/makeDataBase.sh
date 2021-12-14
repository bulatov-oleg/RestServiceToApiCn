# Делаем под guk-admin WSX321rfv456
#username root pass WSX321rfv456
#Для локальной машины root OlegB&123
sudo mysql -u root -p
CREATE USER 'dmv-main'@'localhost' IDENTIFIED BY '-p
';
GRANT ALL PRIVILEGES ON *.* TO 'dmv-main'@'localhost' ;
FLUSH PRIVILEGES;
CREATE DATABASE DmvAppSinkCn;
QUIT;