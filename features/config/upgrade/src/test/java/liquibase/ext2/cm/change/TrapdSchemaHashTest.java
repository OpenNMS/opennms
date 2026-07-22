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
 * Tripwire: the SHA-256 hashes below MUST match the xsdFileHash values in
 * changelog-cm/36.0.0/changelog-cm.xml for the trapd-config changeSets. If a
 * trapd schema file changes, update both the changelog and this test in the
 * same change (NMS-19723).
 */
public class TrapdSchemaHashTest {

    private static final String TRAPD_V1_0_HASH =
            "ad97a001cd7ebb8a8b1a45574ba4e681e204ecf8592bf264764d4d7f52efc7a1";
    private static final String TRAPD_V1_1_HASH =
            "d5fdc5b97d8ae35c9ff47f54f4975d3d6ae0e455a8da1ff0e7436bf699dae232";

    @Test
    public void trapdV10SchemaHashMatchesChangelog() throws IOException {
        assertEquals(TRAPD_V1_0_HASH, HashUtil.getHash("trapd-configuration.xsd"));
    }

    @Test
    public void trapdV11SchemaHashMatchesChangelog() throws IOException {
        assertEquals(TRAPD_V1_1_HASH, HashUtil.getHash("trapd-configuration-1.1.xsd"));
    }
}
