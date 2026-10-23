package ru.university.schedule.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Lecture extends Lesson implements Editable {
    protected String stream;

    public Lecture() {}

    public Lecture(LocalDateTime dateTime, String group, String subject,
                   String room, String teacher, String stream) {
        super(dateTime, group, subject, room, teacher);
        this.stream = stream;
    }

    @Override
    public List<String> validate() {
        List<String> errors = super.validate();

        if (stream == null || stream.isBlank()) { errors.add("Не указан поток"); }

        return errors;
    }

    @Override public LessonKind kind() { return LessonKind.LECTURE; }

    @Override
    public String toString() {
        return super.toString() + "; Поток: %s".formatted(stream);
    }

    public String getStream()            { return stream; }
    public void setStream(String stream) { this.stream = stream; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                                   // 1. та же ссылка
        if (o == null || getClass() != o.getClass()) return false;    // 2. тот же класс
        Lecture other = (Lecture) o;                                  // 3. каст безопасен
        return baseEquals(other) && Objects.equals(stream, other.stream); // 4. своё поле
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, group, subject, room, teacher, stream);
    }
}
