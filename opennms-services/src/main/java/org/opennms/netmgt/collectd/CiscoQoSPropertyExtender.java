/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CiscoQoSPropertyExtender.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class CiscoQoSPropertyExtender implements SnmpPropertyExtender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(CiscoQoSPropertyExtender.class);

    /** The Constant OBJ_QOS_CONF_INDEX.
     *  cbQosObjectsTable::cbQosConfigIndex = 1.3.6.1.4.1.9.9.166.1.5.1.1.2
     */
    private final static String OBJ_QOS_CONF_INDEX = "cbQosConfigIndex";

    /** The Constant OBJ_QOS_CONF_TYPE.
     *  cbQosObjectsTable::cbQosObjectsType = .1.3.6.1.4.1.9.9.166.1.5.1.1.3
     */
    private final static String OBJ_QOS_CONF_TYPE = "cbQosObjectsType";

    /** The Constant OBJ_QOS_PARENT_INDEX.
     *  cbQosObjectsTable::cbQosParentObjectsIndex = .1.3.6.1.4.1.9.9.166.1.5.1.1.4 
     */
    private final static String OBJ_QOS_PARENT_INDEX = "cbQosParentObjectsIndex";

    /** The Constant OBJ_QOS_INTF_INDEX.
     *  cbQosServicePolicyTable::cbQosIfIndex = .1.3.6.1.4.1.9.9.166.1.1.1.1.4
     */
    private final static String OBJ_QOS_INTF_INDEX = "cbQosIfIndex";

    /** The Constant OBJ_QOS_POLICY_NAME.
     *  cbQosPolicyMapCfgTable::cbQosPolicyMapName = .1.3.6.1.4.1.9.9.166.1.6.1.1.1
     */
    private final static String OBJ_QOS_POLICY_NAME = "cbQosPolicyMapName";

    /** The Constant OBJ_QOS_CLASS_NAME.
     *  cbQosCMCfgTable::cbQosCMName = .1.3.6.1.4.1.9.9.166.1.7.1.1.1 
     */
    private final static String OBJ_QOS_CLASS_NAME = "cbQosCMName";

    /** The Constant OBJ_INTF_NAME.
     *  ifTable::ifName
     */
    private final static String OBJ_INTF_NAME = "interfaceName";

    /** The Constant OBJ_INTF_ALIAS.
     *  ifTable::ifAlias
     *  It cannot be named ifAlias because it is invalid for org.opennms.netmgt.collectd.IfInfo (the instance/ifIndex will be null) 
     */
    private final static String OBJ_INTF_ALIAS = "interfaceAlias";

    /** The Constant TYPE_QOS_CONF_INDEX. */
    private final static String TYPE_QOS_CONF_INDEX = "cbQosObjectsEntry";

    /** The Constant TYPE_QOS_CONF_TYPE. */
    private final static String TYPE_QOS_CONF_TYPE = "cbQosObjectsEntry";

    /** The Constant TYPE_QOS_PARENT_INDEX. */
    private final static String TYPE_QOS_PARENT_INDEX = "cbQosObjectsEntry";

    /** The Constant TYPE_QOS_INTF_INDEX. */
    private final static String TYPE_QOS_INTF_INDEX = "cbQosServicePolicyEntry";

    /** The Constant TYPE_QOS_POLICY_NAME. */
    private final static String TYPE_QOS_POLICY_NAME = "cbQosPolicyMapCfgEntry";

    /** The Constant TYPE_QOS_CLASS_NAME. */
    private final static String TYPE_QOS_CLASS_NAME = "cbQosCMCfgEntry";

    /** The Constant PARAM_TARGET_PROPERTY. */
    private final static String PARAM_TARGET_PROPERTY = "target-property";

    /** The Constant PARAM_POLICY_NAME. */
    private final static String PARAM_POLICY_NAME = "policyName";

    /** The Constant PARAM_CLASSMAP_NAME. */
    private final static String PARAM_CLASSMAP_NAME = "classMapName";

    /** The Constant PARAM_INTERFACE__NAME. */
    private final static String PARAM_INTERFACE__NAME = "interfaceName";

    /** The Constant PARAM_INTERFACE_ALIAS. */
    private final static String PARAM_INTERFACE_ALIAS = "interfaceAlias";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpPropertyExtender#getTargetAttribute(java.util.List, org.opennms.netmgt.collectd.SnmpCollectionResource, org.opennms.netmgt.config.datacollection.MibObjProperty)
     */
    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        final String targetProperty = property.getParameterValue(PARAM_TARGET_PROPERTY);
        if (targetProperty == null) {
            LOG.error("CiscoQoS getTargetAttribute: parameter {} is required. Expected values: policyName, classMapName, interfaceAlias, interfaceName", PARAM_TARGET_PROPERTY);
            return null;
        }
        AttributeGroupType groupType = targetResource.getGroupType(property.getGroupName());
        if (groupType == null) {
            LOG.error("CiscoQoS getTargetAttribute: can't find the attribute type name associated with the property {}", property);
            return null;
        }

        String oidIndex = targetResource.getInstance();
        String[] indexes = oidIndex.split("\\.");
        if (indexes.length < 2) {
            LOG.warn("CiscoQoS getTargetAttribute: Malformed Cisco QoS Object Index {} for {}", oidIndex, targetResource);
            return null;
        }
        final Map<String,String> parentIndices = getAttributeMap(sourceAttributes, property.getParameterValue(TYPE_QOS_PARENT_INDEX, TYPE_QOS_PARENT_INDEX), property.getParameterValue(OBJ_QOS_PARENT_INDEX, OBJ_QOS_PARENT_INDEX));
        if (parentIndices.isEmpty()) {
            LOG.warn("CiscoQoS getTargetAttribute: Can't find parent indexes {} for type {}", OBJ_QOS_CONF_INDEX, TYPE_QOS_PARENT_INDEX);
            return null;
        }
        final Map<String,String> configTypes = getAttributeMap(sourceAttributes, property.getParameterValue(TYPE_QOS_CONF_TYPE, TYPE_QOS_CONF_TYPE), property.getParameterValue(OBJ_QOS_CONF_TYPE, OBJ_QOS_CONF_TYPE));
        if (configTypes.isEmpty()) {
            LOG.warn("CiscoQoS getTargetAttribute: Can't find config types {} for type {}", OBJ_QOS_CONF_TYPE, TYPE_QOS_CONF_TYPE);
            return null;
        }

        String policyIndex = indexes[0];
        String objectsIndex = indexes[1];
        String parentIndex = objectsIndex;
        String searchIndex = null;
        boolean found = false;
        do {
            searchIndex = policyIndex + '.' + parentIndex;
            parentIndex =  parentIndices.get(searchIndex);
            String type = configTypes.get(searchIndex);
            found = "1".equals(type); // Is a policy
            LOG.debug("CiscoQoS getTargetAttribute: parent index for {} is {}, is a policy? {}", searchIndex, parentIndex, found);
        } while (!found || parentIndex == null);
        LOG.debug("CiscoQoS getTargetAttribute: retrieving policy config index for {}", searchIndex);

        String targetValue = null;

        final String confIndex_TP = property.getParameterValue(TYPE_QOS_CONF_INDEX, TYPE_QOS_CONF_INDEX);
        final String confIndex_ID = property.getParameterValue(OBJ_QOS_CONF_INDEX, OBJ_QOS_CONF_INDEX);
        final String intfIndex_TP = property.getParameterValue(TYPE_QOS_INTF_INDEX, TYPE_QOS_INTF_INDEX);
        final String intfIndex_ID = property.getParameterValue(OBJ_QOS_INTF_INDEX, OBJ_QOS_INTF_INDEX);

        switch (targetProperty) {

        case PARAM_POLICY_NAME:
            String policyConfigIndex = getAttributeValue(sourceAttributes, searchIndex, confIndex_TP, confIndex_ID);
            if (policyConfigIndex == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find policy for index {}", searchIndex);
                return null;
            }
            final String policyName_TP = property.getParameterValue(TYPE_QOS_POLICY_NAME, TYPE_QOS_POLICY_NAME);
            final String policyName_ID = property.getParameterValue(OBJ_QOS_POLICY_NAME, OBJ_QOS_POLICY_NAME);
            LOG.debug("CiscoQoS getTargetAttribute: found policy config index {}", policyConfigIndex);
            String policyName = getAttributeValue(sourceAttributes, policyConfigIndex, policyName_TP, policyName_ID);
            if (policyName == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find policy name using index {} for resource {} using {}::{}", policyConfigIndex, targetResource, policyName_TP, policyName_ID);
                return null;
            }
            LOG.debug("CiscoQoS getTargetAttribute: policyIndex={}, policyConfigIndex={}, policyName={}", policyIndex, policyConfigIndex, policyName);
            targetValue = policyName;
            break;

        case PARAM_CLASSMAP_NAME:
            String classMapCfgIndex = getAttributeValue(sourceAttributes, oidIndex, confIndex_TP, confIndex_ID);
            if (classMapCfgIndex == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find class-map config index for resource {}", targetResource);
                return null;
            }
            final String classMapName_TP = property.getParameterValue(TYPE_QOS_CLASS_NAME, TYPE_QOS_CLASS_NAME);
            final String classMapName_ID = property.getParameterValue(OBJ_QOS_CLASS_NAME, OBJ_QOS_CLASS_NAME);
            String classMapName = getAttributeValue(sourceAttributes, classMapCfgIndex, classMapName_TP, classMapName_ID);
            if (classMapName == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find class-map name using index {} for resource {} using {}::{}", classMapCfgIndex, targetResource, classMapName_TP, classMapName_ID);
                return null;
            }
            LOG.debug("CiscoQoS getTargetAttribute: classMapCfgIndex={}, classMapName={}", classMapCfgIndex, classMapName);
            targetValue = classMapName;
            break;

        case PARAM_INTERFACE__NAME:
            String interfaceIndex = getAttributeValue(sourceAttributes, policyIndex, intfIndex_TP, intfIndex_ID);
            if (interfaceIndex == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find interface ifIndex for resource {}", targetResource);
                return null;
            }
            final String intfName_ID = property.getParameterValue(OBJ_INTF_NAME, OBJ_INTF_NAME);
            String ifName = getAttributeValue(sourceAttributes, interfaceIndex, CollectionResource.RESOURCE_TYPE_IF, intfName_ID);
            if (ifName == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find ifName using ifIndex {} for resource {} using {}::{}", interfaceIndex, targetResource, CollectionResource.RESOURCE_TYPE_IF, intfName_ID);
                return null;
            }
            targetValue = ifName;
            break;

        case PARAM_INTERFACE_ALIAS:
            interfaceIndex = getAttributeValue(sourceAttributes, policyIndex, intfIndex_TP, intfIndex_ID);
            if (interfaceIndex == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find interface ifIndex for resource {}", targetResource);
                return null;
            }
            final String intfAlias_ID = property.getParameterValue(OBJ_INTF_ALIAS, OBJ_INTF_ALIAS);
            String ifAlias = getAttributeValue(sourceAttributes, interfaceIndex, CollectionResource.RESOURCE_TYPE_IF, intfAlias_ID);
            if (ifAlias == null) {
                LOG.warn("CiscoQoS getTargetAttribute: Can't find ifAlias using ifIndex {} for resource {} using {}::{}", interfaceIndex, targetResource, CollectionResource.RESOURCE_TYPE_IF, intfAlias_ID);
                return null;
            }
            targetValue = ifAlias;
            break;
        }

        if (targetValue == null) {
            LOG.warn("CiscoQoS getTargetAttribute: an unexpected error has happened. Check the parameters on your datacollection-configuration for the property {}", property.getAlias());
            return null;
        }
        MibPropertyAttributeType type = new MibPropertyAttributeType(targetResource.getResourceType(), property, groupType);
        SnmpValue value = SnmpUtils.getValueFactory().getOctetString(targetValue.getBytes());
        return new SnmpAttribute(targetResource, type, value);
    }

    /**
     * Gets the attribute map.
     *
     * @param sourceAttributes the source attributes
     * @param type the resource type
     * @param objName the object name
     * @return the attribute map
     */
    private Map<String, String> getAttributeMap(List<CollectionAttribute> sourceAttributes, String type, String objName) {
        Map<String,String> attributes = new HashMap<String,String>();
        sourceAttributes.stream().filter(a -> objName.equals(a.getAttributeType().getName()) && type.equals(a.getResource().getResourceTypeName())).forEach(a -> attributes.put(a.getResource().getInstance(), a.getStringValue()));
        return attributes;
    }

    /**
     * Gets the attribute value.
     *
     * @param sourceAttributes the source attributes
     * @param index the index
     * @param type the resource type
     * @param attrName the attribute name
     * @return the attribute value
     */
    private String getAttributeValue(List<CollectionAttribute> sourceAttributes, String index, String type, String attrName) {
        Optional<CollectionAttribute> attr = sourceAttributes.stream().filter(a -> attrName.equals(a.getAttributeType().getName()) && type.equals(a.getResource().getResourceTypeName()) && index.equals(a.getResource().getInstance())).findFirst();
        return attr.isPresent() ? attr.get().getStringValue() : null;
    }

}
