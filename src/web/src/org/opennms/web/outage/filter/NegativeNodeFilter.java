package org.opennms.web.outage.filter;

import java.sql.SQLException;
import org.opennms.web.element.NetworkElementFactory;


/** Encapsulates all node filtering functionality. */
public class NegativeNodeFilter extends Object implements Filter 
{
    public static final String TYPE = "nodenot";
    protected int nodeId;

    public NegativeNodeFilter( int nodeId ) {
        this.nodeId = nodeId;
    }

    public String getSql() {
        return( " (OUTAGES.NODEID<>" + this.nodeId + " OR OUTAGES.NODEID IS NULL)" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.nodeId );
    }

    public String getTextDescription() {
        String nodeName = Integer.toString(this.nodeId);
        try {
            nodeName = NetworkElementFactory.getNodeLabel(this.nodeId);
        } catch (SQLException e) {}
        
        return( "node is not " + nodeName );
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


