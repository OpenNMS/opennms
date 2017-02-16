/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.model.requisition.DetectorPluginConfig;
import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.model.requisition.OnmsPluginConfig;
import org.opennms.netmgt.model.requisition.PolicyPluginConfig;
import org.opennms.netmgt.provision.OnmsPolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.support.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetClassAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DefaultForeignSourceService implements ForeignSourceService, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultForeignSourceService.class);
    
    @Autowired
    private ServiceRegistry m_serviceRegistry;
    
    @Autowired
    @Qualifier("database")
    private ForeignSourceRepository m_foreignSourceRepository;
    
    private static Map<String,String> m_detectors;
    private static Map<String,String> m_policies;
    private static Map<String, PluginWrapper> m_wrappers;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Override
    public void setDeployedForeignSourceRepository(ForeignSourceRepository repo) {
        m_foreignSourceRepository = repo;
    }

    @Override
    public void setPendingForeignSourceRepository(ForeignSourceRepository repo) {
        throw new UnsupportedOperationException("määäh"); // TODO MVR ...
    }

    @Override
    public Set<OnmsForeignSource> getAllForeignSources() {
        return Collections.unmodifiableSet(m_foreignSourceRepository.getForeignSources());
    }

    @Override
    public OnmsForeignSource getForeignSource(String name) {
        return m_foreignSourceRepository.getForeignSource(name);
    }

    @Override
    public OnmsForeignSource saveForeignSource(String name, OnmsForeignSource fs) {
        normalizePluginConfigs(fs);
        m_foreignSourceRepository.save(fs);
        return fs;
    }

    @Override
    public void deleteForeignSource(String name) {
        if (name.equals(ForeignSourceRepository.DEFAULT_FOREIGNSOURCE_NAME)) {
            m_foreignSourceRepository.resetDefaultForeignSource();
            m_foreignSourceRepository.resetDefaultForeignSource();
        } else {
            OnmsForeignSource fs = getForeignSource(name);
            m_foreignSourceRepository.delete(fs);
        }
    }

    @Override
    public OnmsForeignSource cloneForeignSource(String name, String target) {
        OnmsForeignSource fs = getForeignSource(name);
        fs.setDefault(false);
        fs.setName(target);
        m_foreignSourceRepository.save(fs); // TODO MVR verify that this clones the foreign source
        return m_foreignSourceRepository.getForeignSource(target);
    }

    @Override
    public OnmsForeignSource addParameter(String foreignSourceName, String pathToAdd) {

//        OnmsForeignSource fs = getForeignSource(foreignSourceName);
//        PropertyPath path = new PropertyPath(pathToAdd);
//        Object obj = path.getValue(fs);
//
//        try {
//            MethodUtils.invokeMethod(obj, "addParameter", new Object[] { "key", "value" });
//        } catch (NoSuchMethodException e) {
//            throw new IllegalArgumentException("Unable to call addParameter on object of type " + obj.getClass(), e);
//        } catch (IllegalAccessException e) {
//            throw new IllegalArgumentException("unable to access property "+pathToAdd, e);
//        } catch (InvocationTargetException e) {
//            throw new IllegalArgumentException("an execption occurred adding a parameter to "+pathToAdd, e);
//        }
//
//        m_pendingForeignSourceRepository.save(fs);
//        return fs;
        // TODO MVR implement me
        return null;
    }

    @Override
    public OnmsForeignSource deletePath(String foreignSourceName, String pathToDelete) {
//        ForeignSource fs = getForeignSource(foreignSourceName);
//        PropertyPath path = new PropertyPath(pathToDelete);
//
//        Object objToDelete = path.getValue(fs);
//        Object parentObject = path.getParent() == null ? fs : path.getParent().getValue(fs);
//
//        String propName = path.getPropertyName();
//        String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
//        String methodName = "delete"+methodSuffix;
//
//        try {
//            MethodUtils.invokeMethod(parentObject, methodName, new Object[] { objToDelete });
//        } catch (NoSuchMethodException e) {
//            throw new IllegalArgumentException("Unable to find method "+methodName+" on object of type "+parentObject.getClass()+" with argument " + objToDelete, e);
//        } catch (IllegalAccessException e) {
//            throw new IllegalArgumentException("unable to access property "+pathToDelete, e);
//        } catch (InvocationTargetException e) {
//            throw new IllegalArgumentException("an execption occurred deleting "+pathToDelete, e);
//        }
//
//        m_pendingForeignSourceRepository.save(fs);
//        return fs;
        // TODO MVR implement me
        return null;
    }

    @Override
    public OnmsForeignSource addDetectorToForeignSource(String foreignSource, String name) {
        OnmsForeignSource fs = getForeignSource(foreignSource);
        DetectorPluginConfig pc = new DetectorPluginConfig(name, "unknown");
        fs.addDetector(pc);
        m_foreignSourceRepository.save(fs);
        return fs;
    }

    @Override
    public OnmsForeignSource deleteDetector(String foreignSource, String name) {
        OnmsForeignSource fs = getForeignSource(foreignSource);
        fs.removeDetector(name);
        m_foreignSourceRepository.save(fs);
        return fs;
    }
    
    @Override
    public OnmsForeignSource addPolicyToForeignSource(String foreignSource, String name) {
        OnmsForeignSource fs = getForeignSource(foreignSource);
        PolicyPluginConfig pc = new PolicyPluginConfig(name, "unknown");
        fs.addPolicy(pc);
        m_foreignSourceRepository.save(fs);
        return fs;
    }

    @Override
    public OnmsForeignSource deletePolicy(String foreignSource, String name) {
        OnmsForeignSource fs = getForeignSource(foreignSource);
        fs.removePolicy(name);
        m_foreignSourceRepository.save(fs);
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
                String className = d.getClass().getName();
                // NMS-8119: The class name may be changed when using proxy objects
                if (d instanceof TargetClassAware) {
                    className = ((TargetClassAware)d).getTargetClass().getName();
                }
                detectors.put(serviceName, className);
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
            m_wrappers = new HashMap<String,PluginWrapper>(m_policies.size() + m_detectors.size());
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

    private void normalizePluginConfigs(OnmsForeignSource fs) {
        for (OnmsPluginConfig pc : fs.getDetectors()) {
            normalizePluginConfig(pc);
        }
        for (OnmsPluginConfig pc : fs.getPolicies()) {
            normalizePluginConfig(pc);
        }
    }

    private void normalizePluginConfig(final OnmsPluginConfig pc) {
        if (m_wrappers.containsKey(pc.getPluginClass())) {
            final PluginWrapper w = m_wrappers.get(pc.getPluginClass());
            if (w != null) {
                final Map<String,String> parameters = pc.getParameters();
                final Map<String,Set<String>> required = w.getRequiredItems();
                for (final Entry<String, Set<String>> entry : required.entrySet()) {
                    final String key = entry.getKey();
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
