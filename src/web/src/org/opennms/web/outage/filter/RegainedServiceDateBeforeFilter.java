package org.opennms.web.outage.filter;

import java.text.DateFormat;
import java.util.Date;
import org.opennms.netmgt.EventConstants;


public class RegainedServiceDateBeforeFilter extends Object implements Filter 
{
    public static final String TYPE = "regainedbefore";
    protected static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);    
    
    protected Date date;

    public RegainedServiceDateBeforeFilter(Date date) {
        if( date == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.date = date;
    }
        
    public RegainedServiceDateBeforeFilter(long epochTime) {
        this(new Date(epochTime));
    }

    public String getSql() {
        return( " ifRegainedService < to_timestamp(\'" + this.date.toString() + "\'," + EventConstants.POSTGRES_DATE_FORMAT+ ")" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.date.getTime() );
    }
    
    public String getTextDescription(){
        return( "regained service date before \"" + DATE_FORMAT.format(this.date) + "\"" );
    }

    public String toString() {
        return( "<Regained Service Date Before Filter: " + this.getDescription() + ">" );
    }

    public Date getDate() {
        return( this.date );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

