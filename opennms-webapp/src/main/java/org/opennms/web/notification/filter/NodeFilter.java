package org.opennms.web.notification.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates all node filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="node"</code> */
    public static final String TYPE = "node";

    //protected int nodeId;

    /**
     * <p>Constructor for NodeFilter.</p>
     *
     * @param nodeId a int.
     */
    public NodeFilter(int nodeId) {
        super(TYPE, SQLType.INT, "NODEID", "node.id", nodeId);
        //this.nodeId = nodeId;
    }

//    public String getSql() {
//        return (" NODEID=" + this.nodeId);
//    }
//    
//    public String getParamSql() {
//        return (" NODEID=?");
//    }
//    
//    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setInt(parameterIndex, this.nodeId);
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.nodeId);
//    }
//
//    public String getTextDescription() {
//        String nodeName = Integer.toString(this.nodeId);
//        try {
//            nodeName = NetworkElementFactory.getNodeLabel(this.nodeId);
//        } catch (SQLException e) {
//        }
//
//        return (TYPE + "=" + nodeName);
//    }
//
//    public String toString() {
//        return ("<NoticeFactory.NodeFilter: " + this.getDescription() + ">");
//    }
//
//    public int getNodeId() {
//        return (this.nodeId);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}
