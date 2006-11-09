package org.opennms.web.svclayer.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.modelimport.Category;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.utils.TcpEventProxy;
import org.opennms.web.BeanUtils;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.opennms.web.svclayer.dao.ManualProvisioningDao;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class DefaultManualProvisioningService implements
        ManualProvisioningService {

    private ManualProvisioningDao m_provisioningDao;

    public ModelImport addCategoryToNode(String groupName, String pathToNode, String categoryName) {
        
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);
        
        Category category = new Category();
        category.setName(categoryName);
        node.addCategory(0, category);
        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    public ModelImport addInterfaceToNode(String groupName, String pathToNode,
            String ipAddr) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = BeanUtils.getPathValue(group, pathToNode, Node.class);
        
        String snmpPrimary = "P";
        if (node.getInterfaceCount() > 0)
            snmpPrimary = "S";

        Interface iface = createInterface(ipAddr, snmpPrimary);
        node.addInterface(0, iface);
        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    private Interface createInterface(String ipAddr, String snmpPrimary) {
        Interface iface = new Interface();
        iface.setIpAddr(ipAddr);
        iface.setStatus(1);
        iface.setSnmpPrimary(snmpPrimary);
        return iface;
    }

    public ModelImport addNewNodeToGroup(String groupName, String nodeLabel) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Node node = createNode(nodeLabel, String.valueOf(System.currentTimeMillis()));
        
        group.addNode(0, node);
        
        m_provisioningDao.save(groupName, group);
        return m_provisioningDao.get(groupName);
    }

    private Node createNode(String nodeLabel, String foreignId) {
        Node node = new Node();
        node.setNodeLabel(nodeLabel);
        node.setForeignId(foreignId);
        return node;
    }

    public ModelImport addServiceToInterface(String groupName, String pathToInterface, String serviceName) {
        ModelImport group = m_provisioningDao.get(groupName);
        
        Interface iface = BeanUtils.getPathValue(group, pathToInterface, Interface.class);
        
        MonitoredService monSvc = createService(serviceName);
        iface.addMonitoredService(0, monSvc);

        
        m_provisioningDao.save(groupName, group);
        
        return m_provisioningDao.get(groupName);
    }

    public ModelImport getProvisioningGroup(String name) {
        return m_provisioningDao.get(name);
    }
    
    public ModelImport saveProvisioningGroup(String groupName, ModelImport group) {
        m_provisioningDao.save(groupName, group);
        return m_provisioningDao.get(groupName);
    }

    public Collection<String> getProvisioningGroupNames() {
        return m_provisioningDao.getProvisioningGroupNames();
    }
    
    public ModelImport createProvisioningGroup(String name) {
        ModelImport group = new ModelImport();
        group.setForeignSource(name);
        
        Node node = createNode("New Node", String.valueOf(System.currentTimeMillis()));
        group.addNode(node);
        
        m_provisioningDao.save(name, group);
        return m_provisioningDao.get(name);
    }

    private MonitoredService createService(String serviceName) {
        MonitoredService svc = new MonitoredService();
        svc.setServiceName(serviceName);
        return svc;
    }


    public void importProvisioningGroup(String groupName) {
        TcpEventProxy proxy = new TcpEventProxy();
        
        String url = m_provisioningDao.getUrlForGroup(groupName);
        Assert.notNull(url, "Could not find url for group "+groupName+".  Does it exists?");
        
        EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
        bldr.addParam(EventConstants.PARM_URL, url);
        
        try {
            proxy.send(bldr.getEvent());
        } catch (EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event to import group "+groupName, e);
        }
    }

    public void setProvisioningDao(ManualProvisioningDao provisioningDao) {
        m_provisioningDao = provisioningDao;
    }
    
    private static class PropertyPath {
        private PropertyPath parent = null;
        private String propertyName;
        private String key;
        
        PropertyPath(String nestedPath) {
            String canonicalPath = PropertyAccessorUtils.canonicalPropertyName(nestedPath);
            int lastIndex = PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(canonicalPath);
            if (lastIndex < 0) {
                propertyName = PropertyAccessorUtils.getPropertyName(canonicalPath);
                key = computeKey(canonicalPath);
            } 
            else {
                parent = new PropertyPath(canonicalPath.substring(0, lastIndex));
                String lastProperty = canonicalPath.substring(lastIndex+1);
                propertyName = PropertyAccessorUtils.getPropertyName(lastProperty);
                key = computeKey(lastProperty);
            }
        }

        private String computeKey(String property) {
            int keyPrefix = property.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
            if (keyPrefix < 0)
                return "";
            
            int keySuffix = property.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
            return property.substring(keyPrefix+1, keySuffix);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(parent == null ? "" : parent.toString()+'.');
            buf.append(propertyName);
            if (key.length() > 0) {
                buf.append(PropertyAccessor.PROPERTY_KEY_PREFIX);
                buf.append(key);
                buf.append(PropertyAccessor.PROPERTY_KEY_SUFFIX);
            }
            return buf.toString();
        }

        public String getKey() {
            return key;
        }

        public PropertyPath getParent() {
            return parent;
        }

        public String getPropertyName() {
            return propertyName;
        }
        
        public Object getValue(Object root) {
            return getValue(new BeanWrapperImpl(root));
        }
        
        public Object getValue(BeanWrapper beanWrapper) {
            return beanWrapper.getPropertyValue(toString());
        }
        
    }
    
    public ModelImport deletePath(String groupName, String pathToDelete) {
        ModelImport group = m_provisioningDao.get(groupName);

        PropertyPath path = new PropertyPath(pathToDelete);
        
        Object objToDelete = path.getValue(group);
        Object parentObject = path.getParent() == null ? group : path.getParent().getValue(group);
        
        String propName = path.getPropertyName();
        String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
        String methodName = "remove"+methodSuffix;

        
        try {
            MethodUtils.invokeMethod(parentObject, methodName, new Object[] { objToDelete });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to find method "+methodName+" on object of type "+parentObject.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("unable to access property "+pathToDelete, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("an execption occurred deleting "+pathToDelete, e);
        }
        
        m_provisioningDao.save(groupName, group);
    
        return m_provisioningDao.get(groupName);
    }



}
