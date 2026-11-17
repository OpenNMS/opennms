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
package liquibase.ext2.cm.change;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * Tripwire: the SHA-256 hash below MUST match the xsdFileHash in
 * changelog-cm/37.0.0/changelog-cm.xml for the http-datacollection-config changeSet.
 * The upgrade refuses to import the schema when they differ, so a change to
 * http-datacollection-config.xsd needs a new schema version and changeSet instead.
 */
public class HttpDatacollectionSchemaHashTest {

    private static final String HTTP_DATACOLLECTION_V1_0_HASH =
            "d0f68d14549789ad7197339319bcde6ea21f8dc4560f5a6ab07fe3dbfbb0fba1";

    @Test
    public void httpDatacollectionSchemaHashMatchesChangelog() throws IOException {
        assertEquals(HTTP_DATACOLLECTION_V1_0_HASH, HashUtil.getHash("http-datacollection-config.xsd"));
    }
}
