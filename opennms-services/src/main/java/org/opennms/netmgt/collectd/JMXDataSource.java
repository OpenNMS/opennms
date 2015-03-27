/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates an RRDTool data source. Data source information
 * parsed from the DataCollection.xml file is stored in RRDDataSource objects.
 *
 * For additional information on RRD and RRDTool see:
 * http://ee-staff.ethz.ch/~oetiker/webtools/rrdtool/
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 */
public class JMXDataSource implements Cloneable {
    private static final Logger LOG = LoggerFactory.getLogger(JMXDataSource.class);

	/** Constant <code>RRD_ERROR="RRD_ERROR"</code> */
	public static final String RRD_ERROR = "RRD_ERROR";

    /**
     * Defines the list of supported (MIB) object types which may be mapped to
     * one of the supported RRD data source types. Currently the only two
     * supported RRD data source types are: COUNTER & GAUGE. A simple string
     * comparison is performed against this list of supported types to determine
     * if an object can be represented by an RRD data source. NOTE: String
     * comparison uses String.startsWith() method so "counter32" & "counter64"
     * values will match "counter" entry. Comparison is case sensitive.
     */
    private static final String[] supportedObjectTypes = new String[] { "counter", "gauge", "timeticks", "integer", "octetstring" };

    /**
     * Index of data type in supportedObjectTypes string array.
     */
    private static final int COUNTER_INDEX = 0;

    private static final int GAUGE_INDEX = 1;

    private static final int TIMETICKS_INDEX = 2;

    private static final int INTEGER_INDEX = 3;

    private static final int OCTETSTRING_INDEX = 4;

    /**
     * RRDTool defined Data Source Types NOTE: "DERIVE" and "ABSOLUTE" not
     * currently supported.
     */
    private static final String DST_GAUGE = "GAUGE";

    private static final String DST_COUNTER = "COUNTER";

    // private static final String DST_DERIVE = "DERIVE";
    // private static final String DST_ABSOLUTE = "ABSOLUTE";

    /**
     * Data Source Type. This must be one of the available RRDTool data source
     * type values: GAUGE, COUNTER, DERIVE, or ABSOLUTE
     */
    private String m_type;

    /**
     * Data Source Heartbeat. This is the maximum number of seconds that may
     * pass between updates of this data source before the value of the data
     * source is assumed to be 'Unknown'.
     */
    private int m_heartbeat;

    /**
     * Minimum Expected Range. Together with m_max defines the expected range of
     * the data supplied by this data source. May be set to "U" for 'Unknown'.
     */
    private String m_min;

    /**
     * Maximum Expected Range. Together with m_min defines the expected range of
     * the data supplied by this data source. May be set to "U" for Unknown.
     */
    private String m_max;
    private String m_oid;
    private String m_instance;
    private String m_name;
    private String m_collectionName;

         /**
          * <p>handlesType</p>
          *
          * @param objectType MIB object type being inquired about
          * @return true if RRDDataSource can  handle the given type, false if it can't
          */
         public static boolean handlesType(String objectType) {
                 return (JMXDataSource.mapType(objectType)!=null);
         }



    /**
     * Static method which takes a MIB object type (counter, counter32,
     * octetstring, etc...) and returns the appropriate RRD data type. If the
     * object type cannot be mapped to an RRD type, null is returned. RRD only
     * supports integer data so MIB objects of type 'octetstring' are not
     * supported.
     *
     * @param objectType -
     *            MIB object type to be mapped.
     * @return RRD type string or NULL object type is not supported.
     */
    public static String mapType(String objectType) {
        String rrdType = null;

        // For consistency lower case objecType parameter before comparison
        objectType = objectType.toLowerCase();

        int index;
        for (index = 0; index < supportedObjectTypes.length; index++) {
            if (objectType.startsWith(supportedObjectTypes[index]))
                break;
        }

        switch (index) {
        // counter maps to RRD data source type COUNTER.
        case COUNTER_INDEX:
            rrdType = DST_COUNTER;
            break;
        // gauge, timeticks, and integer types all map to RRD
        // data source type GAUGE.
        case OCTETSTRING_INDEX:
        case TIMETICKS_INDEX:
        case INTEGER_INDEX:
        case GAUGE_INDEX:
            rrdType = DST_GAUGE;
            break;
        // no match, object data type is NOT supported
        default:
            rrdType = null;
            break;
        }
        return rrdType;
    }

    /**
     * Constructor
     */
    public JMXDataSource() {
	super();
        m_type = null;
        m_heartbeat = 600; // 10 minutes
        m_min = "U";
        m_max = "U";
    }

