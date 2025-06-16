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
package liquibase.ext2.cm.change;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import liquibase.ext2.cm.change.types.AbstractPropertyType.Attribute;
import liquibase.ext2.cm.change.types.BooleanType;
import liquibase.ext2.cm.change.types.NumberType;
import liquibase.ext2.cm.change.types.PropertyType;
import liquibase.ext2.cm.change.types.StringType;
import liquibase.parser.core.ParsedNode;

import org.opennms.features.config.dao.api.ConfigItem;

public class Liqui2ConfigItemUtil {

    public static ConfigItem createConfigItemForProperty(final List<ParsedNode> listOfAttributes) {

        final String type = getAttributeValueOrThrowException(listOfAttributes, Attribute.TYPE);
        if (PropertyType.BOOLEAN.equals(type)) {
            return new BooleanType(listOfAttributes).toItem();
        } else if (PropertyType.STRING.equals(type)) {
            return new StringType(listOfAttributes).toItem();
        } else if (PropertyType.NUMBER.equals(type)) {
            return new NumberType(listOfAttributes).toItem();
        } else {
            throw new IllegalArgumentException(String.format("Unknown type='%s'", type));
        }
    }

    public static Optional<ConfigItem> findPropertyDefinition(ConfigItem schema, String propertyName) {
        Objects.requireNonNull(propertyName);
        return schema
                .getChildren()
                .stream().filter(i -> propertyName.equals(i.getName()))
                .findAny();
    }

    public static String getAttributeValueOrThrowException(final List<ParsedNode> listOfAttributes, final String name) {
        return getAttributeValue(listOfAttributes, name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Attribute %s must not be null.", name)));
    }

    public static Optional<String> getAttributeValue(final List<ParsedNode> listOfAttributes, final String name) {
        return listOfAttributes
                .stream()
                .filter(n -> name.equals(n.getName()))
                .findAny()
                .map(ParsedNode::getValue)
                .map(Object::toString);
    }
}
