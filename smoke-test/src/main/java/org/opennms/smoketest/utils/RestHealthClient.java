package org.opennms.smoketest.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;

public class RestHealthClient {

    private URL url;

    private String alias;

    private Client client;

    private final static String PROBE = "/rest/health/probe";
    private final static String SUCCESS_PROBE = "Everything is awesome";
    private final static String HEALTH_KEY = "Health";

    public RestHealthClient(final URL webUrl, final String alias){
        this.alias = alias;
        this.url = webUrl;
        this.client = ClientBuilder.newClient();
    }

    private WebTarget getTargetFor(final String path){
        return client.target(url.toString()).path(alias).path(path);
    }

    public String getProbeHealthResponse(){
        Response response
                = getTargetFor(PROBE).request(MediaType.TEXT_PLAIN).get();
        return response.getStatus() == 200 && response.getHeaders().containsKey(HEALTH_KEY) ?
                response.getHeaders().get(HEALTH_KEY).toString() : "Health key not found";
    }

    public String getProbeSuccessMessage(){return SUCCESS_PROBE;}
}