       /**
        * <p>Constructor for JMXDataSource.</p>
        *
        * @param obj a {@link org.opennms.netmgt.config.datacollection.MibObject} object.
        * @param collectionName a {@link java.lang.String} object.
        */
       public JMXDataSource(MibObject obj, String collectionName) {
                
                m_collectionName = collectionName;
                

                // Assign heartbeat using formula (2 * step) and hard code
                // min & max values to "U" ("unknown").
                this.setHeartbeat(
                        2
                                * DataCollectionConfigFactory.getInstance().getStep(
                                        collectionName));

                // Truncate MIB object name/alias if it exceeds the 19 char max for
                // RRD data source names.
                this.setName(org.opennms.netmgt.jmx.JmxUtils.trimAttributeName(this.getName()));
//                if (this.getName().length() > MAX_DS_NAME_LENGTH) {
//                        LOG.warn("buildDataSourceList: Mib object name/alias '{}' exceeds 19 char maximum for RRD data source names, truncating.", obj.getAlias());
//                        char[] temp = this.getName().toCharArray();
//                        this.setName(String.copyValueOf(temp, 0, MAX_DS_NAME_LENGTH));
//                }

                // Map MIB object data type to RRD data type
                this.setType(JMXDataSource.mapType(obj.getType()));
                this.m_min = "U";
                this.m_max = "U";

                // Assign the data source object identifier and instance
                LOG.debug("buildDataSourceList: ds_name: {} ds_oid: {}.{} ds_max: {} ds_min: {}", this.getName(), this.getOid(), this.getInstance(), this.getMax(), this.getMin());
        }


    /**
     * This method is used to assign the object's identifier.
     *
     * @param oid -
     *            object identifier in dotted decimal notation (e.g.,
     *            ".1.3.6.1.2.1.1.1")
     */
    public void setOid(String oid) {
        m_oid = oid;
    }

    /**
     * This method is used to assign the object's instance id.
     *
     * @param instance -
     *            instance identifier (to be appended to oid)
     */
    public void setInstance(String instance) {
        m_instance = instance;
    }

    /**
     * This method is used to assign the data source name.
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Returns the object's identifier.
     *
     * @return The object's identifier string.
     */
    public String getOid() {
        return m_oid;
    }

    /**
     * Returns the object's instance id.
     *
     * @return The object's instance id string.
     */
    public String getInstance() {
        return m_instance;
    }

    /**
     * Returns the object's name.
     *
     * @return The object's name.
     */
    public String getName() {
        return m_name;
    }
       

    /**
     * Class copy constructor. Constructs a new object that is an identical to
     * the passed object, however no data is shared between the two objects. Any
     * changes to one will not affect the other.
     *
     * @param second
     *            The object to make a duplicate of.
     */
    public JMXDataSource(JMXDataSource second) {
        m_oid = second.m_oid;
        m_instance = second.m_instance;
        m_name = second.m_name;
        m_type = second.m_type;
        m_heartbeat = second.m_heartbeat;
        m_min = second.m_min;
        m_max = second.m_max;
    }

    /**
     * This method is used to assign the object's expected data type.
     *
     * @param type -
     *            object's data type
     */
    public void setType(String type) {
        m_type = type;
    }

    /**
     * <p>setHeartbeat</p>
     *
     * @param heartbeat a int.
     */
    public void setHeartbeat(int heartbeat) {
        m_heartbeat = heartbeat;
    }

    /**
     * <p>setMin</p>
     *
     * @param minimum a {@link java.lang.String} object.
     */
    public void setMin(String minimum) {
        m_min = minimum;
    }

    /**
     * <p>setMax</p>
     *
     * @param maximum a {@link java.lang.String} object.
     */
    public void setMax(String maximum) {
        m_max = maximum;
    }

    /**
     * Returns the object's data type.
     *
     * @return The object's data type
     */
    public String getType() {
        return m_type;
    }

    /**
     * <p>getHeartbeat</p>
     *
     * @return a int.
     */
    public int getHeartbeat() {
        return m_heartbeat;
    }

    /**
     * <p>getMin</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMin() {
        return m_min;
    }

    /**
     * <p>getMax</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMax() {
        return m_max;
    }

    /**
     * {@inheritDoc}
     *
     * Used to get a duplicate of self. The duplicate is identical to self but
     * shares no common data.
     */
    @Override
    public Object clone() {
        return new JMXDataSource(this);
    }

    /**
     * This method is responsible for returning a String object which represents
     * the content of this RRDDataSource object. Primarily used for debugging
     * purposes.
     *
     * @return String which represents the content of this RRDDataSource object
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        // Build the buffer
        buffer.append("\n   oid:       ").append(m_oid);
        buffer.append("\n   name:      ").append(m_name);
        buffer.append("\n   type:      ").append(m_type);
        buffer.append("\n   heartbeat: ").append(m_heartbeat);
        buffer.append("\n   min:       ").append(m_min);
        buffer.append("\n   max:       ").append(m_max);

        return buffer.toString();
    }
}
