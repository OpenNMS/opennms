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
package org.opennms.netmgt.ha;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class DbConnectionFactoryTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File writeDataSources(String content) throws Exception {
        File etc = new File(tmp.getRoot(), "etc");
        etc.mkdirs();
        File f = new File(etc, "opennms-datasources.xml");
        Files.writeString(f.toPath(), content);
        return f;
    }

    @Test
    public void parsesOpennmsEntry() throws Exception {
        writeDataSources(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<datasource-configuration\n" +
            "  xmlns:this=\"http://xmlns.opennms.org/xsd/config/opennms-datasources\"\n" +
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "  <jdbc-data-source name=\"opennms\"\n" +
            "    url=\"jdbc:postgresql://db.example.com:5432/mydb\"\n" +
            "    user-name=\"opennms\"\n" +
            "    password=\"s3cr3t\" />\n" +
            "  <jdbc-data-source name=\"opennms-admin\"\n" +
            "    url=\"jdbc:postgresql://db.example.com:5432/template1\"\n" +
            "    user-name=\"postgres\"\n" +
            "    password=\"adminpass\" />\n" +
            "</datasource-configuration>\n");

        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
        try {
            DbConnectionFactory f = DbConnectionFactory.fromDatasourcesXml();
            assertNotNull(f);
        } finally {
            System.clearProperty("opennms.home");
        }
    }

    @Test
    public void parsesEnvExpressionWithDefault() throws Exception {
        writeDataSources(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<datasource-configuration\n" +
            "  xmlns:this=\"http://xmlns.opennms.org/xsd/config/opennms-datasources\"\n" +
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "  <jdbc-data-source name=\"opennms\"\n" +
            "    url=\"jdbc:postgresql://${env:__HA_TEST_PG_HOST__|localhost}:5432/opennms\"\n" +
            "    user-name=\"opennms\"\n" +
            "    password=\"pass\" />\n" +
            "</datasource-configuration>\n");

        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
        try {
            DbConnectionFactory f = DbConnectionFactory.fromDatasourcesXml();
            assertNotNull(f);
        } finally {
            System.clearProperty("opennms.home");
        }
    }

    @Test(expected = IllegalStateException.class)
    public void throwsWhenOpennmsEntryMissing() throws Exception {
        writeDataSources(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<datasource-configuration\n" +
            "  xmlns:this=\"http://xmlns.opennms.org/xsd/config/opennms-datasources\"\n" +
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "  <jdbc-data-source name=\"opennms-admin\"\n" +
            "    url=\"jdbc:postgresql://localhost:5432/template1\"\n" +
            "    user-name=\"postgres\" password=\"\" />\n" +
            "</datasource-configuration>\n");

        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
        try {
            DbConnectionFactory.fromDatasourcesXml();
        } finally {
            System.clearProperty("opennms.home");
        }
    }
}
