package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

//FIXME: Make sure you handle time range
public class DefaultChartService implements ChartService {
    
    public class AllParticipantsResponse extends DummyResponse {
        @Override
        public String getText() {
            return "{\"participants\":[{\"name\":\"alternate-node1\"},{\"name\":\"alternate-node2\"},{\"name\":\"node1\"},{\"name\":\"node2\"},{\"name\":\"node3\"},{\"name\":\"node4\"}]}";
        }

    }

    public class AllLocationsResponse extends DummyResponse {
       
        @Override
        public String getText() {
            return "{\"locations\":[{\"area\":\"here\"}, {\"area\":\"there\"}]}";
        }

    }

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
            return "{\"data\":[{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306469100000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306469400000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306469700000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306470000000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306470300000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306470600000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306470900000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306471200000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306471500000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306471800000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306472100000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306472400000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306472700000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306473000000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306473300000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306473600000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306473900000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306474200000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306474500000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306474800000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306475100000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306475400000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306475700000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306476000000\"},{\"values\":[{\"application\":\"IPv4\",\"availability\":\"0.000\"},{\"application\":\"IPv6\",\"availability\":\"0.000\"}],\"time\":\"1306476300000\"}]}";
        }

    }

    public static final String AVAILABILITY_SERVICE_URL = "/rest/remotelocations/availability";
    public static final String PARTICIPANT_SERVICE_URL = "/rest/remotelocations/participants";
    public static final String LOCATION_LIST_SERVICE_URL = "/rest/remotelocations";
    
    @Override
    public void getAllLocationsAvailability(RequestCallback callback) {
        sendRequest(AVAILABILITY_SERVICE_URL, callback);
        //callback.onResponseReceived(null, new DummyResponse());
    }

    @Override
    public void getAvailabilityByLocation(String location, RequestCallback callback) {
        String url = AVAILABILITY_SERVICE_URL + "/" + location;
        sendRequest(url, callback);
        
        //Commented out, but used for testing
        //callback.onResponseReceived(null, new DummyResponse());
    }

    

    @Override
    public void getAvailabilityByParticipant(String participant, RequestCallback callback) {
        String url = AVAILABILITY_SERVICE_URL + "/?host=" + participant;
        sendRequest(url, callback);
        
        //Commented out, but used for testing
        //callback.onResponseReceived(null, new DummyResponse());
    }

    @Override
    public void getAvailabilityByLocationAndParticipant(String location,String participant, RequestCallback callback) {
        String url = AVAILABILITY_SERVICE_URL + "/" + location + "/?host=" + participant;
        sendRequest(url, callback);
        
        //Commented out, but used for testing
        //callback.onResponseReceived(null, new DummyResponse());
    }

    @Override
    public void getAllLocations(RequestCallback callback) {
        String url = LOCATION_LIST_SERVICE_URL;
        sendRequest(url, callback);
        
        //Commented out, but used for testing
        //callback.onResponseReceived(null, new AllLocationsResponse());
    }

    @Override
    public void getAllParticipants(RequestCallback callback) {
        String url = PARTICIPANT_SERVICE_URL;
        sendRequest(url, callback);
        
        //Commented out, but used for testing
        //callback.onResponseReceived(null, new AllParticipantsResponse());
    }
    
    private void sendRequest(String url, RequestCallback callback) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        builder.setHeader("Accept", "application/json");
        try {
            builder.sendRequest(null, callback);
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
