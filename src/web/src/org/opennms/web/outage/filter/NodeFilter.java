package org.opennms.web.outage.filter;

import java.sql.SQLException;
import org.opennms.web.element.NetworkElementFactory;


/** Encapsulates all node filtering functionality. */
public class NodeFilter extends Object implements Filter 
{
    public static final String TYPE = "node";
    protected int nodeId;

    public NodeFilter( int nodeId ) {
        this.nodeId = nodeId;
    }

    public String getSql() {
        return( " OUTAGES.NODEID=" + this.nodeId );
    }

    public String getDescription() {
        return( TYPE + "=" + this.nodeId );
    }

    public String getTextDescription() {
        String nodeName = Integer.toString(this.nodeId);
        try {
            nodeName = NetworkElementFactory.getNodeLabel(this.nodeId);
        } catch (SQLException e) {}
        
        return( "node is " + nodeName );        
    }

    public String toString() {
        return( "<OutageFactory.NodeFilter: " + this.getDescription() + ">" );
    }

    public int getNode() {
        return( this.nodeId );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}


