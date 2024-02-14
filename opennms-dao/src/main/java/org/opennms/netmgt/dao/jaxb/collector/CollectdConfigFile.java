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
package org.opennms.netmgt.dao.jaxb.collector;

import java.io.File;
import java.util.Iterator;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

public class CollectdConfigFile {

    private static final Logger LOG = LoggerFactory.getLogger(CollectdConfigFile.class);

    File m_file;

    /**
     * <p>Constructor for CollectdConfigFile.</p>
     *
     * @param file a {@link java.io.File} object.
     */
    public CollectdConfigFile(File file) {
        m_file = file;
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.dao.jaxb.collector.CollectdConfigVisitor} object.
     */
    public void visit(CollectdConfigVisitor visitor) {
        CollectdConfiguration collectdConfiguration = getCollectdConfiguration();
        visitor.visitCollectdConfiguration(collectdConfiguration);

        for (Iterator<Collector> it = collectdConfiguration.getCollectors().iterator(); it.hasNext();) {
            Collector collector = it.next();
            doVisit(collector, visitor);
        }
        visitor.completeCollectdConfiguration(collectdConfiguration);
    }

    private void doVisit(Collector collector, CollectdConfigVisitor visitor) {
        visitor.visitCollectorCollection(collector);
        visitor.completeCollectorCollection(collector);
    }

    private CollectdConfiguration getCollectdConfiguration() {
        return JaxbUtils.unmarshal(CollectdConfiguration.class, new FileSystemResource(m_file));
    }
}
