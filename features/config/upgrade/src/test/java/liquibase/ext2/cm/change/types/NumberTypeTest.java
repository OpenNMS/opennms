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
