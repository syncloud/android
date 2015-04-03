package org.syncloud.redirect.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIgnoreProperties({"statusCode"})
public class RestResult {
    public String message;
    public List<ParameterMessages> parameters_messages;

    public int statusCode;
}
