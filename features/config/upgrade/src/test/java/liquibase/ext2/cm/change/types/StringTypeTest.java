/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package liquibase.ext2.cm.change.types;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import java.util.List;

import liquibase.parser.core.ParsedNode;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.api.ConfigItem.Type;

public class StringTypeTest extends AbstractTypeTest {

    @Test
    public void shouldParseGoodCase() {
        attributes.put("default", ".*");
        ConfigItem item = createItem();
        assertEquals(Type.STRING, item.getType());
        assertEquals(".*", item.getDefaultValue());
    }

    @Test
    public void shouldRejectNonRegexForDefaultValue() {
        attributes.put("pattern", "I'm not a regex[");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void shouldDefaultNullForNoDefaultValue() {
        // we want it null since some services check against null
        attributes.remove("default");
        ConfigItem item = createItem();
        assertEquals(Type.STRING, item.getType());
        assertEquals(null, item.getDefaultValue());
    }

    @Override
    protected AbstractPropertyType createType(final List<ParsedNode> nodes) {
        return new StringType(nodes);
    }
}
