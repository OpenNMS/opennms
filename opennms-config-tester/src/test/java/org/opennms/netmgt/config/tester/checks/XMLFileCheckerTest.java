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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

public class XMLFileCheckerTest {

    @Test
    public void shouldSucceedOnFileWithCorrectSyntax() throws IOException {
        File file = saveXmltoFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?><element></element>");
        XMLFileChecker.checkFile(file.toPath()).forSyntax();
    }

    @Test(expected = ConfigCheckValidationException.class)
    public void shouldFailOnFileWithIncorrectSyntax() throws IOException {
        File file = saveXmltoFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?><elementThatIsNotClosed>");
        XMLFileChecker.checkFile(file.toPath()).forSyntax();
    }


    private File saveXmltoFile(String xml) throws IOException{
        File file = File.createTempFile(this.getClass().getSimpleName(), ".xml");
        Writer writer = new FileWriter(file);
        writer.write(xml);
        writer.close();
        return file;
    }

}