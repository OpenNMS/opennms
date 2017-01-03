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

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;

import org.opennms.netmgt.collectd.SnmpCollectionResource;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class IndexSplitPropertyExtender.
 * 
 * @author <a href="mailto:jm+opennms@kubek.fr/">J.-M. Kubek</a>
 */
public class PointerToIfIndexPropertyExtender extends PointerLikeIndexPropertyExtender implements SnmpPropertyExtender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(PointerToIfIndexPropertyExtender.class);
    
    private static final String IFENTRY_INTERNAL_SOURCE_TYPE 	= "if";
    private static final String IFLABEL_DUMMY_SOURCE_ALIAS 		= "interfaceLabel@Internal";
    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpPropertyExtender#getTargetAttribute(java.util.List, org.opennms.netmgt.collectd.SnmpCollectionResource, org.opennms.netmgt.config.datacollection.MibObjProperty)
     */
    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
    	final String pointerIndex = property.getParameterValue(SOURCE_èPOINTER_INDEX);
        if (StringUtils.isBlank(pointerIndex)) {
            LOG.warn("Cannot execute the pointer to IfIndex  extender because: missing parameter {}", SOURCE_èPOINTER_INDEX);
            return null;
        }
    	property.addParameter(SOURCE_TYPE, IFENTRY_INTERNAL_SOURCE_TYPE);
    	property.addParameter(SOURCE_ALIAS, IFLABEL_DUMMY_SOURCE_ALIAS);
    	return super.getTargetAttribute(sourceAttributes, targetResource,property);
    	
    }
    @Override
	protected SnmpValue getSnmpValue(Optional<CollectionAttribute> target2) {
		String interfaceLabel = target2.get().getResource().getInterfaceLabel();
		SnmpValue value = SnmpUtils.getValueFactory().getOctetString(interfaceLabel.getBytes());
		return value;
	}
    @Override
    //get whetever sourcealias we collect in "if" sourceType
    protected boolean matchesValue(final String sourceType, final String sourceAlias, final String index, final CollectionAttribute a) {
        final CollectionResource r = a.getResource();
        //LOG.error("name {} ResourceTypeName {} instance {}",a.getName(),r.getResourceTypeName(),r.getInstance());
        return r.getResourceTypeName().equals(IFENTRY_INTERNAL_SOURCE_TYPE) && r.getInstance().equals(index);
    }
}
