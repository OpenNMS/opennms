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
package liquibase.ext2.cm.change.converter;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.JsonAsString;

import java.util.Objects;

/**
 * Converts a xml configuration file into json format to be stored in the cm manger.
 */
public class XmlToJson {

    final private JsonAsString json;

    public XmlToJson(final String xml, ConfigDefinition xmlConfigDefinition) throws Exception {
        Objects.requireNonNull(xml);
        Objects.requireNonNull(xmlConfigDefinition);
        json = new JsonAsString(XsdHelper.getConverter(xmlConfigDefinition).xmlToJson(xml));
    }

    public JsonAsString getJson() {
        return json;
    }
}
