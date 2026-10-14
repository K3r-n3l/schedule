package ru.university.schedule.store;

import ru.university.schedule.model.Lesson;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class AvlLessonStore extends AvlStore<LessonKey, Lesson>
        implements EntityStore<LessonKey, Lesson> {

    public void add(Lesson lesson) {
        add(LessonKey.of(lesson), lesson);
    }

    public Optional<Lesson> findNearest(LocalDateTime target, String group) {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("Группа не указана");
        }
        NearestSearch search = new NearestSearch(group, target);
        search.collect(root);
        return Optional.ofNullable(search.best);
    }

    private static final class NearestSearch {
        private final String group;
        private final LocalDateTime target;
        private Lesson best;

        NearestSearch(String group, LocalDateTime target) {
            this.group = group;
            this.target = target;
        }

        void collect(Node node) {
            if (node == null) return;

            Lesson lesson = node.value;
            if (lesson.getGroup().equals(group)) {
                long d = Math.abs(Duration.between(target, lesson.getDateTime()).toMillis());
                if (best == null || d < Math.abs(Duration.between(target, best.getDateTime()).toMillis())) {
                    best = lesson;
                }
            }

            collect(node.left);
            collect(node.right);
        }
    }
}
