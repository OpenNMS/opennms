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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opennms.features.config.dao.api.ConfigItem.Type;

import liquibase.parser.core.ParsedNode;

public class StringType extends AbstractPropertyType {

    public StringType(final List<ParsedNode> listOfAttributes) {
        super(listOfAttributes);
        this.configItem.setType(Type.STRING);
        getAttributeValue(Attribute.PATTERN)
                .map(this::validateRegex)
                .ifPresent(configItem::setPattern);
        configItem.setDefaultValue(defaultValueOpt.orElse(null));
    }

    private String validateRegex(String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(String.format("Invalid regex %s:  %s", regex, e.getMessage()));
        }
        return regex;
    }

}
