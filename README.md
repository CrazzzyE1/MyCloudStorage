# MyCloudStorage

GitHub:
https://github.com/CrazzzyE1/MyCloudStorage
Youtube:
https://www.youtube.com/watch?v=4em30lb9m84
Yandex.Disk video:
https://disk.yandex.ru/i/FoPXZwGcOfMP8A


Проект состоит из двух модулей:
	- MyClient
	- MyServer

Оба модуля запускают из класса Main соответственно.
Модуль клиента использует JavaFX.
Модуль сервера использует Netty.
Модули написаны на Java 8 (java version "1.8.0_281") 
https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html

------------------------------------------------------------------------------------------------------------------------------
!!! Как запустить:
java -jar MyServer-1.0-SNAPSHOT-jar-with-dependencies.jar
java -jar MyClient-1.0-SNAPSHOT-jar-with-dependencies.jar
Оба файла jar находятся в данном репозитории. 
(Ветка Master)
https://github.com/CrazzzyE1/MyCloudStorage

Параметры для настройки портов Сервера и БД находятся в классе Server
Параметры для настройки портов Клиента находятся в классе Client

Используется MySQL. (Название в моем случае clouddb)
В БД необходимо создать таблицу - users.


CREATE TABLE `users` (`id` int NOT NULL AUTO_INCREMENT,
  `login` varchar(45) NOT NULL, `password` varchar(45) NOT NULL,
  `nickname` varchar(45) NOT NULL, `folderpath` varchar(45) NOT NULL, `space` int NOT NULL,
  PRIMARY KEY (`id`), UNIQUE KEY `id_UNIQUE` (`id`), UNIQUE KEY `login_UNIQUE` (`login`),
  UNIQUE KEY `folderpath_UNIQUE` (`folderpath`)) ENGINE=InnoDB AUTO_INCREMENT=39 
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ciusers

Добавление 2 users:

INSERT INTO `employees`.`users` (`login`, `password`, `nickname`, `folderpath`, `space`) VALUES ('log', 'pass', 'nick', 'log', '15');
INSERT INTO `employees`.`users` (`login`, `password`, `nickname`, `folderpath`, `space`) VALUES ('login', 'password', 'n', 'login', '1');

Никаких дополнительных манипуляций не требуется.

------------------------------------------------------------------------------------------------------------------------------

!!! Что умеет:
Программа состоит из серверной и клиентской части. 
Клиентская часть выполнена в виде графической оболочки.
Обращение к серверу происходит через GUI.

Для каждого пользователя создается своя директория. (доступ только к ней).
Пространство доступное указывается в БД (15 GiB по - умолчанию)
У user'a login - password свободен 1 GiB для примера остановки функции копирования, загрузки при превышении лимита.

Окно авторизации:
	- Проверка на пустой логин, пароль
	- Проверка на правильность логина и пароля

Окно регистрации:
	- Проверка на пустой логин, пароль
	- Проверка на наличие существующего логина (предотвращает дублирование логинов)
	- Регистрация нового пользователя
	
Основное окно:
	- Отображение файлов на сервере в облачном хранилище
	- Отображение файлов на ПК пользователя
	- Переход по дереву папок на ПК и в Облаке
	- Копирование, Перемещение, Удаление файлов в облаке
	- Создание новых директорий в Облаке
	- Сортировка списка файлов в Облаке
	- Скачивание файлов из Облака на ПК
	- Загрузка файлов с ПК в Облако
	- Поиск файлов на сервере в папке пользователя
	- Корзина для хранения удаленных файлов
	- Статус бар с указанием занятого / доступного пространства в Облаке
	- Смена пароля
	- Удаление аккаунта
	- Очистка корзины
	- Восстановление файлов из корзины

Окно смены пароля: 
	- Проверяет на пустые поля.
	- Отправляет на проверку серверу введенные данные.

Окно удаления аккаунта:
	- Проверяет на пустые поля.
	- Отправляет на проверку серверу введенные данные.
Закрывает окно приложения![image](https://user-images.githubusercontent.com/50019250/112650120-20998700-8e5c-11eb-8536-ba9f2540e3f2.png)
