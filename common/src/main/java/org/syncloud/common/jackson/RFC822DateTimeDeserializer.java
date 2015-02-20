package org.syncloud.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RFC822DateTimeDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext) throws IOException, JsonProcessingException {
        DateFormat format = new SimpleDateFormat();
        String dateStr = jsonparser.getText();
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            throw new IOException("Can't parse DateTime from RFC-822 format", e);
        }
        return date;
    }
}
