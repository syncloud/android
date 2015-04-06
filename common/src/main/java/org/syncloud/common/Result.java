package org.syncloud.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.syncloud.common.BaseResult;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result<T> extends BaseResult {
    public T data;
}
