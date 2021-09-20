/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.integrations.R;

import java.io.IOException;
import java.net.URL;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import freemarker.cache.URLTemplateLoader;

/**
 * Attempts to load the template from the class-path, followed
 * by the file-system, otherwise fails.
 *
 * @author jwhite
 */
public class HybridTemplateLoader extends URLTemplateLoader {
    @Override
    protected URL getURL(String name) {
        ClassPathResource cpr = new ClassPathResource(name);
        if (cpr.exists()) {
            try {
                return cpr.getURL();
            } catch (IOException e) {
                return null;
            }
        }
        FileSystemResource fsr = new FileSystemResource(name);
        if (fsr.exists()) {
            try {
                return fsr.getURL();
            } catch (IOException e) {
                return null;
            }
        }
        
        return null;
    }
}
