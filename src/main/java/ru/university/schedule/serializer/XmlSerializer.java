package ru.university.schedule.serializer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlSerializer extends JacksonSerializer {
    public XmlSerializer() {
        super(new XmlMapper(), "xml");
    }
}
