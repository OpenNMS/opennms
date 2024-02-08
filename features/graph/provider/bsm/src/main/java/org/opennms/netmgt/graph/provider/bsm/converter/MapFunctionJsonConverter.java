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
package org.opennms.netmgt.graph.provider.bsm.converter;

import org.json.JSONObject;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;
import org.opennms.netmgt.graph.rest.api.PropertyConverter;

public class MapFunctionJsonConverter implements PropertyConverter<MapFunction, JSONObject> {

    @Override
    public JSONObject convert(MapFunction mapFunction) {
        final JSONObject jsonMapFunction = new JSONObject();
        jsonMapFunction.put("type", mapFunction.getClass().getSimpleName().toLowerCase());
        mapFunction.accept(new MapFunctionVisitor<Void>() {
            @Override
            public Void visit(Decrease decrease) {
                return null;
            }

            @Override
            public Void visit(Identity identity) {
                return null;
            }

            @Override
            public Void visit(Ignore ignore) {
                return null;
            }

            @Override
            public Void visit(Increase increase) {
                return null;
            }

            @Override
            public Void visit(SetTo setTo) {
                jsonMapFunction.put("severity", setTo.getStatus());
                return null;
            }
        });
        return jsonMapFunction;
    }

    @Override
    public boolean canConvert(Class<MapFunction> type) {
        return MapFunction.class.isAssignableFrom(type);
    }

}
