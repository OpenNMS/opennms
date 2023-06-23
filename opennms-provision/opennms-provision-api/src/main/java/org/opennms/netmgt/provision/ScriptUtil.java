/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
