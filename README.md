![java-version](https://img.shields.io/badge/Java-11-brightgreen?style=flat-square)
[![spring-boot-version](https://img.shields.io/badge/spring--boot-2.7+-brightgreen?style=flat-square)](https://github.com/spring-projects/spring-boot/releases)
[![jitpack-last-release](https://jitpack.io/v/spacious-team/table-wrapper-spring-boot-starter.svg?style=flat-square)](
https://jitpack.io/#spacious-team/table-wrapper-api)
[![Unit tests](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fspacious-team%2Ftable-wrapper-spring-boot-starter%2Fbadge%3Fref%3Ddevelop&style=flat-square&label=Test&logo=none)](
https://github.com/spacious-team/table-wrapper-spring-boot-starter/actions/workflows/unit-tests.yml)
[![Coverage](https://img.shields.io/codecov/c/github/spacious-team/table-wrapper-spring-boot-starter/develop?label=Coverage&style=flat-square&token=kqEvgEiKnQ)](
https://codecov.io/gh/spacious-team/table-wrapper-spring-boot-starter)

### Назначение
Позволяет работать с табличным представлением данных в форматах Excel, Xml и Csv через единый интерфейс
[Table Wrapper API](https://github.com/spacious-team/table-wrapper-api).

Совместим с проектами Spring Boot 2.7 и выше.

Spring Boot Starter настраивает реализации фабрик `ExcelTableFactory`, `XmlTableFactory`, `CsvTableFactory`
и подключает необходимые зависимости
1. [table-wrapper-excel-impl](https://github.com/spacious-team/table-wrapper-excel-impl) для работы с excel файлами
```xml
<dependency>
    <groupId>com.github.spacious-team</groupId>
    <artifactId>table-wrapper-excel-impl</artifactId>
</dependency>
```
2. [table-wrapper-xml-impl](https://github.com/spacious-team/table-wrapper-xml-impl) для работы с xml файлами
```xml
<dependency>
    <groupId>com.github.spacious-team</groupId>
    <artifactId>table-wrapper-xml-impl</artifactId>
</dependency>
```
3. [table-wrapper-csv-impl](https://github.com/spacious-team/table-wrapper-csv-impl) для работы с csv (tsv) файлами
```xml
<dependency>
    <groupId>com.github.spacious-team</groupId>
    <artifactId>table-wrapper-csv-impl</artifactId>
</dependency>
```

### Подключение стартера к проекту
Необходимо подключить репозиторий open source библиотек github
[jitpack](https://jitpack.io/#spacious-team/table-wrapper-spring-boot-starter), например для Apache Maven проекта
```xml
<repositories>
    <repository>
        <id>central</id>
        <name>Central Repository</name>
        <url>https://repo.maven.apache.org/maven2</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Далее добавить зависимость
```xml
<dependency>
    <groupId>com.github.spacious-team</groupId>
    <artifactId>table-wrapper-spring-boot-starter</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

В качестве версии можно использовать:
- версию [релиза](https://github.com/spacious-team/table-wrapper-spring-boot-starter/releases) на github;
- паттерн `<branch>-SNAPSHOT` для сборки зависимости с последнего коммита выбранной ветки;
- короткий десяти значный номер коммита для сборки зависимости с указанного коммита.

### Пример использования
Определяются колонки таблицы:
```java
@lombok.Getter
@lombok.RequiredArgsConstructor
enum TableHeader implements TableHeaderColumn {
    PRODUCT(PatternTableColumn.of("Товар")),
    PRICE(PatternTableColumn.of("Цена"));

    private final TableColumn column;
}
```
Извлекаем данные из таблицы с указанными колонками вне зависимости от формата файла (excel, xml, csv и др.):
```java
@org.springframework.beans.factory.annotation.Autowired
ReportPageFactory reportPageFactory;

void parse() {
    // Получаем страницу с данными. Используем бин ReportPageFactory для построения абстракции
    ReportPage reportPage = reportPageFactory.create("1.xlsx");
    // ... или reportPageFactory.create("1.xml");
    // ... или reportPageFactory.create("1.csv");

    // Метод найдет ячейку с текстом "Таблица 1",
    // воспринимает следующую за ней строку как заголовок таблицы (который описан через enum TableHeader).
    // Из последующих строк (до пустой строки или конца файла) извлекаются данные
    // (метод использует бин ExcelTableFactory для создания таблицы Table на основе ReportPage) 
    Table table = reportPage.create("Таблица 1", TableHeader.class);

    // Итерируемся по строкам таблицы и извлекаем ячейки из строк по заголовку таблицы
    table.stream()
            .forEach(row -> {
                String product = row.getStringCellValue(TableHeader.PRODUCT);
                BigDecimal price = getBigDecimalCellValue(TableHeader.PRICE);
            });
}
```
