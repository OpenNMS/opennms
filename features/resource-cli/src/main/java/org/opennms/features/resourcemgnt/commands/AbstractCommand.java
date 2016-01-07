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

    protected WebResource connect(final ResourceCli resourceCli, final String resource) {
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

        // Build the web resource
        return apacheHttpClient
                .resource(url.toString());
    }

    protected WebResource connect(final ResourceCli resourceCli) {
        return this.connect(resourceCli, null);
    }
}
