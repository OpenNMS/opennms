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
package org.opennms.config.upgrade.datacollection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Maps JAXB datacollection objects to values suitable for SQL insertion.
 * Mirrors the serialization logic in DataCollectionConfPersistenceService.
 */
public final class DataCollectionGroupMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DataCollectionGroupMapper() {
    }

    public static String toJson(final Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

    /** Serialize RRD RRAs list to JSON array string. */
    public static String mapRras(final Rrd rrd) {
        if (rrd == null || rrd.getRras() == null) {
            return null;
        }
        return toJson(rrd.getRras());
    }

    /** Serialize MibObj list to JSON. */
    public static String mapMibObjects(final Group group) {
        if (group.getMibObjs() == null || group.getMibObjs().isEmpty()) {
            return "[]";
        }
        return toJson(group.getMibObjs());
    }

    /** Serialize MibObjProperty list to JSON. */
    public static String mapMibObjProperties(final Group group) {
        List<?> props = group.getProperties();
        if (props == null || props.isEmpty()) {
            return null;
        }
        return toJson(props);
    }

    /** Serialize includeGroup names from a Group to JSON. */
    public static String mapIncludeGroups(final Group group) {
        List<String> includes = group.getIncludeGroups();
        if (includes == null || includes.isEmpty()) {
            return null;
        }
        return toJson(includes);
    }

    /** Serialize SystemDef's collect.includeGroups to JSON. */
    public static String mapSystemDefMibGroupNames(final SystemDef sd) {
        List<String> groupNames = Optional.ofNullable(sd.getCollect())
                .map(Collect::getIncludeGroups)
                .orElse(Collections.emptyList());
        return toJson(groupNames);
    }

    /** Serialize IpList addresses to JSON. */
    public static String mapIpAddresses(final SystemDef sd) {
        IpList ipList = sd.getIpList();
        if (ipList == null) {
            return null;
        }
        return toJson(ipList);
    }

    /** Serialize IpList address masks to JSON. */
    public static String mapIpAddressMasks(final SystemDef sd) {
        return Optional.ofNullable(sd.getIpList())
                .map(IpList::getIpAddressMasks)
                .filter(masks -> !masks.isEmpty())
                .map(DataCollectionGroupMapper::toJson)
                .orElse(null);
    }

    /** Get storage strategy class name from ResourceType. */
    public static String mapStorageStrategy(final ResourceType rt) {
        return rt.getStorageStrategy() != null ? rt.getStorageStrategy().getClazz() : null;
    }

    /** Serialize storage strategy parameters to JSON. */
    public static String mapStorageStrategyParams(final ResourceType rt) {
        if (rt.getStorageStrategy() == null || rt.getStorageStrategy().getParameters() == null
                || rt.getStorageStrategy().getParameters().isEmpty()) {
            return null;
        }
        return toJson(rt.getStorageStrategy().getParameters());
    }

    /** Get persistence selector strategy class name from ResourceType. */
    public static String mapPersistenceSelectorStrategy(final ResourceType rt) {
        return rt.getPersistenceSelectorStrategy() != null ? rt.getPersistenceSelectorStrategy().getClazz() : null;
    }

    /** Serialize persistence selector strategy parameters to JSON. */
    public static String mapPersistenceSelectorParams(final ResourceType rt) {
        if (rt.getPersistenceSelectorStrategy() == null || rt.getPersistenceSelectorStrategy().getParameters() == null
                || rt.getPersistenceSelectorStrategy().getParameters().isEmpty()) {
            return null;
        }
        return toJson(rt.getPersistenceSelectorStrategy().getParameters());
    }

    /** Map snmp-collection to source_names JSON array from include-collection elements. */
    public static String mapSourceNames(final List<String> sourceNames) {
        if (sourceNames == null || sourceNames.isEmpty()) {
            return null;
        }
        return toJson(sourceNames);
    }

    /** Get the RRD step from an SnmpCollection. */
    public static int mapRrdStep(final SnmpCollection coll) {
        return coll.getRrd() != null ? coll.getRrd().getStep() : 300;
    }
}
