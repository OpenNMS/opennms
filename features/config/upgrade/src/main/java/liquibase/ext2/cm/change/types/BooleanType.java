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

import liquibase.parser.core.ParsedNode;

import org.opennms.features.config.dao.api.ConfigItem;

public class BooleanType extends AbstractPropertyType {

    public BooleanType(final List<ParsedNode> listOfAttributes) {
        super(listOfAttributes);
        if (defaultValueOpt.isPresent() && !"true".equals(defaultValueOpt.get()) && !"false".equals(defaultValueOpt.get())) {
            throw new IllegalArgumentException(String.format("value='%s' is not true or false", defaultValueOpt.get()));
        }
        this.configItem.setType(ConfigItem.Type.BOOLEAN);
        Boolean defaultValue = defaultValueOpt
                .map(Boolean::valueOf)
                .orElse(null);
        this.configItem.setDefaultValue(defaultValue);
    }
}
