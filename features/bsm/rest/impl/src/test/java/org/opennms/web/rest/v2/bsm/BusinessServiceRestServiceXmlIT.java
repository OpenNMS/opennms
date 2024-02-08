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

import static org.opennms.netmgt.bsm.test.BsmTestUtils.toXml;

import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;

import org.opennms.web.rest.api.ResourceLocation;

public class BusinessServiceRestServiceXmlIT extends AbstractBusinessServiceRestServiceIT {

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_XML;
    }

    @Override
    public String marshal(Object o) {
        return toXml(o);
    }

    @Override
    public <T> T getAndUnmarshal(String url, int expectedStatus, Class<T> expectedClass) throws Exception {
        final JAXBContext context = JAXBContext.newInstance(expectedClass, ResourceLocation.class);
        return getXmlObject(context, url, Collections.emptyMap(), expectedStatus, expectedClass);
    }
}
