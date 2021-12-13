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
import static org.junit.Assert.assertNull;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import java.util.List;

import liquibase.ext2.cm.change.types.AbstractPropertyType.Attribute;
import liquibase.parser.core.ParsedNode;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.api.ConfigItem.Type;

public class NumberTypeTest extends AbstractTypeTest {

    @Test
    public void shouldParseGoodCase() {
        attributes.put(Attribute.DEFAULT, "3");
        attributes.put(Attribute.MIN, "2");
        attributes.put(Attribute.MAX, "4");
        ConfigItem item = createItem();
        assertEquals(Type.NUMBER, item.getType());
        assertEquals((double) 3, item.getDefaultValue());
    }

    @Test
    public void shouldRejectNonNumberForDefaultValue() {
        attributes.put(Attribute.DEFAULT, "I'm not a number");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void shouldRejectNonNumberForMin() {
        attributes.put(Attribute.MIN, "I'm not a number");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void shouldRejectNonNumberForMax() {
        attributes.put(Attribute.MAX, "I'm not a number");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void shouldRejectMaxSmallerMin() {
        attributes.put(Attribute.MIN, "4.0");
        attributes.put(Attribute.MAX, "3.0");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void shouldRejectDefaultOutsideMinMax() {
        attributes.put(Attribute.DEFAULT, "1");
        attributes.put(Attribute.MIN, "2.0");
        attributes.put(Attribute.MAX, "3.0");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void shouldDefaultToNullForNoDefaultValue() {
        attributes.remove("default");
        ConfigItem item = createItem();
        assertEquals(Type.NUMBER, item.getType());
        assertNull(item.getDefaultValue());
    }

    @Override
    protected AbstractPropertyType createType(final List<ParsedNode> nodes) {
        return new NumberType(nodes);
    }
}
