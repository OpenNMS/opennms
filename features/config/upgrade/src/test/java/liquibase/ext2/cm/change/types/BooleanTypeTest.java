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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import java.util.List;

import liquibase.parser.core.ParsedNode;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.api.ConfigItem.Type;

public class BooleanTypeTest extends AbstractTypeTest {

    @Test
    public void shouldParseGoodCase() {
        attributes.put("default", "true");
        ConfigItem item = createItem();
        assertEquals(Type.BOOLEAN, item.getType());
        assertEquals(Boolean.TRUE, item.getDefaultValue());
    }

    @Test
    public void shouldRejectNonBooleanForDefaultValue() {
        attributes.put("default", "I'm not boolean");
        assertThrowsException(IllegalArgumentException.class, this::createItem);
    }

    @Test
    public void shouldDefaultToNullForNoDefaultValue() {
        attributes.remove("default");
        ConfigItem item = createItem();
        assertEquals(Type.BOOLEAN, item.getType());
        assertNull(item.getDefaultValue());
    }

    @Override
    protected AbstractPropertyType createType(final List<ParsedNode> nodes) {
        return new BooleanType(nodes);
    }
}
