package org.opennms.features.topology.plugins.ncs;

import org.opennms.netmgt.model.ncs.NCSComponent;

public class NCSRootServiceItem extends NCSServiceItem {

    public NCSRootServiceItem(NCSComponent ncsComponent) {
        super(ncsComponent);
        setRoot(true);
        setId((long) -(Math.random() * 10000));
        setChildrenAllowed(true);
        setName(computerName(ncsComponent.getForeignSource()));
    }

    private String computerName(String foreignSource) {
        if(foreignSource.startsWith("space_")) {
            return foreignSource.substring("space_".length(), foreignSource.length());
        }
        return foreignSource;
    }

}
