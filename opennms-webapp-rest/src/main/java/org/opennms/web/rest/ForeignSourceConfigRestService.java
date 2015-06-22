/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.support.PluginWrapper;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * The Class ForeignSourceConfigRestService.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("foreignSourcesConfig")
public class ForeignSourceConfigRestService extends OnmsRestService implements InitializingBean {

    /** The m_foreign source service. */
    @Autowired
    protected ForeignSourceService m_foreignSourceService;

    /** The m_provisioning service. */
    @Autowired
    protected ManualProvisioningService m_provisioningService;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_foreignSourceService, "ForeignSourceService is required.");
        Assert.notNull(m_provisioningService, "ManualProvisioningService is required.");
        // The following is required, otherwise getWrappers() throws a NPE
        m_foreignSourceService.getPolicyTypes();
        m_foreignSourceService.getDetectorTypes();
    }

    /**
     * The Class SimplePluginConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="plugin-configuration")
    public static class SimplePluginConfigList extends ArrayList<SimplePluginConfig> {

        /**
         * Gets the Plugins.
         *
         * @return the Plugins
         */
        @XmlElement(name="plugin")
        @XmlElementWrapper(name="plugins")
        public List<SimplePluginConfig> getPlugins() {
            return this;
        }
    }

    /**
     * The Class SimplePluginConfig.
     */
    @XmlRootElement(name="plugin")
    @XmlType(propOrder = { "name", "pluginClass", "parameters" })
    public static class SimplePluginConfig {

        /** The name. */
        @XmlAttribute(name="name")
        public String name;

        /** The plugin class. */
        @XmlAttribute(name="class")
        public String pluginClass;

        /** The parameters. */
        @XmlElement(name="parameter")
        @XmlElementWrapper(name="parameters")
        public List<SimplePluginParameter> parameters = new ArrayList<SimplePluginParameter>();

        /**
         * Instantiates a new simple plugin configuration.
         */
        public SimplePluginConfig() {}

        /**
         * Instantiates a new simple plugin configuration.
         *
         * @param name the name
         * @param pluginClass the plugin class
         */
        public SimplePluginConfig(String name, String pluginClass) {
            this.name = name;
            this.pluginClass = pluginClass;
        }
    }

    /**
     * The Class SimplePluginParameter.
     */
    @XmlRootElement(name="parameter")
    @XmlType(propOrder = { "key", "required", "options" })
    public static class SimplePluginParameter {

        /** The key. */
        @XmlAttribute
        public String key;

        /** The required. */
        @XmlAttribute
        public Boolean required;

        /** The options. */
        @XmlElement(name="option")
        @XmlElementWrapper(name="options")
        public List<String> options = new ArrayList<String>();

        /**
         * Instantiates a new simple plugin parameter.
         */
        public SimplePluginParameter() {}

        /**
         * Instantiates a new simple plugin parameter.
         *
         * @param key the key
         * @param required the required
         * @param options the options
         */
        public SimplePluginParameter(String key, Boolean required, List<String> options) {
            this.key = key;
            this.required = required;
            this.options = options;
        }
    }

    /**
     * The Class ElementList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="elements")
    public static class ElementList extends ArrayList<String> {

        /**
         * Instantiates a new element list.
         */
        public ElementList() {
            super();
        }

        /**
         * Instantiates a new element list.
         *
         * @param c the c
         */
        public ElementList(Collection<? extends String> c) {
            super(c);
        }

        /**
         * Gets the services.
         *
         * @return the services
         */
        @XmlElement(name="element")
        public List<String> getServices() {
            return this;
        }
    }

    /**
     * The Class ParameterComparator.
     */
    public static class ParameterComparator implements Comparator<SimplePluginParameter> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(SimplePluginParameter o1, SimplePluginParameter o2) {
            return o1.key.compareTo(o2.key);
        }
    }

    /**
     * Gets the available policies.
     *
     * @return the available policies
     */
    @GET
    @Path("policies")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public SimplePluginConfigList getAvailablePolicies() {
        return getPlugins(true);
    }

    /**
     * Gets the available detectors.
     *
     * @return the available detectors
     */
    @GET
    @Path("detectors")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public SimplePluginConfigList getAvailableDetectors() {
        return getPlugins(false);
    }

    /**
     * Gets the services.
     *
     * @param groupName the group name
     * @return the services
     */
    @GET
    @Path("services/{groupName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public ElementList getServices(@PathParam("groupName") String groupName) {
        final ElementList list = new ElementList(m_provisioningService.getServiceTypeNames(groupName));
        Collections.sort(list);
        return list;
    }

    /**
     * Gets the assets.
     *
     * @return the assets
     */
    @GET
    @Path("assets")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public ElementList getAssets() {
        final ElementList list = new ElementList(m_provisioningService.getAssetFieldNames());
        Collections.sort(list);
        return list;
    }

    /**
     * Gets the categories.
     *
     * @return the categories
     */
    @GET
    @Path("categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public ElementList getCategories() {
        final ElementList list = new ElementList(m_provisioningService.getNodeCategoryNames());
        Collections.sort(list);
        return list;
    }

    /**
     * Gets the Plugins.
     *
     * @param isPolicies the is policies
     * @return the Plugins
     */
    protected SimplePluginConfigList getPlugins(boolean isPolicies) {
        SimplePluginConfigList plugins = new SimplePluginConfigList();
        Map<String,String> typesMap = isPolicies ? m_foreignSourceService.getPolicyTypes(): m_foreignSourceService.getDetectorTypes();
        for (String pluginClass : typesMap.keySet()) {
            PluginWrapper wrapper = m_foreignSourceService.getWrappers().get(pluginClass);
            final String pluginName = typesMap.get(pluginClass);
            SimplePluginConfig cfg = new SimplePluginConfig(pluginName, pluginClass);
            List<SimplePluginParameter> requiredParams = new ArrayList<SimplePluginParameter>();
            List<SimplePluginParameter> optionalParams = new ArrayList<SimplePluginParameter>();
            for (Map.Entry<String,Boolean> paramEntry : wrapper.getRequired().entrySet()) {
                final Boolean required = paramEntry.getValue();
                final String paramName = paramEntry.getKey();
                final Set<String> options = required ? wrapper.getRequiredItems().get(paramName) : wrapper.getOptionalItems().get(paramName);
                final List<String> optionList = new ArrayList<String>(options);
                Collections.sort(optionList);
                SimplePluginParameter param = new SimplePluginParameter(paramName, required, optionList);
                if (required) {
                    requiredParams.add(param);
                } else {
                    optionalParams.add(param);
                }
            }
            Collections.sort(requiredParams, new ParameterComparator());
            Collections.sort(optionalParams, new ParameterComparator());
            cfg.parameters.addAll(requiredParams);
            cfg.parameters.addAll(optionalParams);
            plugins.add(cfg);
        }
        return plugins;
    }

}
