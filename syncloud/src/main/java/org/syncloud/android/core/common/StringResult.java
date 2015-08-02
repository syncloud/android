package org.syncloud.android.core.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StringResult extends BaseResult {
    public String data;
}
