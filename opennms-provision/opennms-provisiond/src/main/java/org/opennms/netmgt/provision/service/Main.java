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
package org.opennms.netmgt.provision.service;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class Main {

    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        ClassPathXmlApplicationContext appContext = null;
        try {
            appContext = new ClassPathXmlApplicationContext("/META-INF/modelImport-appContext.xml");
            Provisioner importer = (Provisioner)appContext.getBean("modelImporter");
            Resource resource = new FileSystemResource(args[0]);
            importer.importModelFromResource(resource, Boolean.TRUE.toString());
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (appContext != null) appContext.close();
        }
    }

}
