package org.syncloud.redirect.unit.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class Rest {

    public static final String URL = "http://localhost:1111";
    private static HttpServer server;

    public static void start(Class<?> clazz) {
        server = GrizzlyHttpServerFactory.createHttpServer(
                UriBuilder.fromUri(URL).build(),
                new ResourceConfig(clazz),
                true);
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        server.shutdownNow();
    }

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public static class ExistingUser {

        @GET
        @Path("/user/get")
        public String get() {
            return "{\n" +
                    "  \"active\": true, \n" +
                    "  \"email\": \"ribalkin1@gmail.com\", \n" +
                    "  \"ip\": null, \n" +
                    "  \"port\": null, \n" +
                    "  \"update_token\": null, \n" +
                    "  \"user_domain\": null\n" +
                    "}";
        }

        @POST
        @Path("/user/create")
        public Response create() {
            return Response.status(409).entity(
                    "{\n" +
                    "  \"message\": \"Email is already registered\"\n" +
                    "}"
            ).build();
        }
    }

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public static class MissingUser {

        @GET
        @Path("/user/get")
        public Response get() {
            return Response.status(403).entity(
                    "{\n" +
                    "  \"message\": \"Authentication failed\"\n" +
                    "}"
            ).build();
        }

        @POST
        @Path("/user/create")
        public Response create() {
            return Response.ok().entity(
                    "{\n" +
                            "  \"message\": \"User was created\"\n" +
                            "}"
            ).build();
        }
    }
}