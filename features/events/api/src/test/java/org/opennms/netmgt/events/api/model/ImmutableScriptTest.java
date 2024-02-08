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
package org.opennms.netmgt.events.api.model;

import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.netmgt.xml.event.Script;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableScript}'.
 */
public class ImmutableScriptTest {

    @Test
    public void test() {
        Script script = new Script();
        script.setContent("test-value");
        script.setLanguage("test");

        // Mutable to Immutable
        IScript immutableScript = ImmutableMapper.fromMutableScript(script);

        // Immutable to Mutable
        Script convertedScript = Script.copyFrom(immutableScript);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(script);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedScript);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }
}
