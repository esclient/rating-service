# rating-service


# Документация Rating Service

## 🎯 Что Делает Этот Сервис

**Rating Service** — это микросервис, который обрабатывает рейтинги модов (модификаций/плагинов). Представьте это как систему "лайк/дизлайк", где пользователи могут оценивать различные моды, а вы можете получать статистику о том, сколько людей лайкнули или дизлайкнули каждый мод.

**Основные Функции:**
- ✅ Пользователи могут оценивать моды (положительные или отрицательные оценки)
- ✅ Получение статистики рейтингов модов (всего, лайков, дизлайков)
- ✅ Построен с gRPC для быстрой коммуникации
- ✅ Использует PostgreSQL базу данных для хранения
- ✅ Докеризован для легкого развертывания

---

## 🏗️ Обзор Архитектуры

Этот сервис следует **3-уровневой архитектуре** (общий паттерн для организации кода):

```
┌─────────────────┐
│   Handler       │  ← Получает gRPC запросы от клиентов
│   (Controller)  │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   Service       │  ← Содержит бизнес-логику и валидацию
│  (Business)     │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │  ← Общается с базой данных
│   (Data)        │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   PostgreSQL    │  ← Хранит фактические данные
│   Database      │
└─────────────────┘
```

**Почему такая структура?**
- **Разделение ответственности**: Каждый слой имеет одну задачу
- **Легко тестировать**: Можно тестировать каждый слой независимо
- **Легко поддерживать**: Изменения в одном слое не ломают другие
- **Отраслевой стандарт**: Большинство профессиональных приложений используют этот паттерн

---

## 📁 Структура Проекта

```
rating-service/
├── src/main/java/com/esclient/ratingservice/
│   ├── RatingServiceApplication.java    # Главная точка входа приложения
│   ├── handler/
│   │   └── Handler.java                 # gRPC обработчик запросов (Контроллер)
│   ├── service/
│   │   └── RatingService.java          # Бизнес-логика
│   ├── repository/
│   │   └── RatingRepository.java       # Операции с базой данных
│   └── model/
│       └── RateModMessage.java         # Сущность базы данных (пока не используется)
├── RATING-SERVICE
│   └── rating.proto                    # Определение gRPC API
├── application.yaml                    # Конфигурация Spring
├── pom.xml                            # Зависимости и конфигурация сборки
├── Dockerfile                         # Как собрать Docker образ
└── Makefile                           # Команды автоматизации сборки
```

---

## 🔍 Детальный Разбор Кода

### 1. **Handler.java** - Входная Дверь 🚪

```java
@GrpcService
public class Handler extends RatingServiceGrpc.RatingServiceImplBase
```

**Что он делает:**
- Это как "стойка регистрации" вашего сервиса
- Получает gRPC запросы от других сервисов или клиентов
- Конвертирует gRPC сообщения в Java объекты и наоборот
- Вызывает бизнес-логику (слой Service)
- Отправляет ответы обратно клиентам

**Ключевые Методы:**

**`rateMod()` - Добавить новый рейтинг:**
```java
public void rateMod(Rating.RateModRequest request, StreamObserver<Rating.RateModResponse> responseObserver)
```
- Получает: mod_id, author_id, rate (положительное/отрицательное число)
- Делает: Вызывает service для сохранения рейтинга в базу данных
- Возвращает: ID созданного рейтинга

**`getRates()` - Получить статистику рейтингов:**
```java  
public void getRates(Rating.GetRatesRequest request, StreamObserver<Rating.GetRatesResponse> responseObserver)
```
- Получает: mod_id
- Делает: **В настоящее время возвращает поддельные данные** (нужна реализация!)
- Должен возвращать: общее количество рейтингов, лайков, дизлайков для этого мода

**Важные Заметки:**
- `@GrpcService` говорит Spring, что это обрабатывает gRPC запросы
- `StreamObserver` — это то, как gRPC отправляет ответы обратно
- Обработка исключений: Если что-то идет не так, отправляет ошибку обратно клиенту

### 2. **RatingService.java** - Мозг 🧠

```java
@Service
public final class RatingService
```

**Что он делает:**
- Содержит бизнес-логику ("правила" вашего приложения)
- Валидирует данные перед сохранением
- Конвертирует исключения в понятные пользователю ошибки
- Действует как мост между Handler и Repository

**Ключевой Метод:**

