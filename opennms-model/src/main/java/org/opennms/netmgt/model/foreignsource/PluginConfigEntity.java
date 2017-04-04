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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

/**
 * A PluginConfig represents a portion of a configuration that defines a reference
 * to a Java class "plugin" along with a set of parameters used to configure the
 * behavior of that plugin.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
@Entity
@Table(name="foreignsource_plugins")
@DiscriminatorColumn(name="type")
public abstract class PluginConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    @SequenceGenerator(name="foreignSourceSequence", sequenceName="foreignsourcenxtid")
    @GeneratedValue(generator="foreignSourceSequence")
    private Long id;

    @Column(name="name", nullable=false)
    private String name;

    @Column(name="class", nullable=false)
    private String pluginClass;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "foreignsource_plugin_parameters",
            joinColumns=@JoinColumn(name = "plugin_id", referencedColumnName = "id")
    )
    @MapKeyColumn(name="key", unique=true)
    @Column(name="value", nullable = false)
    @BatchSize(size=100)
    private Map<String, String> parameters= new HashMap<>();

    @ManyToOne(optional=false)
    @JoinColumn(name="foreignsource")
    private ForeignSourceEntity foreignSource;

    protected PluginConfigEntity() {
    }

    /**
     * Creates a plugin configuration with the given name and class.
     *
     * @param name the human-readable name of the plugin
     * @param clazz the name of the plugin's java class
     */
    protected PluginConfigEntity(String name, String clazz) {
        setName(name);
        setPluginClass(clazz);
    }

    protected PluginConfigEntity(PluginConfigEntity pluginConfig) {
        setName(pluginConfig.getName());
        setPluginClass(pluginConfig.getPluginClass());
        setParameters(pluginConfig.getParameters());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPluginClass() {
        return pluginClass;
    }

    public void setPluginClass(String clazz) {
        pluginClass = clazz;
    }

    public Map<String,String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public void setForeignSource(ForeignSourceEntity foreignSource) {
        this.foreignSource = foreignSource;
    }

    public ForeignSourceEntity getForeignSource() {
        return foreignSource;
    }

    public abstract PluginConfigType getType();

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof PluginConfigEntity)) return false;
        final PluginConfigEntity other = (PluginConfigEntity) obj;
        if (getId() != null) {
            return getId().equals(other.getId());
        }
        return super.equals(obj);
    }
}
