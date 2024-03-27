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
package org.opennms.web.rest.v2.bsm;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JacksonUtils;

import static org.opennms.netmgt.bsm.test.BsmTestUtils.toJson;

import java.util.Collections;

public class BusinessServiceRestServiceJsonIT extends AbstractBusinessServiceRestServiceIT {

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    public String marshal(Object o) {
        return toJson(o);
    }

    @Override
    public <T> T getAndUnmarshal(String url, int expectedStatus, Class<T> expectedClass) throws Exception {
        final ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
        return getJsonObject(mapper, url, Collections.emptyMap(), expectedStatus, expectedClass);
    }
}
