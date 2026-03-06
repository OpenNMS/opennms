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
package org.opennms.features.events.kafka.consumer;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.event.Event;

/**
 * Deserializes XML-encoded event bytes into {@link Event} objects using JAXB.
 */
public class XmlEventDeserializer implements EventDeserializer {

    @Override
    public Event deserialize(byte[] data) {
        String xml = new String(data, StandardCharsets.UTF_8);
        return JaxbUtils.unmarshal(Event.class, new StringReader(xml));
    }
}
