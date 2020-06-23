/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;

/**
 * <p>HostFileSystemStorageStrategy class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Deprecated
public class HostFileSystemStorageStrategy extends IndexStorageStrategy {

    /** Constant <code>HR_STORAGE_DESC=".1.3.6.1.2.1.25.2.3.1.3"</code> */
    public static String HR_STORAGE_DESC = "hrStorageDescr";

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        StringAttributeVisitor visitor = new StringAttributeVisitor(HR_STORAGE_DESC);
        resource.visit(visitor);
        String value = (visitor.getValue() != null ? visitor.getValue() : resource.getInstance());
        /*
         * Use special translation for root (base) filesystem
         */
        if (value.equals("/"))
            return "_root_fs";
        /*
         * 1. Eliminate first slash character
         * 2. Eliminate tabs and spaces on filesystem names
         * 3. Replace slash (file separator) character with "-"
         * 4. Remove Additional Information on Windows Drives
         */
        return value.replaceFirst("/", "").replaceAll("\\s", "").replaceAll("/", "-").replaceAll(":\\\\.*", "");
    }

}
