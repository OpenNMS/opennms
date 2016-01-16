/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.test;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.test.BusinessServiceEntityBuilder;

// TODO MVR this should live in its own module
public enum Format {
    JSON(new BusinessServiceEntityBuilder.Transformer() {

        @Override
        public String marshal(BusinessServiceRequestDTO input) throws IOException {
            return new ObjectMapper().writeValueAsString(input);
        }
    }),
    XML(new BusinessServiceEntityBuilder.Transformer() {

        @Override
        public String marshal(BusinessServiceRequestDTO input) {
            return JaxbUtils.marshal(input);
        }
    });

    private final BusinessServiceEntityBuilder.Transformer transformer;

    Format(BusinessServiceEntityBuilder.Transformer transformer) {
        this.transformer = transformer;
    }

    String transform(BusinessServiceRequestDTO input) throws IOException {
        return transformer.marshal(input);
    }
}
