package ru.university.shedule.csv;

public class CsvLineException extends Exception {
    private final int lineNumber;
    private final CsvErrorCode code;

    public CsvLineException(int lineNumber, CsvErrorCode code, String details) {
        super("Строка %d; Код ошибки %s, описание: %s%s".
                formatted(lineNumber, code, code.getDescription(),
                        details == null || details.isBlank() ? "" : ": " + details));
        this.lineNumber = lineNumber;
        this.code = code;
    }

    public int getLineNumber()      { return lineNumber; }
    public CsvErrorCode getCode()   { return code; }
}
