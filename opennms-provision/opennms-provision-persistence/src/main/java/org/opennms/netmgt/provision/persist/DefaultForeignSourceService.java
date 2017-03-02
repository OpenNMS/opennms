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

import static org.opennms.netmgt.provision.persist.foreignsource.ForeignSourceMapper.toPersistenceModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.ForeignSourceDao;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigEntity;
import org.opennms.netmgt.provision.OnmsPolicy;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.support.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetClassAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class DefaultForeignSourceService implements ForeignSourceService, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultForeignSourceService.class);
    
    @Autowired
    private ServiceRegistry m_serviceRegistry;
    
    @Autowired
    protected ForeignSourceDao foreignSourceDao;
    
    private Map<String,String> m_detectors;
    private Map<String,String> m_policies;
    private Map<String, PluginWrapper> m_wrappers;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Override
    public Set<String> getActiveForeignSourceNames() {
        return getAllForeignSources().stream().map(fs -> fs.getName()).collect(Collectors.toSet());
    }

    @Override
    public int getForeignSourceCount() {
        return foreignSourceDao.countAll();
    }

    @Override
    public Set<ForeignSourceEntity> getAllForeignSources() {
        return new HashSet<>(foreignSourceDao.findAll());
    }

    @Override
    public ForeignSourceEntity getForeignSource(String name) {
        ForeignSourceEntity foreignSourceEntity = foreignSourceDao.get(name);
        if (foreignSourceEntity == null) {
            foreignSourceEntity = getDefaultForeignSource();
            foreignSourceEntity.setName(name);
            foreignSourceEntity.setDefault(false);
        }
        return foreignSourceEntity;
    }

    @Override
    public void saveForeignSource(ForeignSourceEntity foreignSource) {
        validate(foreignSource);
        normalizePluginConfigs(foreignSource);
        foreignSource.updateDateStamp();
        if (DEFAULT_FOREIGNSOURCE_NAME.equals(foreignSource.getName())) {
            foreignSource.setDefault(true);
        }
        foreignSourceDao.saveOrUpdate(foreignSource);
    }

    @Override
    public ForeignSourceEntity getDefaultForeignSource() {
        ForeignSourceEntity defaultForeignSource = foreignSourceDao.get(DEFAULT_FOREIGNSOURCE_NAME);
        if (defaultForeignSource != null) {
            return defaultForeignSource;
        }
        // No default foreign source exists in the database, load from disk
        ForeignSource foreignSource = JaxbUtils.unmarshal(ForeignSource.class, new ClassPathResource("/org/opennms/netmgt/provision/persist/default-foreign-source.xml"));
        foreignSource.setDefault(true);
        return toPersistenceModel(foreignSource);
    }

    @Override
    public void resetDefaultForeignSource() {
        deleteForeignSource(DEFAULT_FOREIGNSOURCE_NAME);
    }

    @Override
    public void deleteForeignSource(String name) {
        if (name != null && foreignSourceDao.get(name) != null) {
            foreignSourceDao.delete(name);
        }
    }

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

            m_detectors = new LinkedHashMap<>();
            for (Entry<String,String> e : detectors.entrySet()) {
                m_detectors.put(e.getValue(), e.getKey());
            }
        }

        return m_detectors;
    }

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

            m_policies = new LinkedHashMap<>();
            for (Entry<String,String> e : policies.entrySet()) {
                m_policies.put(e.getValue(), e.getKey());
            }
        }

        return m_policies;
    }

    @Override
    public Map<String,PluginWrapper> getWrappers() {
        if (m_wrappers == null) {
            m_wrappers = new HashMap<>(getPolicyTypes().size() + getDetectorTypes().size());
            for (String key : getPolicyTypes().keySet()) {
                try {
                    PluginWrapper wrapper = new PluginWrapper(key);
                    m_wrappers.put(key, wrapper);
                } catch (Exception e) {
                    LOG.warn("unable to wrap {}", key, e);
                }
            }
            for (String key : getDetectorTypes().keySet()) {
                try {
                    PluginWrapper wrapper = new PluginWrapper(key);
                    m_wrappers.put(key, wrapper);
                } catch (Exception e) {
                    LOG.warn("unable to wrap {}", key, e);
                }
            }
        }
        return m_wrappers;
    }

    private void normalizePluginConfigs(ForeignSourceEntity fs) {
        for (PluginConfigEntity pc : fs.getDetectors()) {
            normalizePluginConfig(pc);
        }
        for (PluginConfigEntity pc : fs.getPolicies()) {
            normalizePluginConfig(pc);
        }
    }

    private void normalizePluginConfig(final PluginConfigEntity pc) {
        if (getWrappers().containsKey(pc.getPluginClass())) {
            final PluginWrapper w = getWrappers().get(pc.getPluginClass());
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

    private void validate(ForeignSourceEntity foreignSource) {
        // TODO MVR
//        throw new UnsupportedOperationException("TODO MVR implement me");
    }

}
