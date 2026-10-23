package ru.university.schedule.model;

import java.time.LocalDateTime;

public class Cancelled extends Lesson {
    public Cancelled() {}

    public Cancelled(LocalDateTime dateTime, String group, String subject,
                     String room, String teacher) {
        super(dateTime, group, subject, room, teacher);
    }

    @Override public LessonKind kind() { return LessonKind.CANCELLED; }

    @Override public void setTeacher(String teacher)          { throw readOnly(); }
    @Override public void setRoom(String room)                { throw readOnly(); }
    @Override public void setSubject(String subject)          { throw readOnly(); }
    @Override public void setGroup(String group)              { throw readOnly(); }
    @Override public void setDateTime(LocalDateTime dateTime) { throw readOnly(); }

    public UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("Занятие отменено. Только для чтения");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return baseEquals((Cancelled) o);
    }

    @Override
    public int hashCode() {
        return baseHash();
    }
}
