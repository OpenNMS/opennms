package org.opennms.netmgt.collectd;


import org.apache.log4j.Priority;

public class NumericAttributeType extends AttributeType {
    
    private static String[] s_supportedTypes = new String[] { "counter", "gauge", "timeticks", "integer", "octetstring" };
    
    public static boolean supportsType(String rawType) {
        String type = rawType.toLowerCase();
        for (int i = 0; i < s_supportedTypes.length; i++) {
            String supportedType = s_supportedTypes[i];
            if (type.startsWith(supportedType))
                return true;
        }
        return false;
    }



    /**
     * RRDTool defined Data Source Types NOTE: "DERIVE" and "ABSOLUTE" not
     * currently supported.
     */
    static final String DST_GAUGE = "GAUGE";
    static final String DST_COUNTER = "COUNTER";
    public static final int MAX_DS_NAME_LENGTH = 19;
    
    

    public NumericAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj) {
        super(resourceType, collectionName, mibObj);
        
            // Assign the data source object identifier and instance
            if (log().isDebugEnabled()) {
                log().debug(
                        "buildDataSourceList: ds_name: "+ getName()
                        + " ds_oid: " + getOid()
                        + "." + getInstance());
            }
            
            if (getAlias().length() > NumericAttributeType.MAX_DS_NAME_LENGTH) {
                logNameTooLong();
            }


    }
    
    protected void storeAttribute(Attribute attribute, Persister persister) {
        persister.persistNumericAttribute(attribute);
    }

    void logNameTooLong() {

       if (log().isEnabledFor(Priority.WARN))
           log().warn(
                   "buildDataSourceList: Mib object name/alias '"
                   + getAlias()
                   + "' exceeds 19 char maximum for RRD data source names, truncatin g.");
   }

/**
 * Static method which takes a MIB object type (counter, counter32,
 * octetstring, etc...) and returns the appropriate RRD data type. If the
 * object type cannot be mapped to an RRD type, null is returned. RRD only
 * supports integer data so MIB objects of type 'octetstring' are not
 * supported.
 * 
 * @param objectType -
 *            MIB object type to be mapped.
 * 
 * @return RRD type string or NULL object type is not supported.
 */
public static String mapType(String objectType) {
    if (objectType.toLowerCase().startsWith("counter"))
        return DST_COUNTER;
    
    return DST_GAUGE;
}


}
