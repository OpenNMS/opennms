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
package org.opennms.features.vaadin.mibcompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.opennms.features.vaadin.api.Logger;

import com.vaadin.v7.ui.Upload;

/**
 * The Class MIB Upload Button.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class MibUploadButton extends Upload {

    /**
     * Instantiates a new MIB upload button.
     *
     * @param pendingDir the pending directory
     * @param compiledDir the compiled directory
     * @param logger the logger
     */
    public MibUploadButton(final File pendingDir, final File compiledDir, final Logger logger) {

        setCaption(null);
        setImmediate(true);
        setButtonCaption("Upload MIB");

        setReceiver(new Receiver() {
            @Override
            public OutputStream receiveUpload(String filename, String mimeType) {
                File file = new File(pendingDir, filename);
                try {
                    return new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    logger.warn("Unable to create file '" + file + "': " + e.getLocalizedMessage());
                    return null;
                }
            }
        });

        addStartedListener(new Upload.StartedListener() {
            @Override
            public void uploadStarted(StartedEvent event) {
                File pending = new File(pendingDir, event.getFilename());
                File compiled = new File(compiledDir, event.getFilename());
                if (pending.exists()) {
                    logger.warn("The file " + pending.getName() + " already exist on Pending directory.");
                } else if (compiled.exists()){
                    logger.warn("The file " + compiled.getName() + " already exist on Compiled directory.");
                } else {
                    logger.info("Uploading " + event.getFilename());
                }
            }
        });

        addFailedListener(new Upload.FailedListener() {
            @Override
            public void uploadFailed(FailedEvent event) {
                logger.warn("An error has been found: " + event.getReason() == null? "unknown error" : event.getReason().getLocalizedMessage());
            }
        });

        addSucceededListener(new Upload.SucceededListener() {
            @Override
            public void uploadSucceeded(SucceededEvent event) {
                String mibFilename = event.getFilename();
                logger.info("File " + mibFilename + " successfuly uploaded");
                uploadHandler(mibFilename);
            }
        });
    }

    /**
     * Upload handler.
     *
     * @param filename the filename
     */
    public abstract void uploadHandler(String filename);

}
