/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeolocationQueryDtoTest {

    /**
     * see NMS-18052
     */
    @Test()
    public void testCustomDeserializer() {
        final String json = "{\n" +
                "  \"strategy\": null,\n" +
                "  \"severityFilter\": null,\n" +
                "  \"includeAcknowledgedAlarms\": \"malicious code\"\n" +
                "}";

        try {
            new ObjectMapper().readValue(json, GeolocationQueryDTO.class);
        } catch (final IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertFalse(e.getMessage().contains("malicious code"));
            assertTrue(e.getMessage().contains("Error mapping JSON to Boolean value, details omitted."));
        }
    }
}
