package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <p>Abstract AbstractThresholdEvaluatorState class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractThresholdEvaluatorState implements ThresholdEvaluatorState {

    private static final String UNKNOWN = "Unknown";

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
            resource = new CollectionResourceWrapper(0, 0, null, null, null, null, null);
        }
        String dsLabelValue = resource.getLabelValue(resource.getLabel());
        if (dsLabelValue == null) dsLabelValue = UNKNOWN;

        // create the event to be sent
        EventBuilder bldr = new EventBuilder(uei, "OpenNMS.Threshd." + getThresholdConfig().getDatasourceExpression(), date);

        bldr.setNodeid(resource.getNodeId());
        bldr.setService(resource.getServiceName());

        // As a suggestion from Bug2711. Host Address will contain Interface IP Address for Interface Resource
        bldr.setInterface(resource.getHostAddress());            

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

        // Set resource label
        bldr.addParam("label", dsLabelValue);

        // Set event host
        try {
            bldr.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            bldr.setHost("unresolved.host");
            log().warn("Failed to resolve local hostname: " + e, e);
        }

        // Add datasource name
        bldr.addParam("ds", getThresholdConfig().getDatasourceExpression());
        
        // Add last known value of the datasource fetched from its RRD file
        bldr.addParam("value", formatValue(dsValue));

        // Add the instance name of the resource in question
        bldr.addParam("instance", resource.getInstance() != null ? resource.getInstance() : "null");

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
    protected String formatValue(Double value) {
        String pattern = System.getProperty("org.opennms.threshd.value.decimalformat", "###.##");
        DecimalFormat valueFormatter = new DecimalFormat(pattern);
        return valueFormatter.format(value);
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected final ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
