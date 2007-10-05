package org.opennms.netmgt.threshd;

import java.util.Map;

public interface IfInfoGetter {

    public abstract Map<String, String> getIfInfoForNodeAndLabel(int nodeId,
            String ifLabel);

}