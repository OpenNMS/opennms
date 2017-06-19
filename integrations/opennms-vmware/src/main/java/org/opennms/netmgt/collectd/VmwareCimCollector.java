/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 * Additional permission under GNU AGPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with SBLIM (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License,
 * the licensors of this Program grant you additional permission to
 * convey the resulting work. {Corresponding Source for a non-source
 * form of such a combination shall include the source code for the
 * parts of SBLIM used as well as that of the covered work.}
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.config.vmware.cim.Attrib;
import org.opennms.netmgt.config.vmware.cim.VmwareCimCollection;
import org.opennms.netmgt.config.vmware.cim.VmwareCimGroup;
import org.opennms.netmgt.dao.VmwareCimDatacollectionConfigDao;
import org.opennms.netmgt.dao.VmwareConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.sblim.wbem.cim.CIMObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.mo.HostSystem;

public class VmwareCimCollector extends AbstractRemoteServiceCollector {

    /**
     * Interface for defining methods for value modifications
     */
    private interface ValueModifier {
        String modifyValue(String name, String value, CIMObject cimObject, VmwareViJavaAccess vmwareViJavaAccess);
    }

    /**
     * Value modifiers
     */
    private static Map<String, ValueModifier> valueModifiers = new HashMap<String, ValueModifier>();

    static {
        /**
         * SensorType
         */
        valueModifiers.put("SensorType", new ValueModifier() {
            public String modifyValue(String name, String value, CIMObject cimObject, VmwareViJavaAccess vmwareViJavaAccess) {
                if (value != null) {
                    String modifiedValue = sensorTypeMapping.get(Integer.valueOf(value));

                    if (modifiedValue != null) {
                        return modifiedValue;
                    }
                }

                return "null";
            }
        });

        /**
         * BaseUnits
         */
        valueModifiers.put("BaseUnits", new ValueModifier() {
            public String modifyValue(String name, String value, CIMObject cimObject, VmwareViJavaAccess vmwareViJavaAccess) {
                if (value != null) {
                    String modifiedValue = baseUnitMapping.get(Integer.valueOf(value));

                    if (modifiedValue != null) {
                        return modifiedValue;
                    }
                }

                return "null";
            }
        });

        /**
         * RateUnits
         */
        valueModifiers.put("RateUnits", new ValueModifier() {
            public String modifyValue(String name, String value, CIMObject cimObject, VmwareViJavaAccess vmwareViJavaAccess) {
                if (value != null) {
                    String modifiedValue = rateUnitMapping.get(Integer.valueOf(value));

                    if (modifiedValue != null) {
                        return modifiedValue;
                    }
                }

                return "null";
            }
        });

        /**
         * CurrentReading, this is used also for thresholds and min/max readable
         */
        ValueModifier currentReadingModifier = new ValueModifier() {
            public String modifyValue(String name, String value, CIMObject cimObject, VmwareViJavaAccess vmwareViJavaAccess) {
                if (value == null) {
                    return null;
                } else {
                    return applyUnitModifier(value, vmwareViJavaAccess.getPropertyOfCimObject(cimObject, "UnitModifier"));
                }
            }

            private String applyUnitModifier(String attributeValue, String unitModifier) {
                return String.valueOf(Double.valueOf(attributeValue) * Math.pow(10.0d, Double.valueOf(unitModifier)));
            }
        };

        valueModifiers.put("CurrentReading", currentReadingModifier);
        valueModifiers.put("UpperThresholdCritical", currentReadingModifier);
        valueModifiers.put("LowerThresholdCritical", currentReadingModifier);
        valueModifiers.put("UpperThresholdNonCritical", currentReadingModifier);
        valueModifiers.put("LowerThresholdNonCritical", currentReadingModifier);
        valueModifiers.put("UpperThresholdFatal", currentReadingModifier);
        valueModifiers.put("LowerThresholdFatal", currentReadingModifier);
        valueModifiers.put("MaxReadable", currentReadingModifier);
        valueModifiers.put("MinReadable", currentReadingModifier);
    }

    /**
     * logging for VMware CIM data collection
     */
    private static final Logger logger = LoggerFactory.getLogger(VmwareCimCollector.class);

    private static final String VMWARE_COLLECTION_KEY = "vmwareCollection";
    private static final String VMWARE_MGMT_SERVER_KEY = "vmwareManagementServer";
    private static final String VMWARE_MGED_OBJECT_ID_KEY = "vmwareManagedObjectId";
    private static final String VMWARE_SERVER_KEY = "vmwareServer";

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(VMWARE_COLLECTION_KEY, VmwareCimCollection.class),
            new SimpleEntry<>(VMWARE_SERVER_KEY, VmwareServer.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

    /**
     * the config dao
     */
    private VmwareCimDatacollectionConfigDao m_vmwareCimDatacollectionConfigDao;

    /**
     * the config dao
     */
    private VmwareConfigDao m_vmwareConfigDao = null;

    public VmwareCimCollector() {
        super(TYPE_MAP);
    }

    /**
     * SensorType mapping
     */
    private static Map<Integer, String> sensorTypeMapping = new HashMap<Integer, String>();

    static {
        sensorTypeMapping.put(0, "Unknown");
        sensorTypeMapping.put(1, "Other");
        sensorTypeMapping.put(2, "Temperature");
        sensorTypeMapping.put(3, "Voltage");
        sensorTypeMapping.put(4, "Current");
        sensorTypeMapping.put(5, "Tachometer");
        sensorTypeMapping.put(6, "Counter");
        sensorTypeMapping.put(7, "Switch");
        sensorTypeMapping.put(8, "Lock");
        sensorTypeMapping.put(9, "Humidity");
        sensorTypeMapping.put(10, "Smoke Detection");
        sensorTypeMapping.put(11, "Presence");
        sensorTypeMapping.put(12, "Air Flow");
        sensorTypeMapping.put(13, "Power Consumption");
        sensorTypeMapping.put(14, "Power Production");
        sensorTypeMapping.put(15, "Pressure");
        sensorTypeMapping.put(16, "Intrusion");
        sensorTypeMapping.put(17, "DMTF Reserved");
        sensorTypeMapping.put(18, "Vendor Reserved");
    }

    /**
     * RateUnit mapping
     */
    private static Map<Integer, String> rateUnitMapping = new HashMap<Integer, String>();

    static {
        rateUnitMapping.put(0, "None");
        rateUnitMapping.put(1, "Per MicroSecond");
        rateUnitMapping.put(2, "Per MilliSecond");
        rateUnitMapping.put(3, "Per Second");
        rateUnitMapping.put(4, "Per Minute");
        rateUnitMapping.put(5, "Per Hour");
        rateUnitMapping.put(6, "Per Day");
        rateUnitMapping.put(7, "Per Week");
        rateUnitMapping.put(8, "Per Month");
        rateUnitMapping.put(9, "Per Year");
    }

    /**
     * BaseUnit mapping
     */
    private static Map<Integer, String> baseUnitMapping = new HashMap<Integer, String>();

    static {
        baseUnitMapping.put(0, "Unknown");
        baseUnitMapping.put(1, "Other");
        baseUnitMapping.put(2, "Degrees C");
        baseUnitMapping.put(3, "Degrees F");
        baseUnitMapping.put(4, "Degrees K");
        baseUnitMapping.put(5, "Volts");
        baseUnitMapping.put(6, "Amps");
        baseUnitMapping.put(7, "Watts");
        baseUnitMapping.put(8, "Joules");
        baseUnitMapping.put(9, "Coulombs");
        baseUnitMapping.put(10, "VA");
        baseUnitMapping.put(11, "Nits");
        baseUnitMapping.put(12, "Lumens");
        baseUnitMapping.put(13, "Lux");
        baseUnitMapping.put(14, "Candelas");
        baseUnitMapping.put(15, "kPa");
        baseUnitMapping.put(16, "PSI");
        baseUnitMapping.put(17, "Newtons");
        baseUnitMapping.put(18, "CFM");
        baseUnitMapping.put(19, "RPM");
        baseUnitMapping.put(20, "Hertz");
        baseUnitMapping.put(21, "Seconds");
        baseUnitMapping.put(22, "Minutes");
        baseUnitMapping.put(23, "Hours");
        baseUnitMapping.put(24, "Days");
        baseUnitMapping.put(25, "Weeks");
        baseUnitMapping.put(26, "Mils");
        baseUnitMapping.put(27, "Inches");
        baseUnitMapping.put(28, "Feet");
        baseUnitMapping.put(29, "Cubic Inches");
        baseUnitMapping.put(30, "Cubic Feet");
        baseUnitMapping.put(31, "Meters");
        baseUnitMapping.put(32, "Cubic Centimeters");
        baseUnitMapping.put(33, "Cubic Meters");
        baseUnitMapping.put(34, "Liters");
        baseUnitMapping.put(35, "Fluid Ounces");
        baseUnitMapping.put(36, "Radians");
        baseUnitMapping.put(37, "Steradians");
        baseUnitMapping.put(38, "Revolutions");
        baseUnitMapping.put(39, "Cycles");
        baseUnitMapping.put(40, "Gravities");
        baseUnitMapping.put(41, "Ounces");
        baseUnitMapping.put(42, "Pounds");
        baseUnitMapping.put(43, "Foot-Pounds");
        baseUnitMapping.put(44, "Ounce-Inches");
        baseUnitMapping.put(45, "Gauss");
        baseUnitMapping.put(46, "Gilberts");
        baseUnitMapping.put(47, "Henries");
        baseUnitMapping.put(48, "Farads");
        baseUnitMapping.put(49, "Ohms");
        baseUnitMapping.put(50, "Siemens");
        baseUnitMapping.put(51, "Moles");
        baseUnitMapping.put(52, "Becquerels");
        baseUnitMapping.put(53, "PPM (parts/million)");
        baseUnitMapping.put(54, "Decibels");
        baseUnitMapping.put(55, "DbA");
        baseUnitMapping.put(56, "DbC");
        baseUnitMapping.put(57, "Grays");
        baseUnitMapping.put(58, "Sieverts");
        baseUnitMapping.put(59, "Color Temperature Degrees K");
        baseUnitMapping.put(60, "Bits");
        baseUnitMapping.put(61, "Bytes");
        baseUnitMapping.put(62, "Words (data)");
        baseUnitMapping.put(63, "DoubleWords");
        baseUnitMapping.put(64, "QuadWords");
        baseUnitMapping.put(65, "Percentage");
        baseUnitMapping.put(66, "Pascals");
    }

    /**
     * Initializes this instance with a given parameter map.
     *
     * @param parameters the parameter map to use
     * @throws CollectionInitializationException
     *
     */
    @Override
    public void initialize() throws CollectionInitializationException {
        if (m_nodeDao == null) {
            m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        }

        if (m_nodeDao == null) {
            logger.error("Node dao should be a non-null value.");
        }

        if (m_vmwareCimDatacollectionConfigDao == null) {
            m_vmwareCimDatacollectionConfigDao = BeanUtils.getBean("daoContext", "vmwareCimDatacollectionConfigDao", VmwareCimDatacollectionConfigDao.class);
        }

        if (m_nodeDao == null) {
            logger.error("vmwareCimDatacollectionConfigDao should be a non-null value.");
        }

        if (m_vmwareConfigDao == null) {
            m_vmwareConfigDao = BeanUtils.getBean("daoContext", "vmwareConfigDao", VmwareConfigDao.class);
        }
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();
        final OnmsNode onmsNode = m_nodeDao.get(agent.getNodeId());
        if (onmsNode == null) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No node found with id: %d", agent.getNodeId()));
        }

        // retrieve the assets
        final String vmwareManagementServer = onmsNode.getAssetRecord().getVmwareManagementServer();
        if (Strings.isNullOrEmpty(vmwareManagementServer)) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No management server is set on node with id %d.",  onmsNode.getId()));
        }
        runtimeAttributes.put(VMWARE_MGMT_SERVER_KEY, vmwareManagementServer);

        final String vmwareManagedObjectId = onmsNode.getForeignId();
        if (Strings.isNullOrEmpty(vmwareManagedObjectId)) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No foreign id is set on node with id %d.",  onmsNode.getId()));
        }
        runtimeAttributes.put(VMWARE_MGED_OBJECT_ID_KEY, vmwareManagedObjectId);

        // retrieve the collection
        final String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "vmware-collection", null));
        final VmwareCimCollection collection = m_vmwareCimDatacollectionConfigDao.getVmwareCimCollection(collectionName);
        if (collection == null) {
            throw new IllegalArgumentException(String.format("VmwareCollector: No collection found with name '%s'.",  collectionName));
        }
        runtimeAttributes.put(VMWARE_COLLECTION_KEY, collection);

        // retrieve the server configuration
        final Map<String, VmwareServer> serverMap = m_vmwareConfigDao.getServerMap();
        if (serverMap == null) {
            throw new IllegalStateException(String.format("VmwareCollector: Error getting vmware-config.xml's server map."));
        }
        final VmwareServer vmwareServer = serverMap.get(vmwareManagementServer);
        if (vmwareServer == null) {
            throw new IllegalStateException(String.format("VmwareCollector: Error getting credentials for VMware management server: %s", vmwareManagementServer));
        }
        runtimeAttributes.put(VMWARE_SERVER_KEY, vmwareServer);
        return runtimeAttributes;
    }

    /**
     * This method collect the data for a given collection agent.
     *
     * @param agent      the collection agent
     * @param parameters the parameters map
     * @return the generated collection set
     * @throws CollectionException
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        final VmwareCimCollection collection = (VmwareCimCollection) parameters.get(VMWARE_COLLECTION_KEY);
        final String vmwareManagementServer = (String) parameters.get(VMWARE_MGMT_SERVER_KEY);
        final String vmwareManagedObjectId = (String) parameters.get(VMWARE_MGED_OBJECT_ID_KEY);
        final VmwareServer vmwareServer = (VmwareServer) parameters.get(VMWARE_SERVER_KEY);

        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withStatus(CollectionStatus.FAILED);

        VmwareViJavaAccess vmwareViJavaAccess = new VmwareViJavaAccess(vmwareServer);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", -1);
        if (timeout > 0) {
            if (!vmwareViJavaAccess.setTimeout(timeout)) {
                logger.warn("Error setting connection timeout for VMware management server '{}'", vmwareManagementServer);
            }
        }

        if (collection.getVmwareCimGroup().length < 1) {
            logger.info("No groups to collect. Returning empty collection set.");
            builder.withStatus(CollectionStatus.SUCCEEDED);
            return builder.build();
        }

        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return builder.build();
        } catch (RemoteException e) {
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return builder.build();
        }

        HostSystem hostSystem = vmwareViJavaAccess.getHostSystemByManagedObjectId(vmwareManagedObjectId);

        String powerState = null;

        if (hostSystem == null) {
            logger.debug("hostSystem=null");
        } else {
            HostRuntimeInfo hostRuntimeInfo = hostSystem.getRuntime();

            if (hostRuntimeInfo == null) {
                logger.debug("hostRuntimeInfo=null");
            } else {
                HostSystemPowerState hostSystemPowerState = hostRuntimeInfo.getPowerState();
                if (hostSystemPowerState == null) {
                    logger.debug("hostSystemPowerState=null");
                } else {
                    powerState = hostSystemPowerState.toString();
                }
            }
        }

        logger.debug("The power state for host system '{}' is '{}'", vmwareManagedObjectId, powerState);

        if ("poweredOn".equals(powerState)) {
            HashMap<String, List<CIMObject>> cimObjects = new HashMap<String, List<CIMObject>>();

            for (final VmwareCimGroup vmwareCimGroup : collection.getVmwareCimGroup()) {

                String cimClass = vmwareCimGroup.getCimClass();

                if (!cimObjects.containsKey(cimClass)) {
                    List<CIMObject> cimList = null;
                    try {
                        cimList = vmwareViJavaAccess.queryCimObjects(hostSystem, cimClass, InetAddressUtils.str(agent.getAddress()));
                    } catch (Exception e) {
                        logger.warn("Error retrieving CIM values from host system '{}'. Error message: '{}'", vmwareManagedObjectId, e.getMessage());
                        return builder.build();
                    } finally {
                        vmwareViJavaAccess.disconnect();
                    }
                    cimObjects.put(cimClass, cimList);
                }

                final List<CIMObject> cimList = cimObjects.get(cimClass);

                if (cimList == null) {
                    logger.warn("Error getting objects of CIM class '{}' from host system '{}'", cimClass, vmwareManagedObjectId);
                    continue;

                }

                String keyAttribute = vmwareCimGroup.getKey();
                String attributeValue = vmwareCimGroup.getValue();
                String instanceAttribute = vmwareCimGroup.getInstance();

                for (CIMObject cimObject : cimList) {
                    boolean addObject = false;

                    if (keyAttribute != null && attributeValue != null) {
                        String cimObjectValue = vmwareViJavaAccess.getPropertyOfCimObject(cimObject, keyAttribute);

                        if (attributeValue.equals(cimObjectValue)) {
                            addObject = true;
                        } else {
                            addObject = false;
                        }
                    } else {
                        addObject = true;
                    }

                    if (addObject) {
                        final String instance = vmwareViJavaAccess.getPropertyOfCimObject(cimObject, instanceAttribute);
                        final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
                        final Resource resource = new DeferredGenericTypeResource(nodeResource,vmwareCimGroup.getResourceType(), instance);
                        for (Attrib attrib : vmwareCimGroup.getAttrib()) {
                            final AttributeType type = attrib.getType();
                            String value = vmwareViJavaAccess.getPropertyOfCimObject(cimObject, attrib.getName());
                            if (valueModifiers.containsKey(attrib.getName())) {
                                String modifiedValue = valueModifiers.get(attrib.getName()).modifyValue(attrib.getName(), value, cimObject, vmwareViJavaAccess);
                                logger.debug("Applying value modifier for instance value " + attrib.getName() + "[" + instance + "]='" + value + "' => '" + modifiedValue + "' for node " + agent.getNodeId());
                                value = modifiedValue;
                            }
                            builder.withAttribute(resource, vmwareCimGroup.getName(), attrib.getAlias(), value, type);
                        }
                    }
                }
            }
            builder.withStatus(CollectionStatus.SUCCEEDED);
        }

        vmwareViJavaAccess.disconnect();

        return builder.build();
    }

    /**
     * Returns the Rrd repository for this object.
     *
     * @param collectionName the collection's name
     * @return the Rrd repository
     */
    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        return m_vmwareCimDatacollectionConfigDao.getRrdRepository(collectionName);
    }

    /**
     * Sets the NodeDao object for this instance.
     *
     * @param nodeDao the NodeDao object to use
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

}
