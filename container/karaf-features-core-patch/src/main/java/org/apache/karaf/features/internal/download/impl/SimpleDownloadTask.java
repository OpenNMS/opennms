/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.features.internal.download.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.karaf.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDownloadTask extends AbstractRetryableDownloadTask {

    private static final String BLUEPRINT_PREFIX = "blueprint:";
    private static final String SPRING_PREFIX = "spring:";

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDownloadTask.class);

    private File basePath;

    public SimpleDownloadTask(ScheduledExecutorService executorService, String url, File basePath) {
        super(executorService, url);
        this.basePath = basePath;
    }

    @Override
    protected File download(Exception previousExceptionNotUsed) throws Exception {
        LOG.trace("Downloading [" + url + "]");

        if (url.startsWith(BLUEPRINT_PREFIX) || url.startsWith(SPRING_PREFIX)) {
            return downloadBlueprintOrSpring();
        }

        try {
            basePath.mkdirs();
            if (!basePath.isDirectory()) {
                throw new IOException("Unable to create directory " + basePath.toString());
            }

            URL urlObj = new URL(url);
            File file = new File(basePath, getFileName(urlObj));
            if (file.exists()) {
                return file;
            }

            File dir = new File(System.getProperty("karaf.data"), "tmp");
            dir.mkdirs();
            if (!dir.isDirectory()) {
                throw new IOException("Unable to create directory " + dir.toString());
            }

            File tmpFile = Files.createTempFile(dir.toPath(), "download-", null).toFile();

            urlObj = new URL(DownloadManagerHelper.stripStartLevel(urlObj.toString()));
            try (InputStream is = urlObj.openStream();
                 OutputStream os = new FileOutputStream(tmpFile)) {
                StreamUtils.copy(is, os);
            }

            // OPENNMS (NMS-19841, filed upstream as apache/karaf#2808): the target
            // filename is derived solely from the URL, so
            // two overlapping downloads of the same URL (e.g. from two feature installs
            // that race against each other, each with their own DownloadManager/cache)
            // both land here. The original delete()-then-renameTo() left a window where
            // `file` did not exist at all, which a concurrent reader could observe as a
            // FileNotFoundException. Files.move(..., REPLACE_EXISTING) replaces the
            // destination in one atomic filesystem operation instead, so a concurrent
            // reader always sees either the old or the new (equivalent) content.
            try {
                Files.move(tmpFile.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return file;
        } catch (Exception ignore) {
            throw new IOException("Could not download [" + this.url + "]", ignore);
        }
    }

    // we only want the filename itself, not the whole path
    private String getFileName(URL urlObj) {
        String url = urlObj.getFile();
        // ENTESB-1394: we do not want all these decorators from wrap: protocol
        // or any inlined maven repos
        url = DownloadManagerHelper.stripUrl(url);
        url = DownloadManagerHelper.removeInlinedMavenRepositoryUrl(url);
        int unixPos = url.lastIndexOf('/');
        int windowsPos = url.lastIndexOf('\\');
        url = url.substring(Math.max(unixPos, windowsPos) + 1);
        url = Integer.toHexString(urlObj.toString().hashCode()) + "-" + url;
        return url;
    }

    protected File downloadBlueprintOrSpring() throws Exception {
        // when downloading an embedded blueprint or spring xml file, then it must be as a temporary file
        File dir = new File(System.getProperty("karaf.data"), "tmp");
        dir.mkdirs();
        File tmpFile = Files.createTempFile(dir.toPath(), "download-", null).toFile();
        try (InputStream is = new URL(url).openStream();
             OutputStream os = new FileOutputStream(tmpFile))
        {
            StreamUtils.copy(is, os);
        }
        return tmpFile;
    }

    @Override
    protected Retry isRetryable(IOException e) {
        // TODO: check http errors, etc.
        return super.isRetryable(e);
    }

}
