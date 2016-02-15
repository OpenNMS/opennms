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

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;

public enum MapFunctionType {

    SetTo(org.opennms.netmgt.bsm.service.model.functions.map.SetTo.class) {
        @Override
        public MapFunction fromDTO(MapFunctionDTO input) {
            SetTo setTo = new SetTo();
            String status = Objects.requireNonNull(input.getProperties().get("status"));
            setTo.setStatus(Status.valueOf(status));
            return setTo;
        }

        @Override
        public <T extends MapFunction> MapFunctionDTO toDTO(T input) {
            org.opennms.netmgt.bsm.service.model.functions.map.SetTo setTo = (org.opennms.netmgt.bsm.service.model.functions.map.SetTo) input;
            MapFunctionDTO dto = new MapFunctionDTO();
            dto.setType(this);
            Map<String, String> properties = new HashMap<>();
            if (setTo.getStatus() == null) {
                properties.put("status", "");
            } else {
                properties.put("status", setTo.getStatus().toString());
            }
            dto.setProperties(properties);
            return dto;
        }
    },
    Ignore(org.opennms.netmgt.bsm.service.model.functions.map.Ignore.class) {
        @Override
        public MapFunction fromDTO(MapFunctionDTO input) {
            return new Ignore();
        }

    },
    Decrease(org.opennms.netmgt.bsm.service.model.functions.map.Decrease.class) {
        @Override
        public MapFunction fromDTO(MapFunctionDTO input) {
            return new Decrease();
        }
    },
    Increase(org.opennms.netmgt.bsm.service.model.functions.map.Increase.class) {
        @Override
        public MapFunction fromDTO(MapFunctionDTO input) {
            return new Increase();
        }
    },
    Identity(org.opennms.netmgt.bsm.service.model.functions.map.Identity.class) {
        @Override
        public MapFunction fromDTO(MapFunctionDTO input) {
            return new Identity();
        }
    };

    private final Class<? extends MapFunction> clazz;

    MapFunctionType(Class<? extends MapFunction> clazz) {
        this.clazz = clazz;
    }

    public abstract MapFunction fromDTO(MapFunctionDTO input);

    public <T extends MapFunction> MapFunctionDTO toDTO(T input) {
        MapFunctionDTO dto = new MapFunctionDTO();
        dto.setType(this);
        return dto;
    }

    public static MapFunctionType valueOf(Class<? extends MapFunction> aClass) {
        for (MapFunctionType eachType : values()) {
            if (eachType.clazz == aClass) {
                return eachType;
            }
        }
        throw new IllegalArgumentException("Cannot create Type for map function " + aClass);
    }
}
