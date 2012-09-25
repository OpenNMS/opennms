/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.utils;

import java.io.IOException;

/**
 * This class is used to make a quick backup of a file
 *
 * @author <a href="mailto:jason@opennms.org">Jason</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class FileBackup {
    /**
     * <p>makeBackup</p>
     *
     * @param originalFile a {@link java.lang.String} object.
     * @param backupFile a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static void makeBackup(String originalFile, String backupFile) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("cp " + originalFile + " " + backupFile);
    }
}
