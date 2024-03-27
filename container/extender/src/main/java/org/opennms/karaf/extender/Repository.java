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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public class Repository {
    private final Path m_path;
    private final List<URI> m_featureUris;
    private final List<Feature> m_featuresBoot;
    private final URI m_mavenUri;

    public Repository(Path path, List<URI> featureUris, List<Feature> featuresBoot) throws URISyntaxException {
        m_path = Objects.requireNonNull(path);
        m_featureUris = ImmutableList.copyOf(featureUris);
        m_featuresBoot = ImmutableList.copyOf(featuresBoot);
        m_mavenUri = new URI(String.format("file:%s@id=%s%s",
                m_path.toAbsolutePath().toString(),
                m_path.getFileName().toString(), containsSnapshots() ? "@snapshots" : ""));
    }

    public List<URI> getFeatureUris() {
        return m_featureUris;
    }

    public List<Feature> getFeaturesBoot() {
        return m_featuresBoot;
    }

    public URI toMavenUri() {
        return m_mavenUri;
    }

    public boolean containsSnapshots() {
        return m_featureUris.stream()
                .filter(uri -> uri.toString().contains("-SNAPSHOT"))
                .findFirst().isPresent();
    }

    @Override
    public String toString() {
        return String.format("Repository[path=%s, featureUris=%s, featuresBoot=%s]",
                m_path, m_featureUris, m_featuresBoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_path, m_featureUris, m_featuresBoot);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Repository other = (Repository) obj;
        return Objects.equals(this.m_path, other.m_path) &&
                Objects.equals(this.m_featureUris, other.m_featureUris) &&
                Objects.equals(this.m_featuresBoot, other.m_featuresBoot);
    }
}
