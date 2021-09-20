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

package org.opennms.isoc.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("remotelocations")
public class HelloWorldResource {
    String m_availString = "{\"data\":[{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306469100000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306469400000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306469700000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306470000000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306470300000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306470600000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306470900000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306471200000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306471500000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306471800000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306472100000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306472400000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306472700000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306473000000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306473300000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306473600000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306473900000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306474200000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306474500000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306474800000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306475100000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306475400000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306475700000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306476000000\"},{\"values\":[{\"application\":\"HTTP-v4\",\"availability\":\"0.000\"},{\"application\":\"HTTP-v6\",\"availability\":\"0.000\"}],\"time\":\"1306476300000\"}]}";
    @GET
    @Produces("application/json")
    public String getLocations() {
        return "{\"locations\":{\"area\":\"raleigh\",\"coordinates\":\"35.7174,-79.1619\",\"geolocation\":\"35.7174,-79.1619\",\"name\":\"RDU\",\"pollingPackageName\":\"raleigh\",\"priority\":\"100\"}}";
    }
    
    @GET
    @Produces("application/json")
    @Path("{location}")
    public String getAvailByLocation(@PathParam("{location}") String location) {
        
        return m_availString;
    }
    
    @GET
    @Produces("application/json")
    @Path("participants")
    public String getParticipants() {
        return "{\"participants\":[{\"name\":\"alternate-node1\"},{\"name\":\"alternate-node2\"},{\"name\":\"node1\"},{\"name\":\"node2\"},{\"name\":\"node3\"},{\"name\":\"node4\"}]}";
    }
    
    @GET
    @Produces("application/json")
    @Path("availability")
    public String getAvailability() {
        return m_availString;
    }
}
