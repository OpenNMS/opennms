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
package org.opennms.web.rest.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.api.CollectdConfigFactory;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.support.PluginWrapper;
import org.opennms.web.svclayer.support.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

/**
 * The Class ForeignSourceConfigRestService.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component("foreignSourceConfigRestService")
@Path("foreignSourcesConfig")
@Tag(name = "ForeignSourcesConfig", description = """
        Foreign Sources Config API.

        Read-only lookups: the detector and policy plugin classes this installation can load, the parameters
        each one accepts, the service names a detector may be bound to, the surveillance categories that
        exist, and the asset field names a requisition may set.

        These list what is available, not what any one foreign source is configured with. The configured
        detectors and policies of a foreign source are on `/foreignSources/{foreignSource}`.""")
public class ForeignSourceConfigRestService extends OnmsRestService implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ForeignSourceConfigRestService.class);

    /** The foreign source service. */
    @Autowired
    protected ForeignSourceService m_foreignSourceService;

    /** The poller configuration. */
    @Autowired
    protected PollerConfig m_pollerConfig;

    @Autowired
    protected CollectdConfigFactory m_collectdConfigFactory;

    @Autowired
    private CategoryDao m_categoryDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_foreignSourceService, "ForeignSourceService is required.");

        // The following is required, otherwise getWrappers() throws a NPE
        m_foreignSourceService.getPolicyTypes();
        m_foreignSourceService.getDetectorTypes();
    }

    /**
     * The Class SimplePluginConfigList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="plugin-configuration")
    public static class SimplePluginConfigList extends JaxbListWrapper<SimplePluginConfig> {

        /**
         * Gets the Plugins.
         *
         * @return the Plugins
         */
        @XmlElement(name="plugin")
        @XmlElementWrapper(name="plugins")
        @Schema(description = "One entry per plugin class that could be loaded and introspected. A class that fails to wrap is logged and skipped, so this can be shorter than the configured plugin list.")
        public List<SimplePluginConfig> getPlugins() {
            return getObjects();
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
        @Schema(description = "Display name. For detectors this is the service name the detector registers under; for policies it is the policy's registered label.", example = "SNMP")
        public String name;

        /** The plugin class. */
        @XmlAttribute(name="class")
        @Schema(description = "Fully qualified class name, the value to put in a foreign source definition's detector or policy `class` attribute.", example = "org.opennms.netmgt.provision.detector.snmp.SnmpDetector")
        public String pluginClass;

        /** The parameters. */
        @XmlElement(name="parameter")
        @XmlElementWrapper(name="parameters")
        @Schema(description = "Accepted parameters, required ones first, each group sorted by key.")
        public List<SimplePluginParameter> parameters = new ArrayList<>();

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
        @Schema(description = "Parameter name.", example = "matchBehavior")
        public String key;

        /** The required. */
        @XmlAttribute
        @Schema(description = "Whether the plugin declares the parameter as required.", example = "true")
        public Boolean required;

        /** The options. */
        @XmlElement(name="option")
        @XmlElementWrapper(name="options")
        @Schema(description = "Permitted values, sorted. Empty when the parameter is free-form.", example = "[\"ALL_PARAMETERS\",\"ANY_PARAMETER\",\"NO_PARAMETERS\"]")
        public List<String> options = new ArrayList<>();

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
    public static class ElementList extends JaxbListWrapper<String> {

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
         * Gets the elements.
         *
         * @return the elements
         */
        @XmlElement(name="element")
        @Schema(description = "The listed names, sorted.", example = "[\"Production\",\"Routers\",\"Servers\"]")
        public List<String> getElements() {
            List<String> elements = getObjects();
            Collections.sort(elements);
            return elements;
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
    @Operation(
            summary = "List the provisioning policy classes available on this installation",
            description = """
                    Each entry carries the policy's display name, its class name, and the parameters it accepts.
                    Required parameters are listed before optional ones and each group is sorted by key. A
                    parameter whose value is constrained reports its permitted values in `options`; a free-form
                    parameter reports an empty `options`. Policy classes that could not be introspected are
                    logged and left out, so the list can be shorter than the set of registered policies.""",
            operationId = "getAvailablePolicies")
    @ApiResponse(responseCode = "200", description = "Available policy classes.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SimplePluginConfigList.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "plugins": [
                                        {
                                          "name": "Match IP Interface",
                                          "class": "org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy",
                                          "parameters": [
                                            { "key": "action", "required": true,
                                              "options": [ "DISABLE_COLLECTION", "DISABLE_SNMP_POLL", "DO_NOT_PERSIST", "ENABLE_COLLECTION", "ENABLE_SNMP_POLL", "MANAGE", "UNMANAGE" ] },
                                            { "key": "matchBehavior", "required": true,
                                              "options": [ "ALL_PARAMETERS", "ANY_PARAMETER", "NO_PARAMETERS" ] },
                                            { "key": "hostName", "required": false, "options": [] },
                                            { "key": "ipAddress", "required": false, "options": [] }
                                          ]
                                        }
                                      ],
                                      "totalCount": 6,
                                      "count": 6,
                                      "offset": 0
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SimplePluginConfigList.class),
                            examples = @ExampleObject(value = """
                                    <plugin-configuration count="6" offset="0" totalCount="6">
                                      <plugins>
                                        <plugin name="Match IP Interface" class="org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy">
                                          <parameters>
                                            <parameter key="action" required="true">
                                              <options>
                                                <option>DO_NOT_PERSIST</option>
                                                <option>MANAGE</option>
                                              </options>
                                            </parameter>
                                            <parameter key="ipAddress" required="false"><options/></parameter>
                                          </parameters>
                                        </plugin>
                                      </plugins>
                                    </plugin-configuration>"""))
            })
    public SimplePluginConfigList getAvailablePolicies() {
        SimplePluginConfigList plugins = new SimplePluginConfigList();
        Map<String,String> typesMap = m_foreignSourceService.getPolicyTypes();
        for(String pluginClass: typesMap.keySet()) {
            final PluginWrapper wrapper = m_foreignSourceService.getWrappers().get(pluginClass);
            if (wrapper == null) {
                LOG.warn("No wrapper found for plugin class {}. See previous log messages for wrapping failures.", pluginClass);
                continue;
            }
            String pluginName = typesMap.get(pluginClass);
            SimplePluginConfig cfg = createPluginConfig(pluginName, pluginClass, wrapper);
            plugins.add(cfg);
        }
        return plugins;
    }

    /**
     * Gets the available detectors.
     *
     * @return the available detectors
     */
    @GET
    @Path("detectors")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the service detector classes available on this installation",
            description = """
                    Same shape as `/foreignSourcesConfig/policies`, sorted by detector name. `name` is the
                    service name the detector is registered under, which is the name that appears on the
                    monitored service when detection succeeds. Detector classes that could not be introspected
                    are logged and left out.""",
            operationId = "getAvailableDetectors")
    @ApiResponse(responseCode = "200", description = "Available detector classes, sorted by name.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SimplePluginConfigList.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "plugins": [
                                        {
                                          "name": "ActiveMQ",
                                          "class": "org.opennms.netmgt.provision.detector.jms.ActiveMQDetector",
                                          "parameters": [
                                            { "key": "brokerURL", "required": false, "options": [] },
                                            { "key": "ipMatch", "required": false, "options": [] },
                                            { "key": "port", "required": false, "options": [] },
                                            { "key": "timeout", "required": false, "options": [] }
                                          ]
                                        }
                                      ],
                                      "totalCount": 44,
                                      "count": 44,
                                      "offset": 0
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SimplePluginConfigList.class),
                            examples = @ExampleObject(value = """
                                    <plugin-configuration count="44" offset="0" totalCount="44">
                                      <plugins>
                                        <plugin name="ActiveMQ" class="org.opennms.netmgt.provision.detector.jms.ActiveMQDetector">
                                          <parameters>
                                            <parameter key="brokerURL" required="false"><options/></parameter>
                                            <parameter key="port" required="false"><options/></parameter>
                                          </parameters>
                                        </plugin>
                                      </plugins>
                                    </plugin-configuration>"""))
            })
    public SimplePluginConfigList getAvailableDetectors() {
        SimplePluginConfigList plugins = new SimplePluginConfigList();
        Map<String, Class<?>> detectorMap = m_foreignSourceService.getDetectorTypes();
        for(String serviceName: detectorMap.keySet()) {
            Class<?> clazz = detectorMap.get(serviceName);
            PluginWrapper wrapper = m_foreignSourceService.getWrappers().get(clazz.getCanonicalName());
            if (wrapper == null) {
                LOG.warn("No wrapper found for detector class {}. See previous log messages for wrapping failures.", clazz.getCanonicalName());
                continue;
            }
            SimplePluginConfig cfg = createPluginConfig(serviceName, clazz.getCanonicalName(), wrapper);
            plugins.add(cfg);
        }
        plugins.getPlugins().sort(Comparator.comparing(cfg0 -> cfg0.name));
        return plugins;
    }

    private SimplePluginConfig createPluginConfig(String serviceName, String className, PluginWrapper wrapper) {
        SimplePluginConfig cfg = new SimplePluginConfig(serviceName, className);
        List<SimplePluginParameter> requiredParams = new ArrayList<>();
        List<SimplePluginParameter> optionalParams = new ArrayList<>();
        for(Map.Entry<String, Boolean> paramEntry: wrapper.getRequired().entrySet()) {
            final Boolean required = paramEntry.getValue();
            final String paramName = paramEntry.getKey();
            final Set<String> options = required ? wrapper.getRequiredItems().get(paramName) : wrapper.getOptionalItems().get(paramName);
            final List<String> optionList = new ArrayList<>(options);
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
        return cfg;
    }

    /**
     * Gets the services.
     * <p>It will include all the configured service monitors from poller-configuration.xml.</p>
     * <p>If the groupName is not null, it will include the services defined on the foreign source.</p>
     *
     * @param groupName the group name
     * @return the services
     */
    @GET
    @Path("services/{groupName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the service names that may be bound to a requisition's interfaces",
            description = """
                    The union of the service monitors configured in `poller-configuration.xml`, the services
                    configured in `collectd-configuration.xml`, the detector names on the named foreign source,
                    and the service types already present in the database. The result is sorted and
                    de-duplicated.

                    An unknown `groupName` is not reported as an error: the foreign source lookup falls back to
                    the default definition, so the response is still a 200 and carries the default
                    definition's detector names.""",
            operationId = "getForeignSourceConfigServices")
    @ApiResponse(responseCode = "200", description = "Service names, sorted.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ElementList.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 43,
                                      "count": 43,
                                      "offset": 0,
                                      "element": [ "ActiveMQ", "DNS", "HTTP", "HTTPS", "ICMP", "SNMP", "SSH", "StrafePing" ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ElementList.class),
                            examples = @ExampleObject(value = """
                                    <elements count="43" offset="0" totalCount="43">
                                      <element>ActiveMQ</element>
                                      <element>ICMP</element>
                                      <element>SNMP</element>
                                    </elements>"""))
            })
    public ElementList getServices(@Parameter(required = true, description = "Foreign source (provisioning group) name whose detector names are folded into the list.", example = "selfmonitor") @PathParam("groupName") String groupName) {
        ElementList elements = new ElementList(m_pollerConfig.getServiceMonitorNames());
        m_collectdConfigFactory.getCollectors().forEach(c -> {
            if (!elements.contains(c.getService())) {
                elements.add(c.getService());
            }
        });
        if (groupName != null) {
            final SortedSet<String> serviceNames = new TreeSet<>();
            final ForeignSource pendingForeignSource = m_foreignSourceService.getForeignSource(groupName);
            serviceNames.addAll(pendingForeignSource.getDetectorNames());

            for (final OnmsServiceType type : m_serviceTypeDao.findAll()) {
                serviceNames.add(type.getName());
            }

            // Include all of the service names defined in the poller configuration
            if (m_pollerConfig != null && m_pollerConfig.getServiceMonitorNames() != null && ! m_pollerConfig.getServiceMonitorNames().isEmpty()) {
                serviceNames.addAll(m_pollerConfig.getServiceMonitorNames());
            }
            serviceNames.forEach(s -> {
                if (!elements.contains(s)) {
                    elements.add(s);
                }
            });
        }
        return elements;
    }

    /**
     * Gets the assets.
     *
     * @return the assets
     */
    @GET
    @Path("assets")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the asset field names a requisition may set",
            description = """
                    Derived from the bean properties of the node asset record, sorted, with `id`, `class`,
                    `geolocation` and `node` removed. These are the names accepted as the `name` of a
                    requisition asset, for example in
                    `POST /requisitions/{foreignSource}/nodes/{foreignId}/assets`.""",
            operationId = "getForeignSourceConfigAssets")
    @ApiResponse(responseCode = "200", description = "Asset field names, sorted.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ElementList.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 65,
                                      "count": 65,
                                      "offset": 0,
                                      "element": [ "address1", "building", "category", "city", "country", "department", "description", "region", "serialNumber", "vendor" ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ElementList.class),
                            examples = @ExampleObject(value = """
                                    <elements count="65" offset="0" totalCount="65">
                                      <element>building</element>
                                      <element>city</element>
                                      <element>region</element>
                                    </elements>"""))
            })
    public ElementList getAssets() {
        final List<String> blackList = Lists.newArrayList("id", "class", "geolocation", "node");
        final Collection<String> assets = PropertyUtils.getProperties(new OnmsAssetRecord())
                .stream()
                .filter(a -> !blackList.contains(a))
                .collect(Collectors.toList());
        return new ElementList(assets);
    }

    /**
     * Gets the categories.
     *
     * @return the categories
     */
    @GET
    @Path("categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the surveillance categories that exist",
            description = """
                    Every category name in the database, sorted. A requisition category does not have to name
                    one of these: a category on a requisitioned node is created on import if it does not yet
                    exist, so this is the list of categories already known rather than the set of permitted
                    values.""",
            operationId = "getForeignSourceConfigCategories")
    @ApiResponse(responseCode = "200", description = "Category names, sorted.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ElementList.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 4,
                                      "count": 4,
                                      "offset": 0,
                                      "element": [ "Development", "Production", "Routers", "Servers" ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = ElementList.class),
                            examples = @ExampleObject(value = """
                                    <elements count="4" offset="0" totalCount="4">
                                      <element>Development</element>
                                      <element>Production</element>
                                    </elements>"""))
            })
    public ElementList getCategories() {
        final Set<String> categories = m_categoryDao.findAll().stream()
                .map(c -> c.getName())
                .collect(Collectors.toSet());
        return new ElementList(categories);
    }
}