**`rateMod()` - Обработать рейтинг:**
```java
public int rateMod(long mod_id, long author_id, int rate) {
    try {
        return (int) repository.addRate(mod_id, author_id, rate);
    } catch (SQLException e) {
        throw new RuntimeException("Failed to add rating", e);
    }
}
```

**Что здесь происходит:**
1. Принимает данные рейтинга
2. Вызывает repository для сохранения в базе данных
3. Если операция с базой данных не удается, конвертирует SQL ошибку в общую runtime ошибку
4. Возвращает ID нового рейтинга

**Почему этот слой существует:**
- **Валидация**: Может добавить проверки типа "рейтинг должен быть между -5 и +5"
- **Бизнес-правила**: Может добавить логику типа "пользователи не могут оценивать один мод дважды"
- **Обработка ошибок**: Конвертирует технические ошибки в бизнес-ошибки

### 3. **RatingRepository.java** - Говорящий с Базой Данных 💾

```java
@Repository  
public class RatingRepository
```

**Что он делает:**
- Единственная работа — общаться с базой данных
- Содержит все SQL запросы
- Правильно обрабатывает соединения с базой данных
- Возвращает сырые данные (никакой бизнес-логики здесь нет)

**Ключевые Методы:**

**`addRate()` - Вставить новый рейтинг:**
```java
public long addRate(long modId, long authorId, int rate) throws SQLException
```
**SQL:**
```sql
INSERT INTO rates (author_id, mod_id, rate) VALUES (?, ?, ?)
```
**Что происходит:**
1. Открывает соединение с базой данных из пула соединений
2. Подготавливает SQL statement с параметрами (предотвращает SQL инъекции!)
3. Выполняет вставку
4. Получает автоматически сгенерированный ID новой строки
5. Возвращает этот ID
6. Автоматически закрывает соединение (try-with-resources)

**`getTotalRates()` - Подсчитать рейтинги для мода:**
```java
public long getTotalRates(long modId) throws SQLException
```
**SQL:**
```sql
SELECT COUNT(*) FROM rates WHERE mod_id = ?
```

**Важные Технические Детали:**
- Использует `DataSource` вместо сырого `Connection` (лучше для пула соединений)
- `try-with-resources` автоматически закрывает соединения с базой данных
- `PreparedStatement` предотвращает атаки SQL инъекций
- `RETURN_GENERATED_KEYS` получает автогенерированный ID из базы данных

### 4. **RatingServiceApplication.java** - Стартер 🚀

```java
@SpringBootApplication
public class RatingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RatingServiceApplication.class, args);
    }
}
```

**Что он делает:**
- Это точка входа — где приложение запускается
- `@SpringBootApplication` говорит Spring:
  - Сканировать компоненты (`@Service`, `@Repository`, `@GrpcService`)
  - Настроить соединения с базой данных
  - Запустить gRPC сервер
  - Настроить dependency injection

### 5. **rating.proto** - Контракт 📋

```protobuf
service RatingService {
    rpc RateMod(RateModRequest) returns (RateModResponse);
    rpc GetRates(GetRatesRequest) returns (GetRatesResponse);  
}
```

**Что это такое:**
- Определяет контракт API (как выглядят запросы/ответы)
- Работает как интерфейс, с которым соглашаются и клиент, и сервер
- Автоматически компилируется в Java классы

**Типы Сообщений:**
- `RateModRequest`: mod_id, author_id, rate
- `RateModResponse`: rate_id (ID созданного рейтинга)
- `GetRatesRequest`: mod_id  
- `GetRatesResponse`: rates_total, likes, dislikes

---

## ⚙️ Конфигурационные Файлы

### **application.yaml** - Настройки Сервиса

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}           # Получает из переменной окружения
    driver-class-name: org.postgresql.Driver
  jpa:
    show-sql: true                 # Логирует все SQL запросы (полезно для отладки)
    hibernate:
      ddl-auto: none              # Не создавать таблицы автоматически
grpc:
  server:
    port: ${PORT}                 # Порт gRPC сервера из окружения
