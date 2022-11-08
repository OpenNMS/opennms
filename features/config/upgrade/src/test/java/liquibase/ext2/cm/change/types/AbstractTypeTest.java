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

import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;

public abstract class AbstractTypeTest {

    protected Map<String, String> attributes;

    @Before
    public void setUp() throws ParsedNodeException {
        attributes = new HashMap<>();
        attributes.put("name", "myProperty");
    }

    @Test
    public void throwExceptionForMissingName() {
        attributes.put("name", null);
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void throwExceptionForEmptyName() {
        attributes.put("name", "");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    protected ConfigItem createItem() {
        List<ParsedNode> nodes = new ArrayList<>();
        for (Entry<String, String> entry : this.attributes.entrySet()) {
            ParsedNode node = new ParsedNode(null, entry.getKey());
            try {
                node.setValue(entry.getValue());
            } catch (ParsedNodeException e) {
                throw new RuntimeException(e);
            }
            nodes.add(node);
        }
        return createType(nodes).toItem();
    }

    protected abstract AbstractPropertyType createType(final List<ParsedNode> nodes);
}