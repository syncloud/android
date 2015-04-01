package org.syncloud.platform.ssh;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.platform.ssh.model.SshShortResult;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class DeserializationTest {
    public static final ObjectMapper JSON = new ObjectMapper();

    @Test
    public void testParametersMessages() throws IOException {
        String json = "{\n" +
                "\t\"parameters_messages\": \n" +
                "\t[\n" +
                "\t\t{\"parameter\": \"user_domain\", \"messages\": [\"User domain name is already in use\"]}\n" +
                "\t], \n" +
                "\t\"message\": \"There's a error in parameters\", \n" +
                "\t\"success\": false\n" +
                "}";

        SshShortResult result = JSON.readValue(json, SshShortResult.class);
        assertFalse(result.success);
        assertEquals("There's a error in parameters", result.message);
        assertEquals(1, result.parameters_messages.size());
        assertEquals(1, result.parameters_messages.get(0).messages.size());
        assertEquals("User domain name is already in use", result.parameters_messages.get(0).messages.get(0));
    }
}
