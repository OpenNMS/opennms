package org.opennms.web.element;

import java.util.*;


public class ElementUtil extends Object
{
    /** 
     * Do not use directly, call {@link #getInterfaceStatusMap 
     * getInterfaceStatusMap} instead. 
     */
    protected static HashMap interfaceStatusMap;

    /** 
     * Do not use directly, call {@link #getServiceStatusMap 
     * getServiceStatusMap} instead. z
     */
    protected static HashMap serviceStatusMap;
    
    
    /** Returns the interface status map, initializing a new one if necessary. */
    protected static Map getInterfaceStatusMap() {
        if( interfaceStatusMap == null ) {
            synchronized( ElementUtil.class ) {
                interfaceStatusMap = new HashMap();
                
                interfaceStatusMap = new HashMap();
                interfaceStatusMap.put( new Character('M'), "Managed" );
                interfaceStatusMap.put( new Character('U'), "Unmanaged" );
                interfaceStatusMap.put( new Character('D'), "Deleted" );
                interfaceStatusMap.put( new Character('F'), "Forced Unmanaged" );
                interfaceStatusMap.put( new Character('N'), "Not Polled" );                
            }
        }        
        
        return( interfaceStatusMap );
    }
    

    /** Return the human-readable name for a interface's status, may be null. */    
    public static String getInterfaceStatusString(Interface intf) {
        if( intf == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return getInterfaceStatusString(intf.isManagedChar());
    }    
    

    /** Return the human-readable name for a interface status character, may be null. */    
    public static String getInterfaceStatusString( char c ) {
        Map statusMap = getInterfaceStatusMap();
        return (String)statusMap.get(new Character(c));
    }    

    
    /** Returns the service status map, initializing a new one if necessary. */
    protected static Map getServiceStatusMap() {
        if( serviceStatusMap == null ) {
            synchronized( ElementUtil.class ) {
                serviceStatusMap = new HashMap();
                
                serviceStatusMap.put( new Character('A'), "Managed" );
                serviceStatusMap.put( new Character('U'), "Unmanaged" );
                serviceStatusMap.put( new Character('D'), "Deleted" );
                serviceStatusMap.put( new Character('F'), "Forced Unmanaged" );
                serviceStatusMap.put( new Character('N'), "Not Polled" );                
            }
        }        
        
        return( serviceStatusMap );
    }
    

    /** Return the human-readable name for a service's status, may be null. */    
    public static String getServiceStatusString(Service svc) {
        if( svc == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return getServiceStatusString(svc.getStatus());
    }    
    

    /** Return the human-readable name for a service status character, may be null. */    
    public static String getServiceStatusString( char c ) {
        Map statusMap = getServiceStatusMap();
        return (String)statusMap.get(new Character(c));
    }    

    
    public static final int DEFAULT_TRUNCATE_THRESHOLD = 28;
    
    
    public static String truncateLabel(String label) {
        return truncateLabel(label, DEFAULT_TRUNCATE_THRESHOLD);
    }

    
    public static String truncateLabel(String label, int truncateThreshold) {
        if(label == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if(truncateThreshold < 3) {
            throw new IllegalArgumentException("Cannot take a truncate position less than 3.");
        }

        String shortLabel = label;

        if(label.length() > truncateThreshold) {
            shortLabel = label.substring(0, truncateThreshold-3) + "...";                        
        }

        return shortLabel;
    }    
    
    
    /** Private constructor so this class cannot be instantiated. */
    private ElementUtil() {}
            
}
