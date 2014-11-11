package org.syncloud.redirect.model;

public class Response {
    public Response(int statusCode, RestMessage data) {
        this.statusCode = statusCode;
        this.data = data;
    }

    public int statusCode;
    public RestMessage data;
}
