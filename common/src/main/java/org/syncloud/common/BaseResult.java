package org.syncloud.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResult {
    public boolean success;
    public String message;
    public List<ParameterMessages> parameters_messages;
}
