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
package org.opennms.netmgt.config.tester.checks;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class XMLFileChecker {
    private Path file;

    private XMLFileChecker(Path file) {
        this.file = file;
    }

    public static XMLFileChecker checkFile(Path file) {
        return new XMLFileChecker(file);
    }

    public void forSyntax() {
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFile());
            // Document was parsed => we assume its a valid XML
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new ConfigCheckValidationException(e);
        }
    }
}
