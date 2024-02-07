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
package org.opennms.smoketest.stacks;

import java.net.URL;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class OverlayFile {

    private final URL source;
    private final String target;
    private final Set<PosixFilePermission> permissions;

    public OverlayFile(URL source, String target) {
        this(source, target, Collections.emptySet());
    }

    public OverlayFile(URL source, String target, Set<PosixFilePermission> permissions) {
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.permissions = Collections.unmodifiableSet(Objects.requireNonNull(permissions));
    }

    public URL getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Set<PosixFilePermission> getPermissions() {
        return permissions;
    }
}
