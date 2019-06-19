/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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