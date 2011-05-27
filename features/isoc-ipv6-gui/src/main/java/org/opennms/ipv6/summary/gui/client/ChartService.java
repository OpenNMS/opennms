package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.http.client.RequestCallback;

public interface ChartService {

    public void getAllLocationsAvailability(RequestCallback callback);
    public void getAvailabilityByLocation(String location, RequestCallback callback);
    public void getAvailabilityByParticipant(String participant, RequestCallback callback);
    public void getAvailabilityByLocationAndParticipant(String location, String participant, RequestCallback callback);
    public void getAllLocations(RequestCallback callback);
    public void getAllParticipants(RequestCallback callback);
}
