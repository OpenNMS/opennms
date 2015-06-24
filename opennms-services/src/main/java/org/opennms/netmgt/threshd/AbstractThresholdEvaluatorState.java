/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract AbstractThresholdEvaluatorState class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractThresholdEvaluatorState implements ThresholdEvaluatorState {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractThresholdEvaluatorState.class);

    private static final String UNKNOWN = "Unknown";

    public static final String FORMATED_NAN = "NaN (the threshold definition has been changed)";

    /**
     * <p>createBasicEvent</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     * @param dsValue a double.
     * @param resource a {@link org.opennms.netmgt.threshd.CollectionResourceWrapper} object.
     * @param additionalParams a {@link java.util.Map} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    protected Event createBasicEvent(String uei, Date date, double dsValue, CollectionResourceWrapper resource, Map<String,String> additionalParams) {
        if (resource == null) { // Still works, mimic old code when instance value is null.
            resource = new CollectionResourceWrapper(date, 0, null, null, null, null, null);
        }
        String dsLabelValue = resource.getFieldValue(resource.getDsLabel());
        if (dsLabelValue == null) dsLabelValue = UNKNOWN;

        // create the event to be sent
        EventBuilder bldr = new EventBuilder(uei, "OpenNMS.Threshd." + getThresholdConfig().getDatasourceExpression(), date);

        bldr.setNodeid(resource.getNodeId());
        bldr.setService(resource.getServiceName());

        // As a suggestion from Bug2711. Host Address will contain Interface IP Address for Interface Resource
        bldr.setInterface(addr(resource.getHostAddress()));            

        if (resource.isAnInterfaceResource()) {
            // Update threshold label if it is unknown. This is useful because usually reduction-key is associated to label parameter
            if (UNKNOWN.equals(dsLabelValue))
                dsLabelValue = resource.getIfLabel();
            // Set interface specific parameters
            bldr.addParam("ifLabel", resource.getIfLabel());
            bldr.addParam("ifIndex", resource.getIfIndex());
            String ipaddr = resource.getIfInfoValue("ipaddr");
            if (ipaddr != null && !"0.0.0.0".equals(ipaddr)) {
                bldr.addParam("ifIpAddress", ipaddr);
            }
        }
        if (resource.isNodeResource() && UNKNOWN.equals(dsLabelValue)) {
            dsLabelValue = CollectionResource.RESOURCE_TYPE_NODE;
        }

        // Set resource label
        bldr.addParam("label", dsLabelValue);

        // Set event host
        bldr.setHost(InetAddressUtils.getLocalHostName());

        // Add datasource name
        bldr.addParam("ds", getThresholdConfig().getDatasourceExpression());

        // Add threshold description
        String descr = getThresholdConfig().getBasethresholddef().getDescription();
        bldr.addParam("description", descr != null && !descr.trim().equals("") ? descr : getThresholdConfig().getDatasourceExpression());

        // Add last known value of the datasource fetched from its RRD file
        bldr.addParam("value", formatValue(dsValue));

        String defaultInstance = resource.isNodeResource() ? CollectionResource.RESOURCE_TYPE_NODE : UNKNOWN;

        // Add the instance name of the resource in question
        bldr.addParam("instance", resource.getInstance() == null ? defaultInstance : resource.getInstance());

        // Add the instance label of the resource in question
        bldr.addParam("instanceLabel", resource.getInstanceLabel() == null ? defaultInstance : resource.getInstanceLabel());

        // Add the resource ID required to call the Graph API.
        bldr.addParam("resourceId",resource.getResourceId());

        // Add additional parameters
        if (additionalParams != null) {
            for (String p : additionalParams.keySet()) {
                bldr.addParam(p, additionalParams.get(p));
            }
        }

        return bldr.getEvent();
    }

    /**
     * <p>formatValue</p>
     *
     * @param value a {@link java.lang.Double} object.
     * @return a {@link java.lang.String} object.
     */
    protected String formatValue(double value) {
        if (Double.isNaN(value)) // When reconfiguring thresholds, the value is set to NaN.
            return FORMATED_NAN;
        String pattern = System.getProperty("org.opennms.threshd.value.decimalformat", "###.##");
        DecimalFormat valueFormatter = new DecimalFormat(pattern);
        return valueFormatter.format(value);
    }


}
