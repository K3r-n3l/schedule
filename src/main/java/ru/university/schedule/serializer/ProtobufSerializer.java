package ru.university.schedule.serializer;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.LessonKind;
import ru.university.schedule.model.Practice;
import ru.university.schedule.protobuf.ScheduleProtobuf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class ProtobufSerializer implements Serializer {
    public static final String format = ".binpb";
    private static final int SUPPORTED_VERSION = 1;

    @Override public String format() { return format; }

    @Override
    public void save(Lesson lesson, Path path) {
        byte[] bytes = ScheduleProtobuf.ScheduleData.newBuilder()
                .setVersion(SUPPORTED_VERSION)
                .addLessons(toProto(lesson))
                .build()
                .toByteArray();
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать файл: " + path, e);
        }
    }

    @Override
    public Lesson load(Path path) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать файл: " + path, e);
        }

        ScheduleProtobuf.ScheduleData proto;
        try {
            proto = ScheduleProtobuf.ScheduleData.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("Файл повреждён: " + e.getMessage(), e);
        }
        if (proto.getVersion() > SUPPORTED_VERSION) {
            throw new IllegalArgumentException("Версия формата " + proto.getVersion()
                    + " не поддерживается (максимум " + SUPPORTED_VERSION + ")");
        }
        if (proto.getLessonsCount() == 0) {
            throw new IllegalArgumentException("В файле нет занятий");
        }
        return fromProto(proto.getLessons(0));
    }

    private static ScheduleProtobuf.Lesson toProto(Lesson lesson) {
        LocalDateTime dt = lesson.getDateTime();
        ScheduleProtobuf.Lesson.Builder builder = ScheduleProtobuf.Lesson.newBuilder()
                .setType(typeCode(lesson.kind()).getNumber())
                .setSeconds(dt.toEpochSecond(ZoneOffset.UTC))
                .setNanos(dt.getNano())
                .setGroup(lesson.getGroup())
                .setSubject(lesson.getSubject())
                .setRoom(lesson.getRoom())
                .setTeacher(lesson.getTeacher());

        if (lesson instanceof Lecture lecture) {
            builder.setStream(lecture.getStream());
        } else if (lesson instanceof Practice practice) {
            builder.setSubgroup(practice.getSubgroup());
        }
        return builder.build();
    }

    private static ScheduleProtobuf.LessonType typeCode(LessonKind kind) {
        return switch (kind) {
            case LESSON -> ScheduleProtobuf.LessonType.LESSON;
            case LECTURE -> ScheduleProtobuf.LessonType.LECTURE;
            case PRACTICE -> ScheduleProtobuf.LessonType.PRACTICE;
            case CANCELLED -> ScheduleProtobuf.LessonType.CANCELLED;
        };
    }

    private static Lesson fromProto(ScheduleProtobuf.Lesson proto) {
        LocalDateTime dt = LocalDateTime.ofEpochSecond(
                proto.getSeconds(), proto.getNanos(), ZoneOffset.UTC);
        return switch (proto.getType()) {
            case ScheduleProtobuf.LessonType.LESSON_VALUE -> new Lesson(dt,
                    proto.getGroup(), proto.getSubject(), proto.getRoom(),
                    proto.getTeacher());
            case ScheduleProtobuf.LessonType.LECTURE_VALUE -> new Lecture(dt,
                    proto.getGroup(), proto.getSubject(), proto.getRoom(),
                    proto.getTeacher(), proto.getStream());
            case ScheduleProtobuf.LessonType.PRACTICE_VALUE -> new Practice(dt,
                    proto.getGroup(), proto.getSubject(), proto.getRoom(),
                    proto.getTeacher(), proto.getSubgroup());
            case ScheduleProtobuf.LessonType.CANCELLED_VALUE -> new Cancelled(dt,
                    proto.getGroup(), proto.getSubject(), proto.getRoom(),
                    proto.getTeacher());
            default -> throw new IllegalArgumentException(
                    "Запись с неизвестным типом: " + proto.getType());
        };
    }
}