package org.opennms.web.outage.filter;

import java.sql.SQLException;
import org.opennms.web.element.NetworkElementFactory;


/** Encapsulates all service filtering functionality. */
public class ServiceFilter extends Object implements Filter 
{
    public static final String TYPE = "service";
    protected int serviceId;

    public ServiceFilter( int serviceId ) {
        this.serviceId = serviceId;
    }

    public String getSql() {
        return( " OUTAGES.SERVICEID=" + this.serviceId );
    }

    public String getDescription() {
        return( TYPE + "=" + this.serviceId);
    }

    public String getTextDescription(){
        String serviceName = Integer.toString( this.serviceId );

        try {
            serviceName = NetworkElementFactory.getServiceNameFromId( this.serviceId );
        } 
        catch (SQLException e) {            
            throw new IllegalStateException("Could not get the service name for id " + this.serviceId); 
        }
        
        return( TYPE + " is " + serviceName);
    }

    public String toString() {
        return( "<OutageFactory.ServiceFilter: " + this.getDescription() + ">" );
    }

    public int getServiceId() {
        return( this.serviceId );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

