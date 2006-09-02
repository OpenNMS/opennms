package org.opennms.web.graph;

import java.sql.SQLException;

public interface GraphModel {
    public PrefabGraph getQuery(String report);

    public String getHumanReadableNameForIfLabel(int nodeId, String intf) throws SQLException;

//    public boolean encodeNodeIdInRRDParm();
    public String getRelativePathForAttribute(String resourceType,
            String resourceParent, String resource, String attribute);

    public String getType();
}
