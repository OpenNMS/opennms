/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.snmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsEntityAlias;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EntityPhysicalTableTracker.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EntityPhysicalTableTracker extends TableTracker {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EntityPhysicalTableTracker.class);

    /** The entities. */
    private List<OnmsHwEntity> entities = new ArrayList<>();

    /** The entity aliases. */
    private Map<Integer, SortedSet<OnmsEntityAlias>> aliases = new HashMap<>();

    /** The vendor attributes. */
    private Map<SnmpObjId, HwEntityAttributeType> vendorAttributes;

    /** The replacement map. */
    private Map<String,String> replacementMap;

    /**
     * The Constructor.
     *
     * @param vendorAttributes the vendor attributes
     * @param oids the SNMP OIDs to collect
     * @param replacementMap the replacement map
     */
    public EntityPhysicalTableTracker(Map<SnmpObjId, HwEntityAttributeType> vendorAttributes, SnmpObjId[] oids, Map<String,String> replacementMap) {
        super(oids);
        this.vendorAttributes = vendorAttributes;
        this.replacementMap = replacementMap;
    }

    /**
     * The Constructor.
     *
     * @param rowProcessor the row processor
     * @param oids the OIDs
     */
    public EntityPhysicalTableTracker(RowCallback rowProcessor, SnmpObjId[] oids) {
        super(rowProcessor, oids);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.snmp.TableTracker#createRowResult(int, org.opennms.netmgt.snmp.SnmpInstId)
     */
    @Override
    public SnmpRowResult createRowResult(int columnCount, SnmpInstId instance) {
        LOG.debug("createRowResult: processing instance {}", instance);
        return new EntityPhysicalTableRow(columnCount, instance);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.snmp.TableTracker#rowCompleted(org.opennms.netmgt.snmp.SnmpRowResult)
     */
    @Override
    public void rowCompleted(SnmpRowResult row) {
        // Only entAliases are expected to be mutli-dimentsional
        if (row.getInstance().length() > 1) {
            entAliasRowCompleted(row);
        } else {
           entPhysicalRowCompleted(row);
        }
    }

    private void entPhysicalRowCompleted(SnmpRowResult row) {
        OnmsHwEntity entity = ((EntityPhysicalTableRow) row).getOnmsHwEntity(vendorAttributes, replacementMap);
        LOG.debug("rowCompleted: found entity {}, index: {}, parent: {}", entity.getEntPhysicalName(), entity.getEntPhysicalIndex(), entity.getEntPhysicalContainedIn());
        if (entity.getEntPhysicalContainedIn() != null && entity.getEntPhysicalContainedIn() > 0) {
            OnmsHwEntity parent = getParent(entity.getEntPhysicalContainedIn().intValue());
            if (parent != null) {
                LOG.debug("rowCompleted: adding child index {} to parent index {}", entity.getEntPhysicalIndex(), parent.getEntPhysicalIndex());
                parent.addChildEntity(entity);
            }
        }
        entities.add(entity);        
    }

    /**
     * Convert a SnmpRowResult to OnmsEntityAlias results and track them under each entry.
     * 
     * @param row
     */
    private void entAliasRowCompleted(SnmpRowResult row) {
        // Alias row instances in the the form '1012.0'
        int[] instance = row.getInstance().getIds();
        int entAliasEntry = instance[0];
        int entAliasIndex = instance[1];
        SortedSet<OnmsEntityAlias> aliasSet = aliases.get(entAliasEntry);
        if (aliasSet == null) {
            aliasSet = new TreeSet<>();
            aliases.put(entAliasEntry, aliasSet);
        }
        for (SnmpResult result : row.getResults()) {
            if (!EntityPhysicalTableRow.entAliasMappingTable.equals(result.getBase())) {
                throw new IllegalStateException("Result is not an entAlias result: " + result);
            }
            aliasSet.add(new OnmsEntityAlias(entAliasIndex, result.getValue().toString()));
            LOG.debug("rowCompleted from entAliasMappingTable: found entry {} index: {} oid: {}", entAliasEntry,  entAliasIndex, result.getValue());
        }
    }

    /**
     * Gets the root entity.
     *
     * @return the root entity
     */
    public OnmsHwEntity getRootEntity() {
        OnmsHwEntity root = null;
        for (OnmsHwEntity entity : entities) {
            // Need to attach all aliases to their respective entries before returning the root.
            if (aliases.get(entity.getEntPhysicalIndex()) != null) {
                LOG.debug("Adding entAliasMapping: {} to entity {} ", aliases.get(entity.getEntPhysicalIndex()),  entity.getEntPhysicalIndex());
                entity.setEntAliases(aliases.get(entity.getEntPhysicalIndex()));
            }

            if (root == null && entity.isRoot()) {
                root = entity;
            }
        }
        return root;
    }

    /**
     * Gets the parent.
     *
     * @param parentId the parent ID
     * @return the parent
     */
    private OnmsHwEntity getParent(int parentId) {
        for (OnmsHwEntity e : entities) {
            if (e.getEntPhysicalIndex() != null && e.getEntPhysicalIndex().intValue() == parentId) {
                return e;
            }
        }
        return null;
    }
}
