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
package org.opennms.smoketest.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class helps determining the target directory from any class file.
 *
 * In same tests it is required to directly access the target directory, however determining it
 * is tedious. Therefore this class helps determining it.
 *
 * @author mvrueden
 */
public class TargetRoot {

    private final Path targetRoot;

    // Determines the target directory of TargetRoot.class
    public TargetRoot() {
        this(TargetRoot.class);
    }

    // Determines the target directory of the given class
    public TargetRoot(Class clazz) {
        final String relPath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        Path jarOrClassRoot = Paths.get(relPath);

        this.targetRoot = determineTargetRoot(jarOrClassRoot);
    }

    public Path getPath() {
        return this.targetRoot;
    }

    public Path getPath(String...paths) {
        return Paths.get(targetRoot.toString(), paths);
    }

    /**
     * Determines the target directory starting from the given path.
     * The given pass is usually either the "target/classes" or "target/test-classes" directory.
     * However in some cases it also may be any jar, e.g. "target/some.jar".
     *
     * @param jarOrClassRoot The starting path to find the target directory in
     * @return the first directory which name is "target" starting from <code>jarOrClassRoot</code>
     */
    private Path determineTargetRoot(Path jarOrClassRoot) {
        // If the path is a file, then we tried to get the target directory of a jar file
        // in this case, we just have to get rid of the jar file, before continuing
        if (jarOrClassRoot.toFile().isFile()) {
            jarOrClassRoot = jarOrClassRoot.getParent();
        }
        // Now go back as long as we hit the "target"
        while (jarOrClassRoot != null && !"target".equals(jarOrClassRoot.getFileName().toString())) {
            jarOrClassRoot = jarOrClassRoot.getParent();
        }
        // Maybe no target directory was found
        if (jarOrClassRoot == null) {
            throw new IllegalArgumentException("No target directory found in " + jarOrClassRoot);
        }
        return jarOrClassRoot;
    }
}