package org.opennms.web.svclayer.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.netmgt.dao.ExtensionManager;
import org.opennms.netmgt.provision.Policy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultForeignSourceService implements ForeignSourceService {
    
    @Autowired
    private ExtensionManager m_extensionManager;
    
    private ForeignSourceRepository m_activeForeignSourceRepository;
    private ForeignSourceRepository m_pendingForeignSourceRepository;

    public void setActiveForeignSourceRepository(ForeignSourceRepository repo) {
        m_activeForeignSourceRepository = repo;
    }
    public void setPendingForeignSourceRepository(ForeignSourceRepository repo) {
        m_pendingForeignSourceRepository = repo;
    }
    
    public ForeignSource getForeignSource(String name) {
        return m_pendingForeignSourceRepository.getForeignSource(name);
    }
    public ForeignSource saveForeignSource(String name, ForeignSource fs) {
        m_pendingForeignSourceRepository.save(fs);
        m_activeForeignSourceRepository.save(fs);
        return fs;
    }
    public ForeignSource deleteForeignSource(String name) {
        m_pendingForeignSourceRepository.delete(m_pendingForeignSourceRepository.getForeignSource(name));
        m_activeForeignSourceRepository.delete(m_activeForeignSourceRepository.getForeignSource(name));
        return m_activeForeignSourceRepository.getForeignSource(name);
    }

    public ForeignSource deletePath(String foreignSourceName, String pathToDelete) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(foreignSourceName);
        PropertyPath path = new PropertyPath(pathToDelete);
        
        Object objToDelete = path.getValue(fs);
        Object parentObject = path.getParent() == null ? fs : path.getParent().getValue(fs);
        
        String propName = path.getPropertyName();
        String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
        String methodName = "remove"+methodSuffix;
        
        try {
            MethodUtils.invokeMethod(parentObject, methodName, new Object[] { objToDelete });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to find method "+methodName+" on object of type "+parentObject.getClass()+" with argument " + objToDelete, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("unable to access property "+pathToDelete, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("an execption occurred deleting "+pathToDelete, e);
        }

        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }


    public ForeignSource addDetectorToForeignSource(String foreignSource, String name) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(foreignSource);
        PluginConfig pc = new PluginConfig(name, "unknown");
        fs.addDetector(pc);
        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }
    public ForeignSource deleteDetector(String foreignSource, String name) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(foreignSource);
        List<PluginConfig> detectors = fs.getDetectors();
        for (Iterator<PluginConfig> i = detectors.iterator(); i.hasNext(); ) {
            PluginConfig pc = i.next();
            if (pc.getName().equals(name)) {
                i.remove();
                break;
            }
        }
        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }
    
    public ForeignSource addPolicyToForeignSource(String foreignSource, String name) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(foreignSource);
        PluginConfig pc = new PluginConfig(name, "unknown");
        fs.addPolicy(pc);
        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }
    public ForeignSource deletePolicy(String foreignSource, String name) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(foreignSource);
        List<PluginConfig> policies = fs.getPolicies();
        for (Iterator<PluginConfig> i = policies.iterator(); i.hasNext(); ) {
            PluginConfig pc = i.next();
            if (pc.getName().equals(name)) {
                i.remove();
                break;
            }
        }
        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }

    public Map<String, String> getDetectorTypes() {
        Map<String,String> detectors = new TreeMap<String,String>();

        for (ServiceDetector d : m_extensionManager.findExtensions(ServiceDetector.class)) {
            String serviceName = d.getServiceName();
            if (serviceName == null) {
                serviceName = d.getClass().getSimpleName();
            }
            detectors.put(d.getClass().getName(), serviceName);
        }
        
        return detectors;
    }
    public Map<String, String> getPolicyTypes() {
        Map<String,String> policies = new TreeMap<String,String>();
        for (Policy d : m_extensionManager.findExtensions(Policy.class)) {
            policies.put(d.getClass().getName(), d.getClass().getSimpleName());
        }
        return policies;
    }
}
