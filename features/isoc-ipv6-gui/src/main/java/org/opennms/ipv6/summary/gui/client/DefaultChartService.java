package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;

//FIXME: Make sure you handle time range
public class DefaultChartService implements ChartService {
    
    public static final String AVAILABILITY_SERVICE_URL = "/opennms/rest/remotelocations/availability";
    public static final String PARTICIPANT_SERVICE_URL = "/opennms/rest/remotelocations/participants";
    public static final String LOCATION_LIST_SERVICE_URL = "/opennms/rest/remotelocations";
    
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
