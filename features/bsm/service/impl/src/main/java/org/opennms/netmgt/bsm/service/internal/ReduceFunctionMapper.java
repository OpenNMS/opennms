/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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
