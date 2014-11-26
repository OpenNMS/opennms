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

package org.opennms.features.gwt.combobox.client.rest;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;

public class DefaultNodeService implements NodeService {
    
    public static String TEST_RESPONSE = "{" +
    		"\"@totalCount\" : \"4\"," +
    		"\"@count\" : \"4\"," +
    		"\"node\" : [ {" +
    		  "\"@type\" : \"A\"," +
    		  "\"@id\" : \"47\"," +
    		  "\"@label\" : \"www.gap.con\"," +
    		  "\"@foreignSource\" : \"gap\"," +
    		  "\"@foreignId\" : \"1300814982372\"," +
    		  "\"assetRecord\" : {" +
    		    "\"building\" : \"gap\"," +
    		    "\"category\" : \"Unspecified\"," +
    		    "\"lastModifiedBy\" : \"                    \"," +
    		    "\"lastModifiedDate\" : \"2011-03-22T13:37:38.005-04:00\"," +
    		    "\"node\" : \"47\"" +
    		  "}," +
    		  "\"createTime\" : \"2011-03-22T13:37:38.005-04:00\"," +
    		  "\"labelSource\" : \"U\"," +
    		  "\"lastCapsdPoll\" : \"2011-06-05T07:31:51.867-04:00\"" +
    		"}, {" +
    		  "\"@type\" : \"A\"," +
    		  "\"@id\" : \"33\"," +
    		  "\"@label\" : \"www.hatimonline.com\"," +
    		  "\"@foreignSource\" : \"geoaxis\"," +
    		  "\"@foreignId\" : \"1234123393911\"," +
    		  "\"assetRecord\" : {" +
    		    "\"building\" : \"geoaxis\"," +
    		    "\"category\" : \"Unspecified\"," +
    		    "\"lastModifiedBy\" : \"                    \"," +
    		    "\"lastModifiedDate\" : \"2010-12-14T11:43:25.779-05:00\"," +
    		    "\"node\" : \"33\"" +
    		  "}," +
    		  "\"createTime\" : \"2010-12-14T11:22:25.755-05:00\"," +
    		  "\"labelSource\" : \"U\"," +
    		  "\"lastCapsdPoll\" : \"2011-06-05T07:31:51.910-04:00\"" +
    		"}, {" +
    		  "\"@type\" : \"D\"," +
    		  "\"@id\" : \"51\"," +
    		  "\"@label\" : \"www.me.com\"," +
    		  "\"@foreignSource\" : \"mobileMe\"," +
    		  "\"@foreignId\" : \"www.me.com\"," +
    		  "\"assetRecord\" : {" +
    		    "\"building\" : \"mobileMe\"," +
    		    "\"category\" : \"Unspecified\"," +
    		    "\"lastModifiedBy\" : \"                    \"," +
    		    "\"lastModifiedDate\" : \"2011-05-11T14:54:44.129-04:00\"," +
    		    "\"node\" : \"51\"" +
    		  "}," +
    		  "\"categories\" : {" +
    		    "\"@name\" : \"MobileMe\"," +
    		    "\"@id\" : \"8\"" +
    		  "}," +
    		  "\"createTime\" : \"2011-03-26T07:01:15.009-04:00\"," +
    		  "\"labelSource\" : \"U\"," +
    		  "\"lastCapsdPoll\" : \"2011-06-05T07:31:51.867-04:00\"" +
    		"}, {" +
    		  "\"@type\" : \"A\"," +
    		  "\"@id\" : \"27\"," +
    		  "\"@label\" : \"www.nationwide.com\"," +
    		  "\"@foreignSource\" : \"DJStuff\"," +
    		  "\"@foreignId\" : \"1200604708026\"," +
    		  "\"assetRecord\" : {" +
    		    "\"building\" : \"DJStuff\"," +
    		    "\"category\" : \"Unspecified\"," +
    		    "\"lastModifiedBy\" : \"                    \"," +
    		    "\"lastModifiedDate\" : \"2010-12-14T11:21:23.886-05:00\"," +
    		    "\"node\" : \"27\"" +
    		  "}," +
    		  "\"createTime\" : \"2010-12-14T11:21:23.886-05:00\"," +
    		  "\"labelSource\" : \"U\"," +
    		  "\"lastCapsdPoll\" : \"2011-06-05T07:31:51.912-04:00\"" +
    		  "} ]" +
    		"}";
    
    private static String BASE_URL = "rest/nodes";
    @Override
    public void getAllNodes(RequestCallback callback) {
        sendRequest(callback, BASE_URL + "?limit=0");
    }

    
    private void sendRequest(RequestCallback callback, String url) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        builder.setHeader("accept", "application/json");
        try {
            builder.sendRequest(null, callback);
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getNodeByNodeLabel(String nodeLabel, RequestCallback callback) {
        String url = BASE_URL + "?label=" + nodeLabel + "&comparator=contains&limit=0";
        sendRequest(callback, url);
    }

}
