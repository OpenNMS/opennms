/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
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
 * Used in conjunction with RRD/JRB strategies that persist
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
