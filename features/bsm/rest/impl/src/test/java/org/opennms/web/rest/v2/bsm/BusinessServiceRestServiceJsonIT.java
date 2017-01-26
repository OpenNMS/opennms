/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2016 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JacksonUtils;

import static org.opennms.netmgt.bsm.test.BsmTestUtils.toJson;

import java.util.Collections;

public class BusinessServiceRestServiceJsonIT extends AbstractBusinessServiceRestServiceIT {

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    public String marshal(Object o) {
        return toJson(o);
    }

    @Override
    public <T> T getAndUnmarshal(String url, int expectedStatus, Class<T> expectedClass) throws Exception {
        final ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
        return getJsonObject(mapper, url, Collections.emptyMap(), expectedStatus, expectedClass);
    }
}
