package org.opennms.features.resourcemgnt.commands;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import org.apache.cxf.common.util.Base64Utility;
import org.opennms.features.resourcemgnt.ResourceCli;

import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;

public abstract class AbstractCommand implements Command {

    protected Invocation.Builder connect(final ResourceCli resourceCli, final String resource) {
        // Initialize the REST client
        final Client client = ClientBuilder.newClient();

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
