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
package org.opennms.netmgt.provision;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ScriptUtil {
    public static boolean isDescendantOf(final String root, final String child) {
        if (root == null || child == null) {
            return false;
        }

        return isDescendantOf(Paths.get(root), Paths.get(child));
    }

    public static boolean isDescendantOf(final Path root, final Path child) {
        if (root == null || child == null) {
            return false;
        }

        final Path absoluteRoot = root
                .toAbsolutePath()
                .normalize();

        final Path absoluteChild = child
                .toAbsolutePath()
                .normalize();

        if (absoluteRoot.getNameCount() >= absoluteChild.getNameCount()) {
            return false;
        }

        final Path nextChild = absoluteChild
                .getParent();

        return nextChild.equals(absoluteRoot) || isDescendantOf(absoluteRoot, nextChild);
    }
}
