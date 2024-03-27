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
package org.opennms.karaf.extender;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RepositoryTest {

    @Test
    public void canGenerateMavenUris() throws URISyntaxException {
        Repository releaseRepo = new Repository(Paths.get(File.separator + "release"),
                Lists.newArrayList(new URI("mvn:group.id/artifact.id/2.0.0/xml")),
                Lists.newArrayList());
        assertEquals(new URI("file:/release@id=release"), releaseRepo.toMavenUri());

        Repository snapshotRepo = new Repository(Paths.get(File.separator + "other"),
                Lists.newArrayList(new URI("mvn:other.group.id/other.artifact.id/1.0-SNAPSHOT/xml")),
                Lists.newArrayList());
        assertEquals(new URI("file:/other@id=other@snapshots"), snapshotRepo.toMavenUri());
    }
}
