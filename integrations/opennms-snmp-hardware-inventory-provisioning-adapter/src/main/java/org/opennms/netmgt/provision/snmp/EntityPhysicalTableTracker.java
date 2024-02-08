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
package org.opennms.netmgt.provision.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
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
     * Gets the root entity.
     *
     * @return the root entity
     */
    public OnmsHwEntity getRootEntity() {
        for (OnmsHwEntity entity : entities) {
            if (entity.isRoot()) {
                return entity;
            }
        }
        return null;
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