```

**Ключевые Моменты:**
- Использует переменные окружения (`${DATABASE_URL}`, `${PORT}`) для конфигурации
- `show-sql: true` означает, что вы увидите все запросы к базе данных в логах
- `ddl-auto: none` означает, что нужно создавать таблицы базы данных вручную

### **pom.xml** - Зависимости и Сборка

**Основные Зависимости:**
- **Spring Boot 3.2.0**: Главный фреймворк
- **PostgreSQL**: Драйвер базы данных  
- **gRPC 1.58.0**: Для коммуникации API
- **Spring Data JPA**: Для операций с базой данных
- **net.devh grpc-server-spring-boot-starter**: Соединяет Spring Boot + gRPC

---

## 🚀 Как Запустить Этот Сервис

### **Предварительные Требования:**
1. Установленная Java 17
2. Установленный Docker  
3. Доступная база данных PostgreSQL

### **Необходимые Переменные Окружения:**
(.env)
```
DATABASE_URL=jdbc:postgresql://localhost:5432/ratings_db?user=username&password=password
PORT=7777
```

### **Вариант 1: Использование Make (Рекомендуется)**
```bash
# Собрать и запустить с Docker
make run
```

### **Вариант 2: Вручную через Docker**
```bash
# Собрать Docker образ
docker build -t rating-service .

# Запустить контейнер
docker run --rm -it \
  -e DATABASE_URL="your_db_url" \
  -e PORT=7777 \
  -p 7777:7777 \
  rating-service
```

### **Вариант 3: Прямой Java (для разработки)**
```bash
# Собрать проект
mvn clean package -DskipTests

# Запустить JAR
java -jar target/rating-service-0.0.1-SNAPSHOT.jar
```

---

## 🗄️ Схема Базы Данных

**Таблица: `rates`**
```sql
CREATE TABLE rates (
    id SERIAL PRIMARY KEY,          -- Автоинкрементный ID
    author_id BIGINT NOT NULL,      -- Кто поставил рейтинг
    mod_id BIGINT NOT NULL,         -- Какой мод оценивался  
    rate INTEGER NOT NULL,          -- Значение рейтинга (+1, -1, и т.д.)
);

-- Индексы для производительности
CREATE INDEX idx_rates_mod_id ON rates(mod_id);
CREATE INDEX idx_rates_author_id ON rates(author_id);
```

---

## 🔌 Примеры Использования API

### **Оценить Мод (RateMod)**

**Запрос:**
```protobuf
RateModRequest {
    mod_id: 12345
    author_id: 67890  
    rate: 1           // Положительный рейтинг (лайк)
}
```

**Ответ:**
```protobuf
RateModResponse {
    rate_id: 98765    // ID созданной записи рейтинга
}
```

### **Получить Статистику Мода (GetRates)**

**Запрос:**
```protobuf
GetRatesRequest {
    mod_id: 12345
}
```

**Ответ:**
```protobuf
GetRatesResponse {
    rates_total: 150  // Общее количество рейтингов
    likes: 120        // Положительные рейтинги  
    dislikes: 30      // Отрицательные рейтинги
}
```

---

## 🛠️ Рабочий Процесс Разработки

### **Добавление Новой Функции:**

1. **Обновить `rating.proto`** - Определить новый RPC метод и сообщения
2. **Перегенерировать Java классы** - Запустить `make update` 
3. **Добавить метод Repository** - Написать SQL запросы
4. **Добавить метод Service** - Добавить бизнес-логику
5. **Добавить метод Handler** - Обработать gRPC запросы
6. **Тестировать** - Убедиться, что все работает

### **Пример: Добавление функции "Удалить Рейтинг":**

1. **Proto:**
```protobuf
rpc DeleteRating(DeleteRatingRequest) returns (DeleteRatingResponse);
```

2. **Repository:**
```java
public boolean deleteRating(long rateId, long authorId) throws SQLException {
    String sql = "DELETE FROM rates WHERE id = ? AND author_id = ?";
    // Реализация
}
```

3. **Service:**
```java
public boolean deleteRating(long rateId, long authorId) {
    // Валидация и обработка ошибок
    return repository.deleteRating(rateId, authorId);
}
```

4. **Handler:**
```java
@Override
public void deleteRating(Rating.DeleteRatingRequest request, 
                        StreamObserver<Rating.DeleteRatingResponse> responseObserver) {
    // Обработка запроса/ответа
}
```

---

## 🚨 Текущие Проблемы и TODOs

### **Проблемы для Исправления:**

1. **`getRates()` возвращает поддельные данные**
   - В настоящее время хардкод: `totalRates = 100L; likes = 75L; dislikes = 25L`
   - **Исправить:** Реализовать фактические запросы к базе данных используя метод `getTotalRates()`

2. **Отсутствуют запросы к базе данных для статистики**
   - Нужны методы в Repository: `getLikes()`, `getDislikes()`
   - Нужно вызывать их из слоя Service

3. **Нет обработки ошибок для невалидных рейтингов**
   - Должен валидировать: значения рейтингов, дублирующиеся рейтинги и т.д.

4. **RateModMessage.java не используется**
   - Эта JPA сущность существует, но код использует сырой SQL вместо этого
   - Нужно решить: Использовать JPA сущности или удалить этот файл

### **Предлагаемые Улучшения:**

1. **Добавить валидацию ввода**
   - Валидировать диапазоны рейтингов (например, -5 до +5)
   - Предотвращать оценку пользователями одного мода несколько раз

2. **Добавить правильное логирование**
   - Логировать важные события (новые рейтинги, ошибки)
   - Использовать SLF4J вместо System.out.println

3. **Добавить проверки работоспособности**
   - Добавить эндпоинт для проверки работы сервиса
   - Проверять подключение к базе данных

4. **Добавить метрики**
   - Отслеживать количество рейтингов в минуту
   - Мониторить время ответа

---

## 🧪 Стратегия Тестирования

### **Необходимые Unit Тесты:**

**Тесты Repository:**
```java
@Test
void addRate_ShouldReturnRateId() {
    // Тест вставки в базу данных
}

