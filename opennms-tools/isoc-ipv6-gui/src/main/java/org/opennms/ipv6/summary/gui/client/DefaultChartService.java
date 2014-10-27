/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    }

    @Override
    public void getAvailabilityByLocation(String location, RequestCallback callback) {
        String url = AVAILABILITY_SERVICE_URL + "/" + location;
        sendRequest(url, callback);
    }

    

    @Override
    public void getAvailabilityByParticipant(String participant, RequestCallback callback) {
        String url = AVAILABILITY_SERVICE_URL + "/?host=" + participant;
        sendRequest(url, callback);
        
    }

    @Override
    public void getAvailabilityByLocationAndParticipant(String location,String participant, RequestCallback callback) {
        String url = AVAILABILITY_SERVICE_URL + "/" + location + "/?host=" + participant;
        sendRequest(url, callback);
        
    }

    @Override
    public void getAllLocations(RequestCallback callback) {
        String url = LOCATION_LIST_SERVICE_URL;
        sendRequest(url, callback);
        
    }

    @Override
    public void getAllParticipants(RequestCallback callback) {
        String url = PARTICIPANT_SERVICE_URL;
        sendRequest(url, callback);
        
    }
    
    private void sendRequest(String url, RequestCallback callback) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        builder.setHeader("Accept", "application/json");
        
        builder.setUser("ipv6Rest");
        builder.setPassword("ipv6Rest");
        try {
            builder.sendRequest(null, callback);
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
