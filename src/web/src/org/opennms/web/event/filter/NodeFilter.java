package org.opennms.web.event.filter;

import java.sql.SQLException;
import org.opennms.web.element.NetworkElementFactory;


/** Encapsulates all node filtering functionality. */
public class NodeFilter extends Object implements Filter {
    public static final String TYPE = "node";
    protected int nodeId;

    public NodeFilter( int nodeId ) {
        this.nodeId = nodeId;
    }

    public String getSql() {
        return( " NODEID=" + this.nodeId );
    }

    public String getDescription() {
        return( TYPE + "=" + this.nodeId );
    }
    
    public String getTextDescription(){
        String nodeName = Integer.toString( this.nodeId );
        try {
            nodeName = NetworkElementFactory.getNodeLabel( this.nodeId );
        } catch (SQLException e) {}
        
        return( TYPE + "=" + nodeName );
    }

    public String toString() {
        return( "<EventFactory.NodeFilter: " + this.getDescription() + ">" );
    }

    public int getNodeId() {
        return( this.nodeId );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}
