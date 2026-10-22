package ru.university.schedule.serializer;

import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public final class CsvSerializer implements Serializer {
    private static final String SEP = ";";
    private static final String HEADER = "type;dateTime;group;subject;room;teacher;extra";
    private static final int COLUMNS = 7;

    @Override
    public String format() {
        return "csv";
    }

    @Override
    public void save(List<Lesson> lessons, Path path) {
        List<String> lines = new ArrayList<>(lessons.size() + 1);
        lines.add(HEADER);
        for (Lesson l : lessons) {
            String extra = "";
            if (l instanceof Lecture lecture) extra = lecture.getStream();
            else if (l instanceof Practice practice) extra = String.valueOf(practice.getSubgroup());

            lines.add(String.join(SEP,
                    l.kind().name(),
                    l.getDateTime().toString(),
                    escape(l.getGroup()),
                    escape(l.getSubject()),
                    escape(l.getRoom()),
                    escape(l.getTeacher()),
                    escape(extra)));
        }
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать файл: " + path, e);
        }
    }

    @Override
    public List<Lesson> load(Path path) {
        return loadWithReport(path).lessons();
    }

    @Override
    public LoadResult loadWithReport(Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать файл: " + path, e);
        }

        List<Lesson> lessons = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        List<LoadResult.Skipped> skipped = new ArrayList<>();

        int number = 0; // порядковый номер объекта (строки данных), без заголовка и пустых строк
        boolean first = true;
        for (String line : lines) {
            if (first) {
                first = false;
                if (line.startsWith("﻿")) line = line.substring(1);
                if (line.toLowerCase().startsWith("type" + SEP)) continue;
            }
            if (line.isBlank()) continue;
            number++;

            try {
                lessons.add(parse(line));
                numbers.add(number);
            } catch (RuntimeException e) {
                skipped.add(new LoadResult.Skipped(number, e.getMessage()));
            }
        }
        return new LoadResult(lessons, numbers, skipped);
    }

    private static Lesson parse(String line) {
        List<String> f = split(line);
        if (f.size() != COLUMNS) {
            throw new IllegalArgumentException("ожидалось " + COLUMNS + " полей, найдено " + f.size());
        }

        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(f.get(1).trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("неверная дата/время: " + f.get(1));
        }

        String group = f.get(2), subject = f.get(3), room = f.get(4), teacher = f.get(5), extra = f.get(6);

        Lesson lesson = switch (f.get(0).trim().toUpperCase()) {
            case "LESSON" -> new Lesson(dt, group, subject, room, teacher);
            case "LECTURE" -> new Lecture(dt, group, subject, room, teacher, extra);
            case "PRACTICE" -> {
                try {
                    yield new Practice(dt, group, subject, room, teacher, Integer.parseInt(extra.trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("подгруппа не число: " + extra);
                }
            }
            case "CANCELLED" -> new Cancelled(dt, group, subject, room, teacher);
            default -> throw new IllegalArgumentException("неизвестный тип: " + f.get(0));
        };

        List<String> errors = lesson.validate();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
        return lesson;
    }

    private static String escape(String s) {
        if (s == null) return "";
        s = s.replace('\n', ' ').replace('\r', ' ');
        return s.contains(SEP) || s.contains("\"")
                ? "\"" + s.replace("\"", "\"\"") + "\""
                : s;
    }

    private static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ';') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (quoted) throw new IllegalArgumentException("незакрытая кавычка");
        out.add(cur.toString());
        return out;
    }
}
