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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdMetaDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Used in conjunction with RRD strategies that persist
 * metrics to the local disk.
 *
 * @author jwhite
 */
public class FilesystemResourceStorageDao implements ResourceStorageDao, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemResourceStorageDao.class);

    @Autowired
    private RrdStrategy<?, ?> m_rrdStrategy;

    private static String RRD_EXTENSION = null;

    private File m_rrdDirectory;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        setRrdExtension(m_rrdStrategy.getDefaultFileExtension());
    }

    @Override
    public boolean exists(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        return exists(toPath(path), depth);
    }

    @Override
    public boolean existsWithin(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        return existsWithin(toPath(path), depth);
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth > 0, "depth must be positive");
        final Path root = toPath(path);
        if (!Files.isDirectory(root)) {
            return Collections.emptySet();
        }

        try (Stream<Path> stream = Files.list(root)) {
            return stream.filter(p -> p.toFile().isDirectory()) // filter for directories
                .filter(p -> exists(p, depth-1)) // filter for folders with metrics
                .map(p -> ResourcePath.get(path, p.toFile().getName()))
                .collect(Collectors.toSet());
        } catch (IOException e) {
            LOG.error("Failed to list {}. Returning empty set of children.", path, e);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        return RrdResourceAttributeUtils.getAttributesAtRelativePath(m_rrdDirectory, ResourcePath.resourceToFilesystemPath(path).toString(), RRD_EXTENSION);
    }

    @Override
    public void setStringAttribute(ResourcePath path, String key, String value) {
        try {
            RrdResourceAttributeUtils.updateStringProperty(toPath(path).toFile(), value, key);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getStringAttribute(ResourcePath path, String key) {
        return RrdResourceAttributeUtils.getStringProperty(toPath(path).toFile(), key);
    }

    @Override
    public Map<String, String> getStringAttributes(ResourcePath path) {
        Properties props = RrdResourceAttributeUtils.getStringProperties(m_rrdDirectory, ResourcePath.resourceToFilesystemPath(path).toString());
        return Maps.fromProperties(props);
    }

    @Override
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames) {
        RrdResourceAttributeUtils.updateDsProperties(toPath(path).toFile(), metricsNameToResourceNames);
    }

    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return RrdMetaDataUtils.readMetaDataFile(getRrdDirectory(), ResourcePath.resourceToFilesystemPath(path).toString());
    }

    @Override
    public boolean delete(ResourcePath path) {
        return FileUtils.deleteQuietly(toPath(path).toFile());
    }

    private boolean exists(Path root, int depth) {
        if (!root.toFile().isDirectory()) {
            return false;
        }

        try (Stream<Path> stream = Files.list(root)) {
            if (depth == 0) {
                return stream.anyMatch(isRrdFile);
            } else {
                return stream.anyMatch(p -> exists(p, depth-1));
            }
        } catch (IOException e) {
            LOG.error("Failed to list {}. Marking path as non-existent.", root, e);
            return false;
        }
    }

    private boolean existsWithin(Path root, int depth) {
        if (depth < 0 || !root.toFile().isDirectory()) {
            return false;
        }

        try (Stream<Path> stream = Files.list(root)) {
            return stream.anyMatch(p -> (isRrdFile.test(p) || existsWithin(p, depth-1)));
        } catch (IOException e) {
            LOG.error("Failed to list {}. Marking path as non-existent.", root, e);
            return false;
        }
    }

    private Path toPath(final ResourcePath path) {
        return m_rrdDirectory.getAbsoluteFile().toPath().resolve(ResourcePath.resourceToFilesystemPath(path));
    }

    public void setRrdDirectory(File rrdDirectory) {
        m_rrdDirectory = rrdDirectory;
    }

    public File getRrdDirectory() {
        return m_rrdDirectory;
    }

    public void setRrdStrategy(RrdStrategy<?, ?> rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
        setRrdExtension(m_rrdStrategy.getDefaultFileExtension());
    }

    public void setRrdExtension(String rrdExtension) {
        RRD_EXTENSION = rrdExtension;
    }

    private static Predicate<Path> isRrdFile = new Predicate<Path>() {
        @Override
        public boolean test(Path path) {
            final File file = path.toFile();
            return file.isFile() && file.getName().endsWith(RRD_EXTENSION);
        }
    };
}
