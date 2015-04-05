package org.syncloud.platform.ssh.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.syncloud.common.ParameterMessages;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SshShortResult {
    public boolean success;
    public String message;
    public List<ParameterMessages> parameters_messages;
}
