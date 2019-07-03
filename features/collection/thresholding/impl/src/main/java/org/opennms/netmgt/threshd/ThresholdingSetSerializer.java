/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ThresholdingSetSerializer implements JsonSerializer<Object>, JsonDeserializer<Object> {

    private static final String NODE_ID = "nodeId";

    private static final String HOST_ADDR = "hostAddr";

    private static final String SERVICE_NAME = "serviceName";

    private static final String REPOSITORY = "repository";

    private static final String SERVICE_PARAMS = "svcParams";

    private static final String THRESHOLD_GROUPS = "thresholdGroups";

    private static final String SCHEDULED_OUTAGES = "scheduledOutages";
    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // FIXME
        return null;
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        SerializableThresholdingSet set = (SerializableThresholdingSet) src;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(NODE_ID, set.getNodeId());
        jsonObject.addProperty(HOST_ADDR, set.getHostAddress());
        jsonObject.addProperty(SERVICE_NAME, set.getServiceName());
        jsonObject.add(REPOSITORY, context.serialize(set.getRepository()));
        jsonObject.add(SERVICE_PARAMS, context.serialize(set.getSvcParams()));
        jsonObject.add(SCHEDULED_OUTAGES, context.serialize(set.getScheduledOutages()));
        jsonObject.add(THRESHOLD_GROUPS, serializeThresholdingGroups(set.getThresholdGroups(), context));
        return jsonObject;
    }

    private JsonElement serializeThresholdingGroups(List<ThresholdGroup> thresholdGroups, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (ThresholdGroup group : thresholdGroups) {
            JsonObject jsonObject = new JsonObject();
            /*           
            private String m_name;
            private File m_rrdRepository;
            private ThresholdResourceType m_nodeResourceType;
            private ThresholdResourceType m_ifResourceType;
            private Map<String,ThresholdResourceType> m_genericResourceTypeMap = new HashMap<String,ThresholdResourceType>();
            */
            jsonObject.addProperty("name", group.getName());
            jsonObject.add("repository", context.serialize(group.getRrdRepository()));
            jsonObject.add("nodeResourceType", context.serialize(group.getNodeResourceType()));
            jsonObject.add("ifResourceType", serializeIfResourceType(group.getIfResourceType(), context));

            jsonObject.add("genericResourceMap", context.serialize(group.getGenericResourceTypeMap()));

            array.add(jsonObject);
        }
        return array;
    }

    private JsonElement serializeIfResourceType(ThresholdResourceType ifResourceType, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dsType", ifResourceType.getDsType());
        JsonObject map = new JsonObject();
        for (Entry<String, Set<ThresholdEntity>> entry : ifResourceType.getThresholdMap().entrySet()) {
            map.add(entry.getKey(), serializeThresholdEntities(entry.getValue(), context));
        }
        jsonObject.add("map", map);
        return jsonObject;
    }

    private JsonElement serializeThresholdEntities(Set<ThresholdEntity> entities, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (ThresholdEntity entity : entities) {
            array.add(serializeThresholdEntity(entity, context));
        }
        return array;
    }

    private JsonElement serializeThresholdEntity(ThresholdEntity entity, JsonSerializationContext context) {
        JsonObject map = new JsonObject();
        Map<String, List<ThresholdEvaluatorState>> states = entity.getThresholdEvaluatorStates();
        for (Entry<String, List<ThresholdEvaluatorState>> entry : states.entrySet()) {
            // The 'default' entryset is keyed by 'null'
            String key = entry.getKey() == null ? "null" : entry.getKey();
            map.add(key, serializeEvaluatorStates(entry.getValue(), context));
        }
        return map;
    }

    private JsonElement serializeEvaluatorStates(List<ThresholdEvaluatorState> thresholdEvaluatorStates, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (ThresholdEvaluatorState state : thresholdEvaluatorStates) {
            array.add(serializeEvaluatorState(state, context));
        }
        return array;
    }

    private JsonElement serializeEvaluatorState(ThresholdEvaluatorState state, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        // FIXME - need to expose more info from State - jsonObject.addProperty("isArmed", state.isArmed());
        jsonObject.addProperty("isTriggered", state.isTriggered());
        // FIXME - more to serialize
        return jsonObject;
    }

}
