package ru.university.shedule.csv;

public enum CsvErrorCode {
    WRONG_FIELD_COUNT("Неверное количество полей"),
    UNKNOWN_TYPE("Неизвестный тип занятия"),
    BAD_DATETIME("Некорректные дата и время"),
    BAD_NUMBER("Ожидалось число"),
    INVALID_DATA("Данные не прошли валидацию");

    private final String description;

    CsvErrorCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
