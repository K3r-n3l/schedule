package ru.university.schedule.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Lesson {
    private LocalDateTime dateTime;
    private String group;
    private String subject;
    private String room;
    private String teacher;

    public Lesson(LocalDateTime dateTime, String group, String subject,
                  String room, String teacher) {
        this.dateTime = dateTime;
        this.group = group;
        this.subject = subject;
        this.room = room;
        this.teacher = teacher;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (dateTime == null)                       { errors.add("Не указана дата и время"); }
        if (group == null || group.isBlank())       { errors.add("Не указана группа"); }
        if (subject == null || subject.isBlank())   { errors.add("Не указан предмет"); }
        if (room == null || room.isBlank())         { errors.add("Не указана аудитория"); }
        if (teacher == null || teacher.isBlank())   { errors.add("Не указан преподаватель"); }

        return errors;
    }

    public LessonKind kind() { return LessonKind.LESSON; }

    @Override
    public String toString() {
        return kind().name() + " Время и дата: %s; Группа: %s; Предмет: %s; Аудитория: %s; Преподаватель: %s".
                formatted(dateTime, group, subject, room, teacher);
    }

    public LocalDateTime getDateTime()  { return dateTime; }
    public String getGroup()            { return group; }
    public String getSubject()          { return subject; }
    public String getRoom()             { return room; }
    public String getTeacher()          { return teacher; }

    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public void setGroup(String group)              { this.group = group; }
    public void setSubject(String subject)          { this.subject = subject; }
    public void setRoom(String room)                { this.room = room; }
    public void setTeacher(String teacher)          { this.teacher = teacher; }
}
