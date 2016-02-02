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

package org.opennms.web.rest.v2.bsm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.bsm.service.model.mapreduce.ReductionFunction;

public enum ReduceFunctionType {
    MostCritical(org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical.class) {
        @Override
        public ReductionFunction fromDTO(ReduceFunctionDTO input) {
            return new org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical();
        }
    },
    Threshold(org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold.class) {
        @Override
        public ReductionFunction fromDTO(ReduceFunctionDTO input) {
            org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold threshold = new  org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold();
            String thresholdValue = Objects.requireNonNull(input.getProperties().get("threshold"));
            threshold.setThreshold(Float.parseFloat(thresholdValue));
            return threshold;
        }

        @Override
        public <T extends ReductionFunction> ReduceFunctionDTO toDTO(T input) {
            ReduceFunctionDTO dto = new ReduceFunctionDTO();
            dto.setType(this);
            Map<String, String> properties = new HashMap<>();
            properties.put("threshold", String.valueOf(((org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold) input).getThreshold()));
            dto.setProperties(properties);
            return dto;
        }
    };

    private final Class<? extends ReductionFunction> clazz;

    ReduceFunctionType(Class<? extends ReductionFunction> clazz) {
        this.clazz = clazz;
    }

    public abstract ReductionFunction fromDTO(ReduceFunctionDTO input);

    public <T extends ReductionFunction> ReduceFunctionDTO toDTO(T input) {
        ReduceFunctionDTO dto = new ReduceFunctionDTO();
        dto.setType(this);
        return dto;
    }

    public static ReduceFunctionType valueOf(Class<? extends ReductionFunction> aClass) {
        for (ReduceFunctionType eachType : values()) {
            if (eachType.clazz == aClass) {
                return eachType;
            }
        }
        throw new IllegalArgumentException("Cannot create Type for reduction function " + aClass);
    }
}
