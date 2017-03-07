/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.foreignsource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="foreignsource")
public class ForeignSourceEntity implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ForeignSourceEntity.class);

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date date = new Date();

    @Column(name="scaninterval")
    private long scanInterval = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "foreignSource")
    private List<PluginConfigEntity> plugins = new ArrayList<>();

    @Column(name="isdefault", nullable = false)
    private boolean m_default;

    public ForeignSourceEntity() {

    }

    public ForeignSourceEntity(final String name) {
        setName(name);
    }

    public ForeignSourceEntity(ForeignSourceEntity input) {
        setName(input.getName());
        setDefault(input.isDefault());
        setScanInterval(input.getScanInterval());
        setDetectors(input.getDetectors().stream().map(d -> new DetectorPluginConfigEntity(d)).collect(Collectors.toList()));
        setPolicies(input.getPolicies().stream().map(p -> new PolicyPluginConfigEntity(p)).collect(Collectors.toList()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(long scanInterval) {
        this.scanInterval = scanInterval;
    }

    public void setPlugins(List<PluginConfigEntity> plugins) {
        this.plugins = plugins;
    }

    public List<PluginConfigEntity> getPlugins() {
        return plugins;
    }

    public List<DetectorPluginConfigEntity> getDetectors() {
        return plugins.stream()
                .filter(p -> p instanceof DetectorPluginConfigEntity)
                .map(p -> (DetectorPluginConfigEntity) p)
                .collect(Collectors.toList());
    }

    public void setDetectors(List<DetectorPluginConfigEntity> detectors) {
        plugins.removeAll(getDetectors());
        plugins.addAll(detectors);
    }

    public void addDetector(DetectorPluginConfigEntity detector) {
        addPlugin(detector);
    }

    public DetectorPluginConfigEntity getDetector(final String detector) {
        return getDetectors().stream()
                .filter(d -> d.getName().equals(detector))
                .findFirst()
                .orElse(null);
    }

    public boolean removeDetector(final DetectorPluginConfigEntity detector) {
        return plugins.remove(detector);
    }

    public boolean removeDetector(String detector) {
        return removeDetector(getDetector(detector));
    }

    public List<String> getDetectorNames() {
        return getDetectors().stream().map(d -> d.getName()).collect(Collectors.toList());
    }

    public boolean isDefault() {
        return m_default;
    }

    public void setDefault(boolean defaultValue) {
        this.m_default = defaultValue;
    }

    public void addPolicy(PolicyPluginConfigEntity policy) {
        addPlugin(policy);
    }

    public List<PolicyPluginConfigEntity> getPolicies() {
        return plugins.stream()
                .filter(p -> p instanceof PolicyPluginConfigEntity)
                .map(p -> (PolicyPluginConfigEntity) p)
                .collect(Collectors.toList());
    }

    public void setPolicies(List<PolicyPluginConfigEntity> policies) {
        plugins.removeAll(getPolicies());
        plugins.addAll(policies);
    }

    public PolicyPluginConfigEntity getPolicy(final String policy) {
        return getPolicies().stream()
                .filter(p -> p.getName().equals(policy))
                .findFirst()
                .orElse(null);
    }

    public boolean removePolicy(final PolicyPluginConfigEntity policy) {
        return plugins.remove(policy);
    }

    public boolean removePolicy(String policy) {
        return removePolicy(getPolicy(policy));
    }

    public void updateDateStamp() {
        this.date = new Date();
    }

    public PluginConfigEntity getPlugin(String name, PluginConfigType type) {
        return getPlugins().stream()
                .filter(p -> p.getName().equals(name) && type.isInstance(p))
                .findFirst().orElse(null);
    }

    public void addPlugin(PluginConfigEntity pluginConfig) {
        if (!plugins.contains(pluginConfig)) {
            pluginConfig.setForeignSource(this);
            plugins.add(pluginConfig);
        }
    }

    @Override
    public int hashCode() {
        if (getName() != null) {
            return getName().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ForeignSourceEntity)) return false;
        final ForeignSourceEntity other = (ForeignSourceEntity) obj;
        if (getName() != null) {
            return getName().equals(other.getName());
        }
        return super.equals(obj);
    }
}
