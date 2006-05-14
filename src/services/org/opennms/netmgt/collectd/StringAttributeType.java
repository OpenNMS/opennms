package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.opennms.netmgt.snmp.SnmpValue;

public class StringAttributeType extends AttributeType {
    
    public static boolean supportsType(String rawType) {
        return rawType.toLowerCase().startsWith("string");
    }
    
    public StringAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj) {
        super(resourceType, collectionName, mibObj);
    }

    protected boolean performUpdate(RrdRepository repository, Attribute attribute) {

        CollectionResource resource = attribute.getResource();
        SnmpValue value = attribute.getValue();
        
        File resourceDir = resource.getResourceDir(repository);
        
        String val = (value == null ? null : value.toString());
        Properties props = new Properties();
        File propertiesFile =	 new File(resourceDir,"strings.properties");
        
        FileInputStream fileInputStream = null;
        //Preload existing data
        if (propertiesFile.exists()) {
            try {
                fileInputStream = new FileInputStream(propertiesFile);
                props.load(fileInputStream);
            } catch (Exception e) {
                log().error("performUpdate: Error openning properties file.", e);
                return true;
            } finally {
                try {
                    if (fileInputStream != null) fileInputStream.close();
                } catch (IOException e) {
                    log().error("performUpdate: Error closing file.", e);
                }
            }
        }
        props.setProperty(getName(), val);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(propertiesFile);
            props.store(fileOutputStream, null);
        } catch (Exception e) {
            //Ouch, something went wrong that we should mention to the outside world
            e.printStackTrace();
            return true;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                log().error("performUpdate: Error closing file.", e);
            }
        }
        return false;
    }

}
