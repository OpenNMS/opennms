/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.PropertyPath;
import org.opennms.netmgt.provision.OnmsPolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.support.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <p>DefaultForeignSourceService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultForeignSourceService implements ForeignSourceService, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultForeignSourceService.class);
    
    @Autowired
    private ServiceRegistry m_serviceRegistry;
    
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_deployedForeignSourceRepository;
    
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pendingForeignSourceRepository;

    private static Map<String,String> m_detectors;
    private static Map<String,String> m_policies;
    private static Map<String, PluginWrapper> m_wrappers;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDeployedForeignSourceRepository(ForeignSourceRepository repo) {
        m_deployedForeignSourceRepository = repo;
    }
    /** {@inheritDoc} */
    @Override
    public void setPendingForeignSourceRepository(ForeignSourceRepository repo) {
        m_pendingForeignSourceRepository = repo;
    }
    
    /**
     * <p>getAllForeignSources</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @Override
    public Set<ForeignSource> getAllForeignSources() {
        Set<ForeignSource> foreignSources = new TreeSet<ForeignSource>();
        foreignSources.addAll(m_pendingForeignSourceRepository.getForeignSources());
        for (ForeignSource fs : m_deployedForeignSourceRepository.getForeignSources()) {
            if (!foreignSources.contains(fs)) {
                foreignSources.add(fs);
            }
        }
        return foreignSources;
    }

    /** {@inheritDoc} */
    @Override
    public ForeignSource getForeignSource(String name) {
        ForeignSource fs = m_pendingForeignSourceRepository.getForeignSource(name);
        if (fs.isDefault()) {
            return m_deployedForeignSourceRepository.getForeignSource(name);
        }
        return fs;
    }

    /** {@inheritDoc} */
    @Override
    public ForeignSource saveForeignSource(String name, ForeignSource fs) {
        normalizePluginConfigs(fs);
        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }
    /** {@inheritDoc} */
    @Override
    public void deleteForeignSource(String name) {
        if (name.equals("default")) {
            m_pendingForeignSourceRepository.resetDefaultForeignSource();
            m_deployedForeignSourceRepository.resetDefaultForeignSource();
        } else {
            ForeignSource fs = getForeignSource(name);
            m_pendingForeignSourceRepository.delete(fs);
            m_deployedForeignSourceRepository.delete(fs);
        }
    }
    /** {@inheritDoc} */
    @Override
    public ForeignSource cloneForeignSource(String name, String target) {
        ForeignSource fs = getForeignSource(name);
        fs.setDefault(false);
        fs.setName(target);
        m_deployedForeignSourceRepository.save(fs);
        m_pendingForeignSourceRepository.delete(fs);
        return m_deployedForeignSourceRepository.getForeignSource(target);
    }

    /** {@inheritDoc} */
    @Override
    public ForeignSource addParameter(String foreignSourceName, String pathToAdd) {
        ForeignSource fs = getForeignSource(foreignSourceName);
        PropertyPath path = new PropertyPath(pathToAdd);
        Object obj = path.getValue(fs);
        
        try {
            MethodUtils.invokeMethod(obj, "addParameter", new Object[] { "key", "value" });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to call addParameter on object of type " + obj.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("unable to access property "+pathToAdd, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("an execption occurred adding a parameter to "+pathToAdd, e);
        }

        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }

    /** {@inheritDoc} */
    @Override
    public ForeignSource deletePath(String foreignSourceName, String pathToDelete) {
        ForeignSource fs = getForeignSource(foreignSourceName);
        PropertyPath path = new PropertyPath(pathToDelete);
        
        Object objToDelete = path.getValue(fs);
        Object parentObject = path.getParent() == null ? fs : path.getParent().getValue(fs);
        
        String propName = path.getPropertyName();
        String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
        String methodName = "delete"+methodSuffix;
        
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


    /** {@inheritDoc} */
    @Override
    public ForeignSource addDetectorToForeignSource(String foreignSource, String name) {
        ForeignSource fs = getForeignSource(foreignSource);
        PluginConfig pc = new PluginConfig(name, "unknown");
        fs.addDetector(pc);
        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }
    /** {@inheritDoc} */
    @Override
    public ForeignSource deleteDetector(String foreignSource, String name) {
        ForeignSource fs = getForeignSource(foreignSource);
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
    
    /** {@inheritDoc} */
    @Override
    public ForeignSource addPolicyToForeignSource(String foreignSource, String name) {
        ForeignSource fs = getForeignSource(foreignSource);
        PluginConfig pc = new PluginConfig(name, "unknown");
        fs.addPolicy(pc);
        m_pendingForeignSourceRepository.save(fs);
        return fs;
    }
    /** {@inheritDoc} */
    @Override
    public ForeignSource deletePolicy(String foreignSource, String name) {
        ForeignSource fs = getForeignSource(foreignSource);
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

    /**
     * <p>getDetectorTypes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, String> getDetectorTypes() {
        if (m_detectors == null) {
            Map<String,String> detectors = new TreeMap<String,String>();
            for (ServiceDetector d : m_serviceRegistry.findProviders(ServiceDetector.class)) {
                String serviceName = d.getServiceName();
                if (serviceName == null) {
                    serviceName = d.getClass().getSimpleName();
                }
                detectors.put(serviceName, d.getClass().getName());
            }

            m_detectors = new LinkedHashMap<String,String>();
            for (Entry<String,String> e : detectors.entrySet()) {
                m_detectors.put(e.getValue(), e.getKey());
            }
        }

        return m_detectors;
    }
    /**
     * <p>getPolicyTypes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String, String> getPolicyTypes() {
        if (m_policies == null) {
            Map<String,String> policies = new TreeMap<String,String>();
            for (OnmsPolicy p : m_serviceRegistry.findProviders(OnmsPolicy.class)) {
                String policyName = p.getClass().getSimpleName();
                if (p.getClass().isAnnotationPresent(Policy.class)) {
                    Policy annotation = p.getClass().getAnnotation(Policy.class);
                    if (annotation.value() != null && annotation.value().length() > 0) {
                        policyName = annotation.value();
                    }
                }
                policies.put(policyName, p.getClass().getName());
            }

            m_policies = new LinkedHashMap<String,String>();
            for (Entry<String,String> e : policies.entrySet()) {
                m_policies.put(e.getValue(), e.getKey());
            }
        }

        return m_policies;
    }

    /**
     * <p>getWrappers</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<String,PluginWrapper> getWrappers() {
        if (m_wrappers == null && m_policies != null && m_detectors != null) {
            m_wrappers = new HashMap<String,PluginWrapper>(m_policies.size());
            for (String key : m_policies.keySet()) {
                try {
                    PluginWrapper wrapper = new PluginWrapper(key);
                    m_wrappers.put(key, wrapper);
                } catch (Throwable e) {
                    LOG.warn("unable to wrap {}", key, e);
                }
            }
            for (String key : m_detectors.keySet()) {
                try {
                    PluginWrapper wrapper = new PluginWrapper(key);
                    m_wrappers.put(key, wrapper);
                } catch (Throwable e) {
                    LOG.warn("unable to wrap {}", key, e);
                }
            }
        }
        return m_wrappers;
    }

    private void normalizePluginConfigs(ForeignSource fs) {
        for (PluginConfig pc : fs.getDetectors()) {
            normalizePluginConfig(pc);
        }
        for (PluginConfig pc : fs.getPolicies()) {
            normalizePluginConfig(pc);
        }
    }

    private void normalizePluginConfig(PluginConfig pc) {
        if (m_wrappers.containsKey(pc.getPluginClass())) {
            PluginWrapper w = m_wrappers.get(pc.getPluginClass());
            if (w != null) {
                Map<String,String> parameters = pc.getParameterMap();
                Map<String,Set<String>> required = w.getRequiredItems();
                for (String key : required.keySet()) {
                    String value = "";
                    if (!parameters.containsKey(key)) {
                        if (required.get(key).size() > 0) {
                            value = required.get(key).iterator().next();
                        }
                        pc.addParameter(key, value);
                    }
                }
            }
        }
    }

}
