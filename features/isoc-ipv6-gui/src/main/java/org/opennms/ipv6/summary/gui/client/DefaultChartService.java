package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class DefaultChartService implements ChartService {
    
    public class DummyResponse extends Response {

        @Override
        public String getHeader(String header) {
            return null;
        }

        @Override
        public Header[] getHeaders() {
            return null;
        }

        @Override
        public String getHeadersAsString() {
            return null;
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getStatusText() {
            return "Status is dummy";
        }

        @Override
        public String getText() {
            return "{\"data\":{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"3.350\"}],\"time\":\"1305913527158\"}}";
        }

    }

    public static final String SERVICE_URL = "/rest/locationmonitors";
    
    @Override
    public void getAllLocationsAvailability(RequestCallback callback) {
        //RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(SERVICE_URL));
        
        //builder.sendRequest(null, callback);
        Response response = new DummyResponse();
        callback.onResponseReceived(null, response);
    }

    @Override
    public void getAvailabilityByLocation(String location, RequestCallback callback) {
        callback.onResponseReceived(null, new DummyResponse());
    }

    @Override
    public void getAvailabilityByParticipant(String participant, RequestCallback callback) {
        callback.onResponseReceived(null, new DummyResponse());
    }

    @Override
    public void getAvailabilityByLocationAndParticipant(String location,String participant, RequestCallback callback) {
        callback.onResponseReceived(null, new DummyResponse());
    }

}
