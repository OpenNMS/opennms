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
package org.opennms.netmgt.collection.persistence.tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.ByNameComparator;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.PersistOperationBuilder;
import org.opennms.netmgt.collection.api.ResourceIdentifier;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.DefaultTimeKeeper;
import org.opennms.netmgt.model.ResourcePath;
import org.slf4j.LoggerFactory;

public class TcpPersistOperationBuilder implements PersistOperationBuilder {
    // Piggyback off of RRD directory/extensions
    final String baseDir = System.getProperty("rrd.base.dir", "");
    final String fileExt = System.getProperty("org.opennms.rrd.fileExtension", "");

    private final TcpOutputStrategy m_tcpStrategy;
    private final String m_rrdName;
    private final ResourceIdentifier m_resource;
    private final Map<CollectionAttributeType, Number> m_dbl_declarations = new TreeMap<>(new ByNameComparator());
    private final Map<String, String> m_str_declarations = new TreeMap<>();
    private TimeKeeper m_timeKeeper = new DefaultTimeKeeper();

    public TcpPersistOperationBuilder(TcpOutputStrategy tcpStrategy, ResourceIdentifier resource, String rrdName) {
        m_tcpStrategy = Objects.requireNonNull(tcpStrategy);
        m_resource = Objects.requireNonNull(resource);
        m_rrdName = Objects.requireNonNull(rrdName);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    private File getResourceDir(ResourceIdentifier resource) throws FileNotFoundException {
        return new File(baseDir).toPath()
                .resolve(ResourcePath.resourceToFilesystemPath(resource.getPath()))
                .toFile();
    }

    @Override
    public void setAttributeValue(CollectionAttributeType attributeType, Number value) {
        m_dbl_declarations.put(attributeType, value);
    }

    public void setStringAttributeValue(String key, String value) {
        m_str_declarations.put(key, value);
    }

    @Override
    public void setAttributeMetadata(String metricIdentifier, String name) {
        // Ignore meta-data
    }

    /**
     * Commit.
     *
     * @throws PersistException the persist exception
     */
    @Override
    public void commit() throws PersistException {
        if (m_dbl_declarations.size() == 0 && m_str_declarations.size() == 0) {
            return;
        }

        try {
            final String ownerName = m_resource.getOwnerName();
            final String absolutePath = getResourceDir(m_resource).getAbsolutePath();

            String rrdFile = absolutePath + File.separator + m_rrdName + fileExt;
            long timestamp = m_timeKeeper.getCurrentTime();
            long time = (timestamp + 500L) / 1000L;

            m_tcpStrategy.updateData(rrdFile, ownerName, new Long(time), getDblValues(), getStrValues());
        } catch (FileNotFoundException e) {
            LoggerFactory.getLogger(getClass()).warn("Could not get resource directory: " + e.getMessage(), e);
            return;
        } catch (Exception e) {
            throw new PersistException(e);
        }
    }

    private List<Double> getDblValues() {
        return m_dbl_declarations.values().stream()
                .map(Number::doubleValue)
                .collect(Collectors.toList());
    }

    private List<String> getStrValues() {
        return m_str_declarations.values().stream()
                .collect(Collectors.toList());
    }

    public TimeKeeper getTimeKeeper() {
        return m_timeKeeper;
    }

    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }

}
