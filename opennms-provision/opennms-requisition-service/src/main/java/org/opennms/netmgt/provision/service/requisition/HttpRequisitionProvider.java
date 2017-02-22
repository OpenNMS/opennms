package org.opennms.netmgt.provision.service.requisition;

import java.net.URI;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.AbstractRequisitionProvider;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class HttpRequisitionProvider extends AbstractRequisitionProvider<HttpRequisitionRequest> {

    public static final String TYPE_NAME = "http";

    public HttpRequisitionProvider() {
        super(HttpRequisitionRequest.class);
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public HttpRequisitionRequest getRequest(Map<String, String> parameters) {
        return new HttpRequisitionRequest(parameters);
    }

    @Override
    public Requisition getRequisitionFor(HttpRequisitionRequest request) {
        try (HttpClientWrapper client = HttpClientWrapper.create()) {
            final URI uri = new URI(request.getUrl());
            HttpGet get = new HttpGet(uri);
            if (Boolean.FALSE.equals(request.getStrictSsl())) {
                client.trustSelfSigned(uri.getScheme());
            }
            if (request.getUsername() != null) {
                client.addBasicCredentials(request.getPassword(), request.getPassword());
            }
            try (CloseableHttpResponse response = client.execute(get)) {
                String responseString = new BasicResponseHandler().handleResponse(response);
                return JaxbUtils.unmarshal(Requisition.class, responseString);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
