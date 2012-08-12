/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.nrtg.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Defines a collection job for a satellite.
 * </p>
 * <p>
 * The smallest sensible job unit to collect in one action.
 * </p>
 * <p>
 * A set of metrics from one interface in one technology at one time.
 * </p>
 * <p>
 * The satellite calls the responsible protocol handler. The protocol handler
 * can optimize the collection of metrics depending on protocol specifics
 * (one/multiple connection, one/multiple session, one/multiple call ...).
 * </p>
 *
 * @author Simon Walter
 */
public interface CollectionJob extends Serializable {

    public Set<String> getAllMetrics();

    void setMetricValue(String metricId, String value) throws IllegalArgumentException;

    String getMetricValue(String metricId) throws IllegalArgumentException;

    void addMetric(String metricId, Set<String> destinationSet) throws IllegalArgumentException;

    void setParameters(HashMap<String, Object> parameters);

    HashMap<String, Object> getParameters();

    public MeasurementSet getMeasurementSet();

    void setId(String id);

    public String getId();

    void setNodeId(int nodeId);

    int getNodeId();

    void setSite(String site);

    String getSite();

    void setCreationTimestamp(Date creationTimestamp);

    Date getCreationTimestamp();

    void setFinishedTimestamp(Date finishedTimestamp);

    Date getFinishedTimestamp();

    public void setNetInterface(String theInterface);

    //ToDo tak change to InetAddress
    public String getNetInterface();

    public String getService();

    void setService(String service);

    public Map<String, MeasurementSet> getMeasurementSetsByDestination();

	public void setProtocolConfiguration(String configurationString);
	
	public String getProtocolConfiguration();

}
