package ru.university.schedule.serializer;

import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonSerializer extends JacksonSerializer {
    public JsonSerializer() {
        super(new JsonMapper(), "json");
    }
}
