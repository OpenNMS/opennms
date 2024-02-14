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

import java.util.List;
import java.util.Optional;

import liquibase.parser.core.ParsedNode;

import org.opennms.features.config.dao.api.ConfigItem;

/** A data type parser to get from liquibase to ConfigItem. */
public abstract class AbstractPropertyType {

    public interface Attribute {
        String NAME = "name";
        String DEFAULT = "default";
        String PATTERN = "pattern";
        String MIN = "min";
        String MAX = "max";
        String TYPE = "type";
    }

    final protected List<ParsedNode> listOfAttributes;
    final protected ConfigItem configItem;
    final protected Optional<String> defaultValueOpt;

    protected AbstractPropertyType(final List<ParsedNode> listOfAttributes) {
        this.listOfAttributes = listOfAttributes;
        this.configItem = new ConfigItem();
        this.configItem.setName(getAttributeValueNotBlankOrThrowException(Attribute.NAME));
        this.defaultValueOpt = getAttributeValue(Attribute.DEFAULT);
    }

    public ConfigItem toItem() {
        return configItem;
    }

    public String getAttributeValueNotBlankOrThrowException(final String name) {
        return getAttributeValue(name)
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Attribute %s must not be blank.", name)));
    }

    public Optional<String> getAttributeValue(final String name) {
        return listOfAttributes
                .stream()
                .filter(n -> name.equals(n.getName()))
                .findAny()
                .map(ParsedNode::getValue)
                .map(Object::toString);
    }
}
