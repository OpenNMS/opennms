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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Used in conjunction with RRD/JRB strategies that persist
 * metrics to the local disk.
 *
 * @author jwhite
 */
public class FilesystemResourceStorageDao implements ResourceStorageDao, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemResourceStorageDao.class);

    private static final int INFINITE_DEPTH = -1;

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
    public boolean exists(ResourcePath path) {
        return exists(path, INFINITE_DEPTH);
    }

    @Override
    public boolean exists(ResourcePath path, int depth) {
        return exists(toFile(path).toPath(), depth);
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path) {
        return children(path, INFINITE_DEPTH);
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        final File root = toFile(path);
        if (depth == 0 || !root.isDirectory()) {
            return Collections.emptySet();
        }

        try (Stream<Path> stream = Files.list(root.toPath())) {
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
        return ResourceTypeUtils.getAttributesAtRelativePath(m_rrdDirectory, toRelativePath(path), RRD_EXTENSION);
    }

    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return RrdUtils.readMetaDataFile(getRrdDirectory().getAbsolutePath(), toRelativePath(path));
    }

    @Override
    public boolean delete(ResourcePath path) {
        return FileUtils.deleteQuietly(toFile(path));
    }

    private boolean exists(Path root, int depth) {
        if (!root.toFile().isDirectory()) {
            return false;
        }

        if (depth < 0) {
            try (Stream<Path> stream = Files.walk(root)) {
                return stream.anyMatch(isRrdFile);
            } catch (IOException e) {
                LOG.error("Failed to walk {}. Marking path as non-existent.", root, e);
                return false;
            }
        } else {
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
    }

    private File toFile(ResourcePath path) {
        return Paths.get(m_rrdDirectory.getAbsolutePath(), path.elements()).toFile();
    }

    private String toRelativePath(ResourcePath path) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final String el : path) {
            if (!first) {
                sb.append(File.separator);
            } else {
                first = false;
            }
            sb.append(el);
        }
        return sb.toString();
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
