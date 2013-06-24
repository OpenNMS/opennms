package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.Resource;

public class RequisitionFileUtils {

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

    static File getOutputFileForRequisition(final String path, final Requisition requisition) {
        final File reqPath = new File(path);
        createPath(reqPath);
        return encodeFileName(path, requisition.getForeignSource());
    }

    public static File createSnapshot(final ForeignSourceRepository repository, final String foreignSource, final Date date) {
        final URL url = repository.getRequisitionURL(foreignSource);
        if (url == null) {
            LogUtils.warnf(RequisitionFileUtils.class, "Unable to get requisition URL for foreign source %s", foreignSource);
            return null;
        }

        final String sourceFileName = url.getFile();
        if (sourceFileName == null) {
            LogUtils.warnf(RequisitionFileUtils.class, "Trying to create snapshot for %s, but getFile() doesn't return a value", url);
            return null;
        }
        final File sourceFile = new File(sourceFileName);
        
        if (!sourceFile.exists()) {
            LogUtils.warnf(RequisitionFileUtils.class, "Trying to create snapshot for %s, but %s does not exist.", url, sourceFileName);
            return null;
        }

        final String targetFileName = sourceFileName + '.' + date.getTime();
        final File targetFile = new File(targetFileName);
        try {
            FileUtils.copyFile(sourceFile, targetFile, true);
            return targetFile;
        } catch (final IOException e) {
            LogUtils.warnf(RequisitionFileUtils.class, e, "Failed to copy %s to %s", sourceFileName, targetFileName);
        }

        return null;
    }

    public static List<File> findSnapshots(final ForeignSourceRepository repository, final String foreignSource) {
        final List<File> files = new ArrayList<File>();

        URL url = null;
        try {
            url = repository.getRequisitionURL(foreignSource);
        } catch (final ForeignSourceRepositoryException e) {
            LogUtils.debugf(RequisitionFileUtils.class, e, "Can't find snapshots for %s, an exception occurred getting the requisition URL!", foreignSource);
        }

        if (url != null) {
            final String sourceFileName = url.getFile();
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
                if (!resourceFile.delete()) {
                    LogUtils.debugf(RequisitionFileUtils.class, "Failed to delete %s", resourceFile);
                }
            }
        } catch (final IOException e) {
            LogUtils.debugf(RequisitionFileUtils.class, e, "Resource %s can't be turned into a file, skipping snapshot delete detection.", resource);
            return;
        }
        
    }

    public static void deleteAllSnapshots(final  ForeignSourceRepository repository) {
        for (final String foreignSource : repository.getActiveForeignSourceNames()) {
            final List<File> snapshots = findSnapshots(repository, foreignSource);
            for (final File snapshot : snapshots) {
                snapshot.delete();
            }
        }
    }

}
