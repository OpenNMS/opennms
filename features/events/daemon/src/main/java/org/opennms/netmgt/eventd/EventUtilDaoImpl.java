/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.LikeRestriction;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.MetricRegistry;

public class EventUtilDaoImpl extends AbstractEventUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EventUtilDaoImpl.class);

	@Autowired
	private NodeDao nodeDao;
	
	@Autowired
	private AssetRecordDao assetRecordDao;
	
	@Autowired
	private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private HwEntityDao hwEntityDao;

	private final Pattern ASSET_PARM_PATTERN = Pattern.compile("^asset\\[(.*)\\]$");

	private final Pattern HW_PARM_PATTERN = Pattern.compile("^hardware\\[(.*):(.*)\\]$");

	private final static Map<String, PropertyDescriptor> assetDescriptorsByName = getDescriptorsForStrings(OnmsAssetRecord.class);

	private final static Map<String, PropertyDescriptor> hwEntityDescriptorsByName = getDescriptorsForStrings(OnmsHwEntity.class);

    public EventUtilDaoImpl() { }

    public EventUtilDaoImpl(MetricRegistry registry) {
        super(registry);
    }

    @Override
    public String getNodeLabel(long nodeId) {
        return nodeDao.getLabelForId(Integer.valueOf((int)nodeId));
    }

    @Override
    public String getNodeLocation(long nodeId) {
        return nodeDao.getLocationForId(Integer.valueOf((int)nodeId));
    }

    @Override
    public String getForeignSource(long nodeId) {
        OnmsNode node = nodeDao.get((int)nodeId);
        if (node != null) {
            return node.getForeignSource();
        }
        return null;
    }

    @Override
    public String getForeignId(long nodeId) {
        OnmsNode node = nodeDao.get((int)nodeId);
        return node == null ? null : node.getForeignId();
    }

    @Override
    public String getIfAlias(long nodeId, String ipaddr) {
        OnmsIpInterface iface = ipInterfaceDao.findByNodeIdAndIpAddress((int)nodeId, ipaddr);
        if (iface != null && iface.getSnmpInterface() != null) {
            return iface.getSnmpInterface().getIfAlias();
        } else {
            return null;
        }
    }

    @Override
    public String getAssetFieldValue(String parm, long nodeId) {
        final Matcher matcher = ASSET_PARM_PATTERN.matcher(parm);
        if (!matcher.matches()) {
            LOG.warn("Unsupported asset field parameter '{}'.", parm);
            return null;
        }
        final String assetField = matcher.group(1).toLowerCase();

        OnmsAssetRecord assetRecord = assetRecordDao.findByNodeId((int)nodeId);
        if (assetRecord == null) {
            return null;
        }

        return getStringPropertyByName(assetField, assetRecord, assetDescriptorsByName);
    }

    @Override
    public String getHardwareFieldValue(String parm, long nodeId) {
        final Matcher matcher = HW_PARM_PATTERN.matcher(parm);
        if (!matcher.matches()) {
            LOG.warn("Unsupported hardware field parameter '{}'.", parm);
            return null;
        }
        final String hwFieldSelector = matcher.group(1);
        final String hwField = matcher.group(2).toLowerCase();

        // Retrieve the entity with a like query
        if (hwFieldSelector.startsWith("~")) {
            final String likeQuery = hwFieldSelector.substring(1);
            LOG.debug("Retrieving hardware field value {} on {} with like query {}",
                    parm, nodeId, likeQuery);
            Criteria criteria = new Criteria(OnmsHwEntity.class)
                .setAliases(Arrays.asList(new Alias[] {
                        new Alias("node","node", JoinType.LEFT_JOIN),
                }))
                .addRestriction(new EqRestriction("node.id", (int)nodeId))
                .addRestriction(new LikeRestriction("entPhysicalName", likeQuery))
                .setOrders(Arrays.asList(new Order[] {
                        Order.desc("id")
                }));
            List<OnmsHwEntity> hwEntities = hwEntityDao.findMatching(criteria);
            System.err.println(hwEntities);
            if (hwEntities.size() < 1) {
                return null;
            }
            return getStringPropertyByName(hwField, hwEntities.get(0), hwEntityDescriptorsByName);
        }

        // Retrieve the entity by index if the select is an integer
        try {
            int index = Integer.parseInt(hwFieldSelector);
            OnmsHwEntity hwEntity = hwEntityDao.findEntityByIndex((int)nodeId, index);
            if (hwEntity == null) {
                // No entry with this index
                return null;
            }
            return getStringPropertyByName(hwField, hwEntity, hwEntityDescriptorsByName);
        } catch (NumberFormatException e) {
            // pass
        }

        // Retrieve the entity by name
        OnmsHwEntity hwEntity = hwEntityDao.findEntityByName((int)nodeId, hwFieldSelector);
        if (hwEntity == null) {
            // No entry with this name
            return null;
        }
        return getStringPropertyByName(hwField, hwEntity, hwEntityDescriptorsByName);
    }

    /**
     * This method is used to convert the event host into a hostname id by
     * performing a lookup in the database. If the conversion is successful then
     * the corresponding hostname will be returned to the caller.
     * 
     * @param nodeId Node ID
     * @param hostip The event host
     * 
     * @return The hostname
     */
    @Override
    public String getHostName(final int nodeId, final String hostip) {

        OnmsIpInterface ints = ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, hostip);
        if (ints == null) {
            return hostip;
        } else {
            final String hostname = ints.getIpHostName();
            return (hostname == null) ? hostip : hostname;
        }
    }

    @Override
    public String expandParms(String inp, Event event) {
        return super.expandParms(inp, event, null);
    }

    @Override
    public String expandParms(String input, Event event, Map<String, Map<String, String>> decode) {
        return super.expandParms(input, event, decode);
    }

    /**
     * Retrieves the property with the given name on the bean.
     *
     * Returns null if there is no such property, or if there's an
     * error in retrieving it.
     */
    private static String getStringPropertyByName(String name, Object bean, Map<String, PropertyDescriptor> map) {
        PropertyDescriptor propertyDescr = map.get(name);
        if (propertyDescr == null) {
            return null;
        }

        try {
            return (String)PropertyUtils.getProperty(bean, propertyDescr.getName());
        } catch (IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            LOG.error("Retrieving propery {} on {} by name failed.", propertyDescr, bean);
            return null;
        }
    }

    /**
     * Retrieves the list of PropertyDescriptor that are of type String
     * and maps them with their lower-case name.
     */
    private static Map<String, PropertyDescriptor> getDescriptorsForStrings(Class<?> clazz) {
        Map<String, PropertyDescriptor> descriptorsByName = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(clazz)) {
            if (pd.getPropertyType() == String.class) {
                descriptorsByName.put(pd.getName().toLowerCase(), pd);
            }
        }
        return descriptorsByName;
    }
}
