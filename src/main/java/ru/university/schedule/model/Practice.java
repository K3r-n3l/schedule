package ru.university.schedule.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Practice extends Lesson implements Editable {
    protected int subgroup;

    public Practice() {}

    public Practice(LocalDateTime dateTime, String group, String subject,
                    String room, String teacher, int subgroup) {
        super(dateTime, group, subject, room, teacher);
        this.subgroup = subgroup;
    }

    @Override
    public List<String> validate() {
        List<String> errors = super.validate();
        if (subgroup < 1) {
            errors.add("Недопустимое значение подгруппы");
        }
        return errors;
    }

    @Override
    public LessonKind kind() { return LessonKind.PRACTICE; }

    @Override
    public String toString() {
        return super.toString() + "; Подгруппа: %s".formatted(subgroup);
    }

    public int getSubgroup()              { return subgroup; }
    public void setSubgroup(int subgroup) { this.subgroup = subgroup; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Practice other = (Practice) o;
        return baseEquals(other) && subgroup == other.subgroup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, group, subject, room, teacher, subgroup);
    }
}
