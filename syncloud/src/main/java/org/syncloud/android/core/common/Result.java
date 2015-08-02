package org.syncloud.android.core.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result<T> extends BaseResult {
    public T data;
}
