/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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