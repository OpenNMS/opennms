/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class EscapeSequenceAdapterTest {

    private EscapeSequenceAdapter adapter;

    @BeforeEach
    public void setUp() {
        adapter = new EscapeSequenceAdapter();
    }

    @Test
    public void testUnmarshalWithEscapeSequences() throws Exception {

        String input = "Hello&#xd;World&#xa;Test";
        String expected = "Hello\rWorld\nTest";
        String result = adapter.unmarshal(input);
        assertNotNull(result, "Result should not be null");
        assertEquals(expected, result, "Escape sequences should be replaced correctly");
    }

    @Test
    public void testUnmarshalWithNullValue() throws Exception {
        String result = adapter.unmarshal(null);
        assertNull(result, "Null input should return null");
    }

    @Test
    public void testMarshalWithSpecialCharacters() throws Exception {
        String input = "Hello\rWorld\nTest";
        String expected = "Hello&#xd;World&#xa;Test";
        String result = adapter.marshal(input);
        assertNotNull(result, "Result should not be null");
        assertEquals(expected, result, "Special characters should be converted to escape sequences");
    }

    @Test
    public void testMarshalWithNullValue() throws Exception {
        String result = adapter.marshal(null);
        assertNull(result, "Null input should return null");
    }

    @Test
    public void testUnmarshalWithNoEscapeSequences() throws Exception {
        String input = "NoEscapeSequenceHere";
        String expected = "NoEscapeSequenceHere";
        String result = adapter.unmarshal(input);
        assertNotNull(result, "Result should not be null");
        assertEquals(expected, result, "Input without escape sequences should remain unchanged");
    }

    @Test
    public void testMarshalWithNoSpecialCharacters() throws Exception {
        String input = "NormalString";
        String expected = "NormalString";
        String result = adapter.marshal(input);
        assertNotNull(result, "Result should not be null");
        assertEquals(expected, result, "Input without special characters should remain unchanged");
    }

}