@Test  
void getTotalRates_ShouldReturnCorrectCount() {
    // Тест логики подсчета
}
```

**Тесты Service:**
```java
@Test
void rateMod_ShouldCallRepository() {
    // Тест бизнес-логики с мокированным repository
}
```

**Тесты Handler:**
```java
@Test
void rateMod_ShouldReturnValidResponse() {
    // Тест обработки gRPC запрос/ответ
}
```

### **Интеграционные Тесты:**
- Тест полного потока: Handler → Service → Repository → Database
- Использовать TestContainers для реального PostgreSQL в тестах

---

## 🔐 Соображения Безопасности

### **Текущий Статус Безопасности:**
- ✅ Защита от SQL инъекций (PreparedStatements)  
- ❌ Нет аутентификации/авторизации
- ❌ Нет ограничения скорости
- ❌ Нет санитизации ввода

### **Рекомендуемые Дополнения Безопасности:**
1. **Аутентификация**: Проверка личности пользователя
2. **Авторизация**: Проверка может ли пользователь оценивать этот мод
3. **Ограничение скорости**: Предотвращение спам-рейтингов
4. **Валидация ввода**: Санитизация всех входов
5. **Аудит логирование**: Логировать кто что когда делал

---

## 📊 Соображения Производительности

### **Текущая Производительность:**
- ✅ Пул соединений (HikariCP)
- ✅ Подготовленные statements (быстрее чем сырой SQL)
- ✅ Правильная очистка ресурсов (try-with-resources)

### **Потенциальные Узкие Места:**
1. **Запросы к базе данных**: Нет кэширования для популярных модов
2. **Нет пагинации**: `getRates()` может вернуть огромные наборы данных  
3. **Нет батчинга**: Каждый рейтинг — отдельный вызов базы данных

### **Улучшения Производительности:**
1. **Добавить кэширование** (Redis) для статистики популярных модов
2. **Добавить индексы базы данных** на часто запрашиваемые столбцы
3. **Реализовать пагинацию** для больших наборов результатов
4. **Использовать пул соединений** (уже реализовано)

---

## 🚀 Руководство по Развертыванию

### **Docker Развертывание:**
```bash
# Собрать образ
docker build -t rating-service:latest .

# Запустить контейнер
docker run -d \
  --name rating-service \
  -e DATABASE_URL="jdbc:postgresql://db:5432/ratings" \
  -e PORT=9090 \
  -p 9090:9090 \
  rating-service:latest
```

### **Kubernetes Развертывание:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rating-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rating-service
  template:
    spec:
      containers:
      - name: rating-service
        image: rating-service:latest
        ports:
        - containerPort: 9090
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
```

---





**Следующие Шаги:**
1. Исправить метод `getRates()` для использования реальных данных
2. Добавить правильную валидацию ввода  
3. Добавить комплексные тесты
4. Рассмотреть добавление кэширования для лучшей производительности

