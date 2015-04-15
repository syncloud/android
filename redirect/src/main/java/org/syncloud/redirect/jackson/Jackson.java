package org.syncloud.redirect.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Date;

public class Jackson {

    public static ObjectMapper createObjectMapper() {
        RFC822DateTimeDeserializer deserializer = new RFC822DateTimeDeserializer();

        SimpleModule module = new SimpleModule("RFC822DateTimeDeserializerModule", new Version(1, 0, 0, null));
        module.addDeserializer(Date.class, deserializer);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
