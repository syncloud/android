package org.syncloud.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.model.Result;
import org.syncloud.model.SshResult;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public static <T> Result<List<T>> parse(SshResult sshResult, Class<T> valueType) {

        if (!sshResult.ok())
            return Result.error(sshResult.getMessage());

        try {
            List<T> objs = new ArrayList<T>();
            ObjectMapper mapper = new ObjectMapper();
            String[] lines = sshResult.getMessage().split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().startsWith("{"))
                    objs.add(mapper.readValue(line, valueType));
            }
            return Result.value(objs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public static <T> Result<T> parseSingle(SshResult sshResult, Class<T> valueType) {

        if (!sshResult.ok())
            return Result.error(sshResult.getMessage());

        try {
            return Result.value(new ObjectMapper().readValue(sshResult.getMessage(), valueType));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
