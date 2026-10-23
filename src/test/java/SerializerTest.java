import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.university.schedule.model.Cancelled;
import ru.university.schedule.model.Lecture;
import ru.university.schedule.model.Lesson;
import ru.university.schedule.model.Practice;
import ru.university.schedule.serializer.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest {

    @TempDir
    Path tmp;

    private static final LocalDateTime DT = LocalDateTime.of(2026, 10, 26, 9, 0);

    private static Lecture lecture() {
        return new Lecture(DT, "ИВТ-101", "Java", "A-101", "Иванов", "Поток-1");
    }

    private static Practice practice() {
        return new Practice(DT.plusHours(2), "ИВТ-101", "Java", "B-202", "Петров", 2);
    }

    private static Cancelled cancelled() {
        return new Cancelled(DT.plusDays(1), "ИВТ-101", "Алгебра", "C-303", "Сидоров");
    }

    // позитивные

    @Test
    void csv_roundTrip_allLessonTypes() {
        Serializer s = new CsvSerializer();
        Path file = tmp.resolve("s.csv");
        List<Lesson> src = List.of(new Lesson(DT, "ИВТ-101", "Физика", "D-1", "Орлов"),
                lecture(), practice());

        s.save(src, file);

        assertEquals(src, s.load(file));
    }

    @Test
    void json_roundTrip_lectureAndPractice() {
        Serializer s = new JsonSerializer();
        Path file = tmp.resolve("s.json");
        List<Lesson> src = List.of(lecture(), practice());

        s.save(src, file);

        assertEquals(src, s.load(file));
    }

    @Test
    void yaml_roundTrip_allLessonTypes() {
        Serializer s = new YamlSerializer();
        Path file = tmp.resolve("s.yaml");
        List<Lesson> src = List.of(new Lesson(DT, "ИВТ-101", "Физика", "D-1", "Орлов"),
                lecture(), practice());

        s.save(src, file);

        assertEquals(src, s.load(file));
    }

    @Test
    void binary_roundTrip_includingCancelled() {
        Serializer s = new BinarySerializer();
        Path file = tmp.resolve("s.bin");
        List<Lesson> src = List.of(lecture(), practice());

        s.save(src, file);

        assertEquals(src, s.load(file));
    }

    @Test
    void protobuf_roundTrip_includingCancelled() {
        Serializer s = new ProtobufSerializer();
        Path file = tmp.resolve("s.binpb");
        List<Lesson> src = List.of(lecture(), practice());

        s.save(src, file);

        assertEquals(src, s.load(file));
    }

    @Test
    void csv_loadWithReport_handlesBomHeaderAndBlankLines() throws IOException {
        String content = "\uFEFF" + CsvSerializer.HEADER + "\n\n"
                + "lecture;2026-10-26T09:00:00;ИВТ-101;Java;A-101;Иванов;Поток-1;\n";
        Path file = tmp.resolve("bom.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        LoadResult r = new CsvSerializer().loadWithReport(file);

        assertEquals(List.of(3), r.numbers());
        assertEquals(1, r.lessons().size());
        assertTrue(r.skipped().isEmpty());
    }

    // негативные

    private LoadResult loadCsvLines(String... lines) throws IOException {
        Path file = tmp.resolve("bad.csv");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return new CsvSerializer().loadWithReport(file);
    }

    @Test
    void csv_wrongFieldCount_isSkipped() throws IOException {
        LoadResult r = loadCsvLines(CsvSerializer.HEADER, "lecture;2026-10-26T09:00:00;ИВТ-101");

        assertTrue(r.lessons().isEmpty());
        assertEquals(1, r.skipped().size());
        assertEquals(2, r.skipped().get(0).number());
        assertTrue(r.skipped().get(0).reason().contains("WRONG_FIELD_COUNT"));
    }

    @Test
    void csv_unknownType_isSkipped() throws IOException {
        LoadResult r = loadCsvLines("seminar;2026-10-26T09:00:00;ИВТ-101;Java;A-101;Иванов;;");

        assertTrue(r.lessons().isEmpty());
        assertTrue(r.skipped().get(0).reason().contains("UNKNOWN_TYPE"));
    }

    @Test
    void csv_badDateTime_isSkipped() throws IOException {
        LoadResult r = loadCsvLines("lesson;не-дата;ИВТ-101;Java;A-101;Иванов;;");

        assertTrue(r.lessons().isEmpty());
        assertTrue(r.skipped().get(0).reason().contains("BAD_DATETIME"));
    }

    @Test
    void csv_badSubgroupNumber_isSkipped() throws IOException {
        LoadResult r = loadCsvLines("practice;2026-10-26T09:00:00;ИВТ-101;Java;A-101;Иванов;;abc");

        assertTrue(r.lessons().isEmpty());
        assertTrue(r.skipped().get(0).reason().contains("BAD_NUMBER"));
    }

    @Test
    void csv_invalidData_isSkipped_andValidLinesStillLoaded() throws IOException {
        LoadResult r = loadCsvLines(
                "lecture;2026-10-26T09:00:00;ИВТ-101;Java;A-101;Иванов;;",
                "lesson;2026-10-26T09:00:00;ИВТ-101;Java;A-101;Иванов;;");

        assertEquals(1, r.lessons().size());
        assertEquals(1, r.skipped().size());
        assertTrue(r.skipped().get(0).reason().contains("INVALID_DATA"));
    }

    @Test
    void csv_missingFile_throwsUncheckedIOException() {
        assertThrows(UncheckedIOException.class,
                () -> new CsvSerializer().load(tmp.resolve("nope.csv")));
    }

    @Test
    void json_malformedFile_throwsIllegalArgument() throws IOException {
        Path file = tmp.resolve("broken.json");
        Files.writeString(file, "{ это не json");

        assertThrows(IllegalArgumentException.class, () -> new JsonSerializer().load(file));
    }

    @Test
    void json_unknownProperty_throwsIllegalArgument() throws IOException {
        Path file = tmp.resolve("unknown.json");
        Files.writeString(file, """
                {"lessons":[{"type":"lecture","dateTime":"2026-10-26T09:00:00",
                "group":"g","subject":"s","room":"r","teacher":"t","stream":"x","extra":1}]}
                """);

        assertThrows(IllegalArgumentException.class, () -> new JsonSerializer().load(file));
    }

    @Test
    void binary_corruptedFile_throwsUncheckedIOException() throws IOException {
        Path file = tmp.resolve("garbage.bin");
        Files.write(file, new byte[]{1, 2, 3, 4, 5, 6, 7, 8});

        assertThrows(UncheckedIOException.class, () -> new BinarySerializer().load(file));
    }

    @Test
    void cancelled_isReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> cancelled().setRoom("X-1"));
    }
}
