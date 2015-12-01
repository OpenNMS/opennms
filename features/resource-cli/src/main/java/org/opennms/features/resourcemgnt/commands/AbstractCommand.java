package org.opennms.features.resourcemgnt.commands;

import org.opennms.features.resourcemgnt.ResourceCli;

import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public abstract class AbstractCommand implements Command {

    protected Invocation.Builder connect(final ResourceCli resourceCli, final String resource) {
        // Initialize the REST client
        final DefaultApacheHttpClientConfig defaultApacheHttpClientConfig = new DefaultApacheHttpClientConfig();
        defaultApacheHttpClientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        defaultApacheHttpClientConfig.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, Boolean.TRUE);
        defaultApacheHttpClientConfig.getState().setCredentials(null, null, -1, resourceCli.getUsername(), resourceCli.getPassword());
        final ApacheHttpClient apacheHttpClient = ApacheHttpClient.create(defaultApacheHttpClientConfig);

        // Build the request URL
        final StringBuilder url = new StringBuilder();
        url.append(resourceCli.getBaseUrl());
        url.append("/rest/resources");
        if (! Strings.isNullOrEmpty(resource)) {
            url.append("/");
            url.append(UrlEscapers.urlPathSegmentEscaper().escape(resource));
        }
        WebTarget target = client.target(url.toString());

        String authorizationHeader = "Basic " + Base64Utility.encode((resourceCli.getUsername() + ":" + resourceCli.getPassword()).getBytes());
        return target.request().header("Authorization", authorizationHeader);
    }

    protected Invocation.Builder connect(final ResourceCli resourceCli) {
        return connect(resourceCli, null);
    }
}
