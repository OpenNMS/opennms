/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.config.osgi;

import static org.junit.Assert.assertEquals;

import java.util.Dictionary;
import java.util.Properties;

import org.junit.Test;

public class DictionaryUtilTest {

    @Test
    public void shouldHandlePropertiesFiles() {
        Properties props = new Properties();
        props.setProperty("a", "b");
        props.setProperty("c", "d");
        String rawString = DictionaryUtil.writeToRawString(props);
        Dictionary propsLoaded = DictionaryUtil.createFromRawString(rawString);
        assertEquals(2, propsLoaded.size());
        assertEquals("b", propsLoaded.get("a"));
        assertEquals("d", propsLoaded.get("c"));

    }

}
