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
