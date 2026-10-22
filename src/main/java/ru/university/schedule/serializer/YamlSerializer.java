package ru.university.schedule.serializer;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlSerializer extends JacksonSerializer {
    public YamlSerializer() {
        super(new YAMLMapper(), "yaml");
    }
}
