package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.snmp.SnmpValue;

public class GroupPersister extends AbstractCollectionSetVisitor implements Persister {

    private ServiceParameters m_params;
    private RrdRepository m_repository;
    
    private LinkedList stack = new LinkedList();
    
    public GroupPersister(ServiceParameters params) {
        m_params = params;
        m_repository = new RrdRepository(params.getCollectionName());

    }
    
    public void visitResource(CollectionResource resource) {
        push(resource.shouldPersist(m_params));
    }

    public void completeResource(CollectionResource resource) {
        pop();
    }
    
    public void visitGroup(AttributeGroup group) {
        boolean shouldPersist = top() && group.shouldPersist(m_params);
        push(shouldPersist);
        
        
    }

    public void completeGroup(AttributeGroup group) {
        pop();
    }

    public void visitAttribute(Attribute attribute) {
        if (top() && attribute.shouldPersist(m_params))
            attribute.storeAttribute(this);
    }

    private void push(boolean b) {
        stack.addLast(Boolean.valueOf(b));
    }
    
    private boolean top() {
        return ((Boolean)stack.getLast()).booleanValue();
    }
    
    private boolean pop() {
        boolean top = top();
        stack.removeLast();
        return top;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.Persister#persistNumericAttribute(org.opennms.netmgt.collectd.Attribute)
     */
    public void persistNumericAttribute(Attribute attribute) {
        RrdRepository repository = m_repository;
        CollectionResource resource = attribute.getResource();
        SnmpValue value = attribute.getValue();
        String owner = resource.getCollectionAgent().getHostAddress();
        AttributeType attrType = attribute.getAttributeType();
        File resourceDir = resource.getResourceDir(repository);
    
        String val = (value == null ? null : Long.toString(value.toLong()));
        if (val == null) {
            log().info("No data collected for attribute "+attribute+". Skipping");
            return;
        }
    
        int step = repository.getStep();
        List rraList = repository.getRraList();
    
        try {
            int heartBeat = repository.getHeartBeat();
            String name = attrType.getName();
            String type = attrType.getType();
            // FIXME: pull these values from config file
            String min = "U";
            String max = "U";
            String truncated = StringUtils.truncate(name, NumericAttributeType.MAX_DS_NAME_LENGTH);
            RrdUtils.createRRD(owner, resourceDir.getAbsolutePath(), truncated, step, NumericAttributeType.mapType(type), heartBeat, min, max, rraList);
    
            RrdUtils.updateRRD(owner, resourceDir.getAbsolutePath(), truncated, val);
        } catch (Throwable e) {
            log().error("Unable to persist data for attribute "+attribute, e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.Persister#persistStringAttribute(org.opennms.netmgt.collectd.Attribute)
     */
    public void persistStringAttribute(Attribute attribute) {
        try {
            RrdRepository repository = m_repository;
            CollectionResource resource = attribute.getResource();
            SnmpValue value = attribute.getValue();

            File resourceDir = resource.getResourceDir(repository);

            String val = (value == null ? null : value.toString());
            if (val == null) {
                log().info("No data collected for attribute "+attribute+". Skipping");
                return;
            }
            File propertiesFile = new File(resourceDir,"strings.properties");
            Properties props = getCurrentProperties(propertiesFile);
            props.setProperty(attribute.getName(), val);
            saveUpdatedProperties(propertiesFile, props);
        } catch(IOException e) {
            log().error("Unable to save string attribute "+attribute, e);
        }
    }

    private void saveUpdatedProperties(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(propertiesFile);
            props.store(fileOutputStream, null);
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
    }

    private Properties getCurrentProperties(File propertiesFile) throws FileNotFoundException, IOException {
        Properties props = new Properties();

        FileInputStream fileInputStream = null;
        //Preload existing data
        if (propertiesFile.exists()) {
            try {
                fileInputStream = new FileInputStream(propertiesFile);
                props.load(fileInputStream);
            } finally {
                try {
                    if (fileInputStream != null) fileInputStream.close();
                } catch (IOException e) {
                    log().error("performUpdate: Error closing file.", e);
                }
            }
        }
        return props;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }



}
