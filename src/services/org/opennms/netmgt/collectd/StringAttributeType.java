package org.opennms.netmgt.collectd;



public class StringAttributeType extends AttributeType {
    
    public static boolean supportsType(String rawType) {
        return rawType.toLowerCase().startsWith("string");
    }
    
    public StringAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj) {
        super(resourceType, collectionName, mibObj);
    }

    protected void storeAttribute(Attribute attribute, Persister persister) {
        persister.persistStringAttribute(attribute);
    }

}
