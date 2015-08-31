package org.opennms.netmgt.jasper.helper;

import com.google.common.base.Strings;
import org.opennms.core.utils.RrdLabelUtils;

/**
 * Created by mvrueden on 24/08/15.
 */
// TODO MVR ...
public abstract class MeasurementsHelper {

    private MeasurementsHelper() {

    }

    public static String getInterfaceDescriptor(String snmpifname, String snmpifdescr, String snmpphysaddr) {
        return RrdLabelUtils.computeLabelForRRD(snmpifname, snmpifdescr, snmpphysaddr);
    }

    public static String getNodeOrNodeSourceDescriptor(String nodeId, String foreignSource, String foreignId) {
        if (!Strings.isNullOrEmpty(foreignSource) && !Strings.isNullOrEmpty(foreignId)) {
            return String.format("nodeSource[%s:%s]", foreignSource, foreignId);
        }
        return String.format("node[%s]", nodeId);
    }
}
