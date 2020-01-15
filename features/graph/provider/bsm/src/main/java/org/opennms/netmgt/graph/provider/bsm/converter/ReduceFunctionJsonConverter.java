/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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
