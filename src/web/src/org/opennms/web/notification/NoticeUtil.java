package org.opennms.web.notification;

import java.util.HashMap;
import java.util.StringTokenizer;

public abstract class NoticeUtil extends Object
{
    protected static final HashMap sortStyles;
    protected static final HashMap ackTypes;

    static
    {
        sortStyles = new java.util.HashMap();
        sortStyles.put( "user",         NoticeFactory.SortStyle.USER );
	sortStyles.put( "responder",    NoticeFactory.SortStyle.RESPONDER );
        sortStyles.put( "pagetime",     NoticeFactory.SortStyle.PAGETIME );
	sortStyles.put( "respondtime",  NoticeFactory.SortStyle.RESPONDTIME );
        sortStyles.put( "node",         NoticeFactory.SortStyle.NODE );
        sortStyles.put( "interface",    NoticeFactory.SortStyle.INTERFACE );
        sortStyles.put( "service",      NoticeFactory.SortStyle.SERVICE );
        sortStyles.put( "id",           NoticeFactory.SortStyle.ID );
	sortStyles.put( "rev_user",         NoticeFactory.SortStyle.REVERSE_USER );
	sortStyles.put( "rev_responder",    NoticeFactory.SortStyle.REVERSE_RESPONDER );
        sortStyles.put( "rev_pagetime",     NoticeFactory.SortStyle.REVERSE_PAGETIME );
	sortStyles.put( "rev_respondtime",  NoticeFactory.SortStyle.REVERSE_RESPONDTIME );
        sortStyles.put( "rev_node",         NoticeFactory.SortStyle.REVERSE_NODE );
        sortStyles.put( "rev_interface",    NoticeFactory.SortStyle.REVERSE_INTERFACE );
        sortStyles.put( "rev_service",      NoticeFactory.SortStyle.REVERSE_SERVICE );
        sortStyles.put( "rev_id",           NoticeFactory.SortStyle.REVERSE_ID );
	sortStyles.put( NoticeFactory.SortStyle.USER, "user" );
	sortStyles.put( NoticeFactory.SortStyle.RESPONDER, "responder" );
        sortStyles.put( NoticeFactory.SortStyle.PAGETIME, "pagetime" );
	sortStyles.put( NoticeFactory.SortStyle.RESPONDTIME, "respondtime" );
        sortStyles.put( NoticeFactory.SortStyle.NODE, "node" );
        sortStyles.put( NoticeFactory.SortStyle.INTERFACE, "interface" );
        sortStyles.put( NoticeFactory.SortStyle.SERVICE, "service" );
        sortStyles.put( NoticeFactory.SortStyle.ID, "id" );
	sortStyles.put( NoticeFactory.SortStyle.REVERSE_USER, "rev_user" );
	sortStyles.put( NoticeFactory.SortStyle.REVERSE_RESPONDER, "rev_responder" );
        sortStyles.put( NoticeFactory.SortStyle.REVERSE_PAGETIME, "rev_pagetime" );
	sortStyles.put( NoticeFactory.SortStyle.REVERSE_RESPONDTIME, "rev_respondtime" );
        sortStyles.put( NoticeFactory.SortStyle.REVERSE_NODE, "rev_node" );
        sortStyles.put( NoticeFactory.SortStyle.REVERSE_INTERFACE, "rev_interface" );
        sortStyles.put( NoticeFactory.SortStyle.REVERSE_SERVICE, "rev_service" );
        sortStyles.put( NoticeFactory.SortStyle.REVERSE_ID, "rev_id" );
	
        ackTypes = new java.util.HashMap();
        ackTypes.put( "ack",   NoticeFactory.AcknowledgeType.ACKNOWLEDGED );
        ackTypes.put( "unack", NoticeFactory.AcknowledgeType.UNACKNOWLEDGED );
        ackTypes.put( NoticeFactory.AcknowledgeType.ACKNOWLEDGED, "ack" );
        ackTypes.put( NoticeFactory.AcknowledgeType.UNACKNOWLEDGED, "unack" );
  }


    public static NoticeFactory.SortStyle getSortStyle( String sortStyleString ) {
        if( sortStyleString == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        return (NoticeFactory.SortStyle)sortStyles.get( sortStyleString.toLowerCase() ); 
    }


    public static String getSortStyleString( NoticeFactory.SortStyle sortStyle ) {
        if( sortStyle == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        return (String)sortStyles.get( sortStyle );
    }


    public static NoticeFactory.AcknowledgeType getAcknowledgeType( String ackTypeString ) {
        if( ackTypeString == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        return (NoticeFactory.AcknowledgeType)ackTypes.get( ackTypeString.toLowerCase() );
    }


    public static String getAcknowledgeTypeString( NoticeFactory.AcknowledgeType ackType ) {
        if( ackType == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        return (String)ackTypes.get( ackType );
    }


    public static NoticeFactory.Filter getFilter( String filterString ) {
        if( filterString == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        NoticeFactory.Filter filter = null;

        StringTokenizer tokens = new StringTokenizer( filterString, "=" );
        String type  = tokens.nextToken();
        String value = tokens.nextToken();

	if ( type.equals( NoticeFactory.UserFilter.TYPE )) {
	    filter = new NoticeFactory.UserFilter( value );
	}
	else if ( type.equals ( NoticeFactory.ResponderFilter.TYPE )) {
	    filter = new NoticeFactory.ResponderFilter( value );
	}
        else if( type.equals( NoticeFactory.NodeFilter.TYPE )) {
            filter = new NoticeFactory.NodeFilter( Integer.parseInt( value ));
        }
        else if( type.equals( NoticeFactory.InterfaceFilter.TYPE )) {
            filter = new NoticeFactory.InterfaceFilter( value );
        }
        else if( type.equals( NoticeFactory.ServiceFilter.TYPE )) {
            filter = new NoticeFactory.ServiceFilter( Integer.parseInt( value ));
        }
        
        return( filter );
    }


    public static String getFilterString( NoticeFactory.Filter filter ) {
        if( filter == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        return( filter.getDescription() );
    }

}

