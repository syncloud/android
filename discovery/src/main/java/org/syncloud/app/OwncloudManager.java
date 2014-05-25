package org.syncloud.app;

import com.google.common.base.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.syncloud.model.Device;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.ssh.Ssh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.syncloud.ssh.Ssh.execute;

public class OwncloudManager {

    private static String OWNCLOUD_CTL_BIN = "/opt/owncloud-ctl/bin/owncloud-ctl";

    public static Result<SshResult> finishSetup(Device device, String login, String password) {
        return execute(device, asList(String.format("%s finish %s %s", OWNCLOUD_CTL_BIN, login, password)));
    }

    public static Result<Optional<String>> owncloudUrl(Device device) {

        Result<SshResult> execute = execute(device, asList(String.format("%s url", OWNCLOUD_CTL_BIN)));
        if (execute.hasError())
            return Result.error(execute.getError());

        if (!execute.getValue().ok())
            return Result.value(Optional.<String>absent());
        else
            return Result.value(Optional.fromNullable(execute.getValue().getMessage()));

    }

}
