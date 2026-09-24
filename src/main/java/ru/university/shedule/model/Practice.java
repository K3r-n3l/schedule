package ru.university.shedule.model;

import java.time.LocalDateTime;
import java.util.List;

public class Practice extends Lesson implements Editable {
    protected int subgroup;

    public Practice(LocalDateTime dateTime, String group, String subject,
                    String room, String teacher, int subgroup) {
        super(dateTime, group, subject, room, teacher);
        this.subgroup = subgroup;
    }

    @Override
    public List<String> validate() {
        List<String> errors = super.validate();

        if (subgroup < 1) { errors.add("Недопустимое значение подгруппы"); }

        return errors;
    }

    @Override public LessonType getType() { return LessonType.PRACTICE; }

    @Override
    public String toString() {
        return super.toString() + "; Подгруппа: %s".formatted(subgroup);
    }

    public int getSubgroup()              { return subgroup; }
    public void setSubgroup(int subgroup) { this.subgroup = subgroup; }
}
