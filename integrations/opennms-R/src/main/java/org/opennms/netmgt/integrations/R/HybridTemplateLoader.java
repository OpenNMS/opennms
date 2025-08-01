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
