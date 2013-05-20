/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.opennms.features.vaadin.api.Logger;

import com.vaadin.ui.Upload;

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
