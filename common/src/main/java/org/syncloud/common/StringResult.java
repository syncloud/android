package org.syncloud.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.syncloud.common.BaseResult;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StringResult extends BaseResult {
    public String data;
}
