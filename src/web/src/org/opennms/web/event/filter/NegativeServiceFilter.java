package org.opennms.web.event.filter;

import java.sql.SQLException;
import org.opennms.web.element.NetworkElementFactory;


/** Encapsulates all service filtering functionality. */
public class NegativeServiceFilter extends Object implements Filter {
    public static final String TYPE = "servicenot";
    protected int serviceId;

    public NegativeServiceFilter( int serviceId ) {
        this.serviceId = serviceId;
    }

    public String getSql() {
        return( " (SERVICEID<>" + this.serviceId + " OR SERVICEID IS NULL)" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.serviceId );
    }
    
    public String getTextDescription(){
            String serviceName = Integer.toString( this.serviceId );
        try {
            serviceName = NetworkElementFactory.getServiceNameFromId( this.serviceId );
        } catch (SQLException e) {}
        
        return( "service is not " + serviceName);
    }

    public String toString() {
        return( "<EventFactory.NegativeServiceFilter: " + this.getDescription() + ">" );
    }

    public int getServiceId() {
        return( this.serviceId );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}


