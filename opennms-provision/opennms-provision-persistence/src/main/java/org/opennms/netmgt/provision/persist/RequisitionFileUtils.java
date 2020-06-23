/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class RequisitionFileUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionFileUtils.class);

    static void createPath(final File fsPath) throws ForeignSourceRepositoryException {
        if (!fsPath.exists()) {
            if (!fsPath.mkdirs()) {
                throw new ForeignSourceRepositoryException("unable to create directory " + fsPath.getPath());
            }
        }
    }

    static File encodeFileName(final String path, final String foreignSourceName) {
        return new File(path, foreignSourceName + ".xml");
  }

    static ForeignSource getForeignSourceFromFile(final File inputFile) throws ForeignSourceRepositoryException {
        return JaxbUtils.unmarshal(ForeignSource.class, inputFile);
    }

    static Requisition getRequisitionFromFile(final File inputFile) throws ForeignSourceRepositoryException {
        try {
            return JaxbUtils.unmarshal(Requisition.class, inputFile);
        } catch (final Throwable e) {
            throw new ForeignSourceRepositoryException("unable to unmarshal " + inputFile.getPath(), e);
        }
    }

    static File getOutputFileForForeignSource(final String path, final ForeignSource foreignSource) {
        final File fsPath = new File(path);
        createPath(fsPath);
        return encodeFileName(path, foreignSource.getName());
    }

    public static File getOutputFileForRequisition(final String path, final Requisition requisition) {
        return getOutputFileForRequisition(path, requisition.getForeignSource());
    }

    public static File getOutputFileForRequisition(final String path, final String foreignSource) {
        final File reqPath = new File(path);
        createPath(reqPath);
        return encodeFileName(path, foreignSource);
    }

    public static File createSnapshot(final ForeignSourceRepository repository, final String foreignSource, final Date date) {
        final URL url = repository.getRequisitionURL(foreignSource);
        if (url == null) {
            LOG.warn("Unable to get requisition URL for foreign source {}", foreignSource);
            return null;
        }

        final String sourceFileName = getFilename(url);
        if (sourceFileName == null) {
            LOG.warn("Trying to create snapshot for {}, but getFile() doesn't return a value", url);
            return null;
        }
        final File sourceFile = new File(sourceFileName);
        
        if (!sourceFile.exists()) {
            LOG.warn("Trying to create snapshot for {}, but {} does not exist.", url, sourceFileName);
            return null;
        }

        final String targetFileName = sourceFileName + '.' + date.getTime();
        final File targetFile = new File(targetFileName);
        try {
            FileUtils.copyFile(sourceFile, targetFile, true);
            return targetFile;
        } catch (final IOException e) {
            LOG.warn("Failed to copy {} to {}", sourceFileName, targetFileName, e);
        }

        return null;
    }

    public static List<File> findSnapshots(final ForeignSourceRepository repository, final String foreignSource) {
        final List<File> files = new ArrayList<>();

        URL url = null;
        try {
            url = repository.getRequisitionURL(foreignSource);
        } catch (final ForeignSourceRepositoryException e) {
            LOG.debug("Can't find snapshots for {}, an exception occurred getting the requisition URL!", foreignSource, e);
        }

        if (url != null) {
            final String sourceFileName = getFilename(url);
            if (sourceFileName != null) {
                final File sourceFile = new File(sourceFileName);
                final File sourceDirectory = sourceFile.getParentFile();
                for (final File entry : sourceDirectory.listFiles()) {
                    if (isSnapshot(foreignSource, entry)) {
                        files.add(entry);
                    }
                }
            }
        }

        return files;
    }

    private static boolean isSnapshot(final String foreignSource, final File entry) {
        return !entry.isDirectory() && entry.getName().matches(foreignSource + ".xml.\\d+");
    }

    public static void deleteResourceIfSnapshot(final Requisition requisition) {
        final Resource resource = requisition.getResource();
        if (resource == null) return;

        try {
            final File resourceFile = resource.getFile();
            if (isSnapshot(requisition.getForeignSource(), resourceFile)) {
                LOG.trace("Deleting {}", resourceFile);
                if (!resourceFile.delete()) {
                    LOG.debug("Failed to delete {}", resourceFile);
                }
            }
        } catch (final IOException e) {
            LOG.debug("Resource {} can't be turned into a file, skipping snapshot delete detection.", resource, e);
            return;
        }
        
    }

    public static void deleteSnapshotsOlderThan(final ForeignSourceRepository repository, final String foreignSource, final Date date) {
        for (final File snapshotFile : findSnapshots(repository, foreignSource)) {
            if (!isNewer(snapshotFile, date)) {
                LOG.trace("Deleting {}", snapshotFile);
                if(!snapshotFile.delete()) {
                	LOG.warn("Could not delete file: {}", snapshotFile.getPath());
                }
            }
        }
    }

    public static void deleteAllSnapshots(final  ForeignSourceRepository repository) {
        for (final String foreignSource : repository.getActiveForeignSourceNames()) {
            final List<File> snapshots = findSnapshots(repository, foreignSource);
            for (final File snapshot : snapshots) {
                LOG.trace("Deleting {}", snapshot);
                if(!snapshot.delete()) {
                	LOG.warn("Could not delete file: {}", snapshot.getPath());
                }
            }
        }
    }

    public static Requisition getLatestPendingOrSnapshotRequisition(final ForeignSourceRepository foreignSourceRepository, final String foreignSource) {
        Requisition newest = foreignSourceRepository.getRequisition(foreignSource);
        for (final File snapshotFile : findSnapshots(foreignSourceRepository, foreignSource)) {
            if (newest == null || isNewer(snapshotFile, newest.getDate())) {
                newest = JaxbUtils.unmarshal(Requisition.class, snapshotFile);
                newest.setResource(new FileSystemResource(snapshotFile));
            }
        }
        return newest;
    }

    /** return true if the snapshot file is newer than the supplied date **/
    public static boolean isNewer(final File snapshotFile, final Date date) {
        final String name = snapshotFile.getName();
        final String timestamp = name.substring(name.lastIndexOf('.') + 1);
        final Date snapshotDate = new Date(Long.valueOf(timestamp));
        final boolean isNewer = snapshotDate.after(date);
        LOG.trace("snapshot date = {}, comparison date = {}, snapshot date {} newer than comparison date", snapshotDate.getTime(), date.getTime(), (isNewer? "is" : "is not"));
        return isNewer;
    }

    private static String getFilename(final URL url) {
        try {
            return URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Failed to decode URL {} as a file.", url.getFile(), e);
            return null;
        }
    }
}
