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
import org.opennms.netmgt.collection.api.AttributeGroupType;
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
public class PointerLikeIndexPropertyExtender implements SnmpPropertyExtender {

	private static final Logger LOG = LoggerFactory.getLogger(PointerLikeIndexPropertyExtender.class);

    protected static final String SOURCE_TYPE 			= "source-type";
    protected static final String SOURCE_ALIAS 			= "source-alias";
    
    protected static final String SOURCE_èPOINTER_INDEX = "source-index-pointer";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpPropertyExtender#getTargetAttribute(java.util.List, org.opennms.netmgt.collectd.SnmpCollectionResource, org.opennms.netmgt.config.datacollection.MibObjProperty)
     */
    @Override
    public SnmpAttribute getTargetAttribute(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource, MibObjProperty property) {
        final String sourceType = property.getParameterValue(SOURCE_TYPE);
        // sourcetype peutèêtre "if" : dans ce cas -6> tabkle ifentry
        if (StringUtils.isBlank(sourceType)) {
            LOG.warn("Cannot execute the Index Split property extender because: missing parameter {}", SOURCE_TYPE);
            return null;
        }
        //lorsqu'on utilise if pour sourceTYpe, on peut utiliser interfaceLabel
        // comme nom de valeur
        final String sourceAlias = property.getParameterValue(SOURCE_ALIAS);
        if (StringUtils.isBlank(sourceAlias)) {
            LOG.warn("Cannot execute the Index Split property extender because: missing parameter {}", SOURCE_ALIAS);
            return null;
        }
        final String pointerIndex = property.getParameterValue(SOURCE_èPOINTER_INDEX);
        if (StringUtils.isBlank(pointerIndex)) {
            LOG.warn("Cannot execute the Index Split property extender because: missing parameter {}", SOURCE_èPOINTER_INDEX);
            return null;
        }
        
        final String indexInSourceTable = getIndirectIndex(sourceAttributes,  targetResource , pointerIndex);
        if (indexInSourceTable != null) {
            final SnmpValue value = getValueInSourceTable(sourceAttributes, indexInSourceTable, sourceType, sourceAlias);           
            if (value != null) {
    			final MibPropertyAttributeType type = getAttributeType(targetResource,property);
            	return new SnmpAttribute(targetResource, type, value); 
            }
        }
        return null;
    }
    
	final private MibPropertyAttributeType getAttributeType(SnmpCollectionResource targetResource, MibObjProperty property) {
		final AttributeGroupType	groupType 		= targetResource.getGroupType(property.getGroupName());
		final ResourceType 			resourceType 	= targetResource.getResourceType();
		
		return new MibPropertyAttributeType(resourceType, property, groupType);
	}

	final private String getIndirectIndex(List<CollectionAttribute> sourceAttributes, SnmpCollectionResource targetResource,
			String pointerIndex) {
    	final String currentSourceTypeName	= targetResource.getResourceTypeName();
		final String currentInstance		= targetResource.getInstance();
		
    	final Optional<CollectionAttribute> target 	=  
    			sourceAttributes.stream().filter(a -> matches(currentSourceTypeName,pointerIndex , currentInstance, a)).findFirst();
    	if ((target == null) ||  (! target.isPresent())){
    		LOG.warn("Unable to find an indirect value in resource {} for index {} and instance {}", currentSourceTypeName, pointerIndex, currentInstance);
    		return null;
    	}
    	String result = target.get().getStringValue();
        LOG.debug("{} value {} points to {}  ",pointerIndex, currentInstance, result);

    	return result;
	}
	private SnmpValue getValueInSourceTable(List<CollectionAttribute> sourceAttributes, String indexInSourceTable,
			String sourceType, String sourceAlias) {
		final Optional<CollectionAttribute> target = 
				sourceAttributes.stream().filter(a -> matchesValue(sourceType, sourceAlias, indexInSourceTable, a)).findFirst();
		
		if ((target == null) ||  (! target.isPresent())){
        	LOG.debug("Unable to find a value in resource  {} for index {} and instance {}", sourceType, sourceAlias, indexInSourceTable);
        	return null;
		}
		final  SnmpValue result = 	getSnmpValue(target);
		LOG.debug("Value in source table is" + result);
		return result;
	}
	
	protected SnmpValue getSnmpValue(final Optional<CollectionAttribute> target2) {
		final SnmpValue value = SnmpUtils.getValueFactory().getOctetString(target2.get().getStringValue().getBytes());
		return value;
	}

    protected boolean matchesValue(final String sourceType, final String sourceAlias, final String index, final CollectionAttribute a) {
    	return  matches(sourceType,sourceAlias,index,a);
    }
    /**
     * Matches.final 
     *
     * @param sourceType the source type
     * @param sourceAlias the source alias
     * @param index the resource index to check
     * @param a the collection attribute to check
     * @return true, if successful
     */
    private boolean matches(final String sourceType, final String sourceAlias, final String index, final CollectionAttribute a) {
        final CollectionResource r 	= a.getResource();
        return a.getName().equals(sourceAlias) && r.getResourceTypeName().equals(sourceType) && r.getInstance().equals(index);
    }
    
    
}
