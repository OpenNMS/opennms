package org.opennms.web.event.filter;

import java.util.Date;
import org.opennms.netmgt.EventConstants;

public class AfterDateFilter extends Object implements Filter 
{
    public static final String TYPE = "afterdate";
    protected Date date;

    public AfterDateFilter(Date date) {
        if( date == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.date = date;
    }
        
    public AfterDateFilter(long epochTime) {
        this(new Date(epochTime));
    }

    public String getSql() {
        return( " EVENTTIME > to_timestamp(\'" + this.date.toString() + "\'," + EventConstants.POSTGRES_DATE_FORMAT+ ")" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.date.getTime() );
    }
    
    public String getTextDescription(){
        return( "date after \"" + this.date.toString() + "\"" );
    }

    public String toString() {
        return( "<AfterTimeFilter: " + this.getDescription() + ">" );
    }

    public Date getDate() {
        return( this.date );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

