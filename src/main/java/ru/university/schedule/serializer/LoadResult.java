package ru.university.schedule.serializer;

import ru.university.schedule.model.Lesson;

import java.util.ArrayList;
import java.util.List;

/**
 * Результат загрузки: занятия, их порядковые номера в файле (1-based, параллельный список)
 * и пропущенные битые объекты с причиной.
 */
public record LoadResult(List<Lesson> lessons, List<Integer> numbers, List<Skipped> skipped) {

    public record Skipped(int number, String reason) { }

    public static LoadResult of(List<Lesson> lessons) {
        List<Integer> numbers = new ArrayList<>(lessons.size());
        for (int i = 1; i <= lessons.size(); i++) {
            numbers.add(i);
        }
        return new LoadResult(lessons, numbers, List.of());
    }
}
