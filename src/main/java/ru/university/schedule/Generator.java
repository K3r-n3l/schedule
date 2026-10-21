package ru.university.schedule;

import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public final class Generator {
    private static final List<String> GROUPS = List.of(
            "ИВТ-1", "ИВТ-2", "ИВТ-21", "ИВТ-22", "ПИ-1", "ПИ-2");

    private static final List<String> SUBJECTS = List.of(
            "Математика", "Физика", "Программирование",
            "Базы данных", "Английский язык", "Теория вероятностей");

    private static final List<String> TEACHERS = List.of(
            "Петров П.П.", "Сидорова А.А.", "Кузнецов К.К.",
            "Иванова И.И.", "Смирнов С.С.");

    private static final List<String> ROOMS = List.of(
            "А-101", "А-102", "Б-204", "Б-205", "В-310", "Г-11");

    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    private static final LocalTime DAY_END = LocalTime.of(18, 0);
    private static final int MINUTES_PER_DAY =
            (int) Duration.between(DAY_START, DAY_END).toMinutes();

    private static final int MAX_NOISE_MINUTES = 5;

    private final Random random = new Random();

    public List<Lesson> generate(int count, LocalDate startDay, int days) {
        if (count < 1) throw new IllegalArgumentException("count < 1");
        if (days < 1)  throw new IllegalArgumentException("days < 1");

        int slotsPerDay = MINUTES_PER_DAY / (MAX_NOISE_MINUTES * 2 + 1); // шаг ≥ 11 минут
        long totalSlots = (long) slotsPerDay * days;
        if (count > totalSlots) {
            throw new IllegalArgumentException(
                    "Слишком много занятий: максимум " + totalSlots + " за " + days + " дн.");
        }

        List<Lesson> result = new ArrayList<>(count);
        long slotStep = totalSlots / count;          // средний шаг между занятиями
        long slot = random.nextInt((int) Math.max(1, slotStep));   // случайный старт

        for (int i = 0; i < count; i++) {
            int dayIndex = (int) (slot / slotsPerDay);
            int minuteOfDay = (int) (slot % slotsPerDay) * (MAX_NOISE_MINUTES * 2 + 1);

            LocalDate day = startDay.plusDays(dayIndex);
            LocalDateTime dateTime = LocalDateTime.of(day, DAY_START).plusMinutes(minuteOfDay);

            String group = GROUPS.get(random.nextInt(GROUPS.size()));
            String subject = SUBJECTS.get(random.nextInt(SUBJECTS.size()));
            String teacher = TEACHERS.get(random.nextInt(TEACHERS.size()));
            String room = ROOMS.get(random.nextInt(ROOMS.size()));

            if (random.nextBoolean()) {
                result.add(new Lecture(dateTime, group, subject, room, teacher,
                        "Поток " + (1 + random.nextInt(3))));
            } else {
                result.add(new Practice(dateTime, group, subject, room, teacher,
                        1 + random.nextInt(2)));
            }

            slot += slotStep;
        }
        return result;
    }
}