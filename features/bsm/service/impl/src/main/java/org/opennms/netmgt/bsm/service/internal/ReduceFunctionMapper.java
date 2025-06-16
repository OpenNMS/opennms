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
package org.opennms.netmgt.bsm.service.internal;

import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ExponentialPropagationEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityAboveEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionEntityVisitor;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ThresholdEntity;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ExponentialPropagation;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverityAbove;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReduceFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;

public class ReduceFunctionMapper {

    private static final ReduceFunctionVisitor<AbstractReductionFunctionEntity> serviceToPersistenceMapping = new ReduceFunctionVisitor<AbstractReductionFunctionEntity>() {

        @Override
        public AbstractReductionFunctionEntity visit(HighestSeverity highestSeverity) {
            return new HighestSeverityEntity();
        }

        @Override
        public AbstractReductionFunctionEntity visit(HighestSeverityAbove highestSeverityAbove) {
            HighestSeverityAboveEntity entity = new HighestSeverityAboveEntity();
            entity.setThreshold(highestSeverityAbove.getThreshold().ordinal());
            return entity;
        }

        @Override
        public AbstractReductionFunctionEntity visit(Threshold threshold) {
            ThresholdEntity entity = new ThresholdEntity();
            entity.setThreshold(threshold.getThreshold());
            return entity;
        }

        @Override
        public AbstractReductionFunctionEntity visit(ExponentialPropagation exponentialPropagation) {
            ExponentialPropagationEntity entity = new ExponentialPropagationEntity();
            entity.setBase(exponentialPropagation.getBase());
            return entity;
        }
    };

    private static final ReductionFunctionEntityVisitor<ReductionFunction> persistenceToServiceMapping = new ReductionFunctionEntityVisitor<ReductionFunction>() {
        @Override
        public ReductionFunction visit(HighestSeverityAboveEntity highestSeverityAboveEntity) {
            HighestSeverityAbove result = new HighestSeverityAbove();
            result.setThreshold(Status.get(highestSeverityAboveEntity.getThreshold()));
            return result;
        }

        @Override
        public ReductionFunction visit(HighestSeverityEntity highestSeverityEntity) {
            return new HighestSeverity();
        }

        @Override
        public ReductionFunction visit(ThresholdEntity thresholdEntity) {
            Threshold result = new Threshold();
            result.setThreshold(thresholdEntity.getThreshold());
            return result;
        }

        @Override
        public ReductionFunction visit(ExponentialPropagationEntity exponentialPropagationEntity) {
            ExponentialPropagation result = new ExponentialPropagation();
            result.setBase(exponentialPropagationEntity.getBase());
            return result;
        }
    };

    public ReductionFunction toServiceFunction(AbstractReductionFunctionEntity reductionFunctionEntity) {
        if (reductionFunctionEntity == null) {
            return null;
        }
        ReductionFunction reductionFunction = reductionFunctionEntity.accept(persistenceToServiceMapping);
        if (reductionFunction == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return reductionFunction;
    }

    public AbstractReductionFunctionEntity toPersistenceFunction(ReductionFunction reductionFunction) {
        AbstractReductionFunctionEntity functionEntity = reductionFunction.accept(serviceToPersistenceMapping);
        if (functionEntity == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return functionEntity;
    }
}
