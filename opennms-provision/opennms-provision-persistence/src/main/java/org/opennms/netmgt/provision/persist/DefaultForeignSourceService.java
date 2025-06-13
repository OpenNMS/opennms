/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.persist;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.MethodUtils;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.spring.PropertyPath;
import org.opennms.netmgt.provision.OnmsPolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
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

    private static Map<String, Class<?>> m_detectors;
    private static Map<String,String> m_policies;
    private static Map<String, PluginWrapper> m_wrappers;

    @Override
    public void afterPropertiesSet() throws Exception {

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
        Set<ForeignSource> foreignSources = new TreeSet<>();
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
    public Map<String, Class<?>> getDetectorTypes() {
        if(m_detectors == null) {
            m_detectors = new LinkedHashMap<>();
        }
        ServiceDetectorRegistry registry = m_serviceRegistry.findProvider(ServiceDetectorRegistry.class);
        if(registry == null) {
            // This should only occur on read only web ui (stripped down) instances and prevents a null pointer exception
            LOG.warn("No ServiceDetectorRegistry found in the ServiceRegistry");
            return m_detectors;
        }
        for(String serviceName: registry.getServiceNames()) {
            if(!m_detectors.containsKey(serviceName)) {
                m_detectors.put(serviceName, registry.getDetectorClassByServiceName(serviceName));
            }
        }
        //Remove those uninstalled detectors
        Set<String> removeList = m_detectors.keySet().stream().filter(svcName -> !registry.getServiceNames().contains(svcName))
                .collect(Collectors.toSet());
        removeList.forEach(svc-> m_detectors.remove(svc));
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
            Map<String,String> policies = new TreeMap<>();
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

            m_policies = new LinkedHashMap<>();
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
        if (m_wrappers == null) {
            m_wrappers = new HashMap<>();
        }
        if(m_policies != null) {
            for (String key : m_policies.keySet()) {
                try {
                    if(!m_wrappers.containsKey(key)) {
                        PluginWrapper wrapper = new PluginWrapper(key);
                        m_wrappers.put(key, wrapper);
                    }
                } catch (Throwable e) {
                    LOG.warn("unable to wrap {}", key, e);
                }
            }
        }
        if(m_detectors != null) {
            for (String key : m_detectors.keySet()) {
                try {
                    Class clazz = m_detectors.get(key);
                    if(!m_wrappers.containsKey(clazz.getCanonicalName())) {
                        PluginWrapper wrapper = new PluginWrapper(clazz);
                        m_wrappers.put(clazz.getCanonicalName(), wrapper);
                    }
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

    private void normalizePluginConfig(final PluginConfig pc) {
        if (m_wrappers.containsKey(pc.getPluginClass())) {
            final PluginWrapper w = m_wrappers.get(pc.getPluginClass());
            if (w != null) {
                final Map<String,String> parameters = pc.getParameterMap();
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
