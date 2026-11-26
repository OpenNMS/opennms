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
package org.opennms.protocols.xml.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import org.opennms.features.config.dao.impl.util.XsdHelper;

import com.google.common.hash.Hashing;
import com.google.common.io.Resources;

/**
 * Tripwire: the configuration manager refuses to start when the hash of xml-datacollection-config.xsd differs from
 * the xsdFileHash of its importSchemaFromXsd changeSets. Changing the XSD therefore needs a new changeSet with the
 * new hash (an existing changeSet must not be edited, it already ran on existing installations).
 */
public class XmlDataCollectionSchemaHashTest {

    private static final String XSD = "xml-datacollection-config.xsd";
    private static final Pattern IMPORT_SCHEMA = Pattern.compile(
            "<cm:importSchemaFromXsd[^>]*xsdFileName=\"" + Pattern.quote(XSD) + "\"[^>]*xsdFileHash=\"([0-9a-f]+)\"");

    @Test
    public void changeSetsMatchTheSchema() throws IOException {
        // the upgrade module is not a dependency of this one, read the changelog from its sources
        final URL changelog = Paths.get("../../features/config/upgrade/src/main/resources/changelog-cm/37.0.0/changelog-cm.xml").toUri().toURL();
        final Matcher matcher = IMPORT_SCHEMA.matcher(Resources.toString(changelog, StandardCharsets.UTF_8));
        final List<String> hashes = new ArrayList<>();
        while (matcher.find()) {
            hashes.add(matcher.group(1));
        }
        assertFalse("no importSchemaFromXsd for " + XSD + " in " + changelog, hashes.isEmpty());
        // same as liquibase.ext2.cm.change.HashUtil, which the upgrade uses to check the changeSet
        final String actual = Hashing.sha256().hashBytes(Resources.toByteArray(XsdHelper.getSchemaPath(XSD))).toString();
        for (final String hash : hashes) {
            assertThat("xsdFileHash in " + changelog, hash, equalTo(actual));
        }
    }
}
