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
import org.opennms.netmgt.bsm.service.model.functions.reduce.ExponentialPropagation;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverityAbove;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReduceFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.graph.rest.api.PropertyConverter;

public class ReduceFunctionJsonConverter implements PropertyConverter<ReductionFunction, JSONObject> {

    @Override
    public boolean canConvert(Class<ReductionFunction> type) {
        return ReductionFunction.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(ReductionFunction input) {
        final JSONObject jsonOutput = new JSONObject();
        jsonOutput.put("type", input.getClass().getSimpleName().toLowerCase());
        input.accept(new ReduceFunctionVisitor<Void>() {
            @Override
            public Void visit(HighestSeverity highestSeverity) {
                return null;
            }

            @Override
            public Void visit(HighestSeverityAbove highestSeverityAbove) {
                jsonOutput.put("threshold", highestSeverityAbove.getThreshold());
                return null;
            }

            @Override
            public Void visit(Threshold threshold) {
                jsonOutput.put("threshold", threshold.getThreshold());
                return null;
            }

            @Override
            public Void visit(ExponentialPropagation exponentialPropagation) {
                jsonOutput.put("base", exponentialPropagation.getBase());
                return null;
            }
        });
        return jsonOutput;
    }
}
