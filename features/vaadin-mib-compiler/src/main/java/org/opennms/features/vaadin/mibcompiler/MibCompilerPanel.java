/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.vaadin.mibcompiler.services.MibParser;
import org.opennms.features.vaadin.mibcompiler.services.MibbleMibParser;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.TextFileProperty;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

/**
 * The Class MIB Compiler Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibCompilerPanel extends HorizontalSplitPanel {

    /** The Constant PENDING. */
    private static final String PENDING = "pending";

    /** The Constant COMPILED. */
    private static final String COMPILED = "compiled";

    /** The Constant MIBS_ROOT_DIR. */
    private static final File MIBS_ROOT_DIR = new File("/Users/agalue/temporal"); // FIXME

    /** The Constant MIBS_COMPILED_DIR. */
    private static final File MIBS_COMPILED_DIR = new File(MIBS_ROOT_DIR, COMPILED);

    /** The Constant MIBS_PENDING_DIR. */
    private static final File MIBS_PENDING_DIR = new File(MIBS_ROOT_DIR, PENDING);

    /** The Constant ERROR. */
    private static final String ERROR = "<b><font color='red'>&nbsp;[ERROR]&nbsp;</font></b>";

    /** The Constant WARN. */
    private static final String WARN  = "<b><font color='orange'>&nbsp;[WARN]&nbsp;</font></b>";

    /** The Constant INFO. */
    private static final String INFO  = "<b><font color='green'>&nbsp;[INFO]&nbsp;</font></b>";

    /** The Constant ACTION_EDIT. */
    private static final Action ACTION_EDIT = new Action("Edit MIB");

    /** The Constant ACTION_COMPILE. */
    private static final Action ACTION_COMPILE = new Action("Compile MIB");

    /** The Constant ACTION_EVENTS. */
    private static final Action ACTION_EVENTS = new Action("Generate Events");

    /** The Constant ACTION_COLLECT. */
    private static final Action ACTION_COLLECT = new Action("Generate Data Collection");

    /** The MIB console. */
    private final Panel mibConsole;

    /** The MIB controls. */
    private final Panel mibControls;

    /** The upload. */
    private final Upload upload;

    /** The MIBs tree. */
    private final Tree mibsTree;

    /** The MIBs container. */
    private final HierarchicalContainer mibsContainer;

    /** The MIB parser. */
    private final MibParser mibParser;

    /**
     * Instantiates a new compile panel.
     */
    public MibCompilerPanel() {

        // Parser Configuration

        mibParser = new MibbleMibParser();
        mibParser.addMibDirectory(MIBS_COMPILED_DIR);

        // Initialize Objects

        upload = new Upload();
        mibControls = new Panel("MIB Compiler");
        mibsTree = new Tree("MIB Tree");
        mibConsole = new Panel("MIB Console");

        // Setup Controls Panel - Top Toolbar

        initUploadButton();
        mibControls.addComponent(upload);
        ((VerticalLayout) mibControls.getContent()).setComponentAlignment(upload, Alignment.TOP_RIGHT);

        // Setup Controls Panel - MIB Tree

        mibsContainer = new HierarchicalContainer();
        initMibTree();
        mibControls.addComponent(mibsTree);

        // Setup Controls Panel

        mibControls.setSizeFull();
        mibControls.addStyleName(Runo.PANEL_LIGHT);
        ((VerticalLayout) mibControls.getContent()).setExpandRatio(mibsTree, 1);

        // Setup Console Panel

        mibConsole.setSizeFull();
        mibConsole.addStyleName(Runo.PANEL_LIGHT);

        // Setup Main Compile Panel

        setSizeFull();
        setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);
        addComponent(mibControls);
        addComponent(mibConsole);
    }

    /**
     * Initialize the upload button.
     */
    private void initUploadButton() {
        upload.setCaption(null);
        upload.setImmediate(true);
        upload.setButtonCaption("Upload");

        upload.setReceiver(new Receiver() {
            public OutputStream receiveUpload(String filename, String mimeType) {
                File file = new File(MIBS_PENDING_DIR, filename);
                try {
                    return new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    return null;
                }
            }
        });

        upload.addListener(new Upload.StartedListener() {
            public void uploadStarted(StartedEvent event) {
                File pending = new File(MIBS_PENDING_DIR, event.getFilename());
                File compiled = new File(MIBS_COMPILED_DIR, event.getFilename());
                if (pending.exists()) {
                    event.getUpload().interruptUpload();
                    logMsg(WARN, "File " + pending.getName() + " already exist on Pending directory.");
                } else if (compiled.exists()){
                    event.getUpload().interruptUpload();
                    logMsg(WARN, "File " + compiled.getName() + " already exist on Compiled directory.");
                } else {
                    logMsg(INFO, "Uploading " + event.getFilename());
                }
            }
        });

        upload.addListener(new Upload.FailedListener() {
            public void uploadFailed(FailedEvent event) {
                logMsg(WARN, "An error has been found");
            }
        });

        upload.addListener(new Upload.SucceededListener() {
            public void uploadSucceeded(SucceededEvent event) {
                String mibFilename = event.getFilename();
                logMsg(INFO, "File " + mibFilename + " successfuly uploaded");
                addTreeItem(mibFilename, PENDING);
            }
        });
    }

    /**
     * Initialize the MIB tree.
     */
    private void initMibTree() {
        if (! MIBS_ROOT_DIR.exists()) {
            MIBS_ROOT_DIR.mkdirs();
            MIBS_COMPILED_DIR.mkdirs();
            MIBS_PENDING_DIR.mkdirs();
        }

        File[] folders = new File[] { MIBS_COMPILED_DIR, MIBS_PENDING_DIR };
        for (File folder : folders) {
            addTreeItem(folder.getName(), null);
        }
        for (File folder : folders) {
            String[] files = folder.list();
            for (String file : files) {
                addTreeItem(file, folder.getName());
            }
        }

        mibsTree.setContainerDataSource(mibsContainer);
        mibsTree.expandItemsRecursively(COMPILED);
        mibsTree.expandItemsRecursively(PENDING);

        mibsTree.addActionHandler(new Action.Handler() {

            public Action[] getActions(Object target, Object sender) {
                if (target == null) {
                    return new Action[] {};
                }
                Object parent = mibsContainer.getParent(target);
                if (parent == null) {
                    return new Action[] {};
                }
                if (parent.equals(COMPILED)) {
                    LogUtils.debugf(this, "Adding actions for COMPILED MIB %s", target);
                    return new Action[] { ACTION_EVENTS, ACTION_COLLECT };
                } else {
                    LogUtils.debugf(this, "Adding actions for PENDING MIB %s", target);
                    return new Action[] { ACTION_EDIT, ACTION_COMPILE };
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                String fileName = (String) target;
                if (action == ACTION_EDIT) {
                    Window w = new MibEditWindow(new TextFileProperty(new File(MIBS_PENDING_DIR, fileName)));
                    getApplication().getMainWindow().addWindow(w);
                }
                if (action == ACTION_COMPILE) {
                    if (parseMib(new File(MIBS_PENDING_DIR, fileName))) {
                        mibsTree.removeItem(target);
                        addTreeItem(fileName, COMPILED);
                        File file = new File(MIBS_PENDING_DIR, fileName);
                        file.renameTo(new File(MIBS_COMPILED_DIR, file.getName()));
                    }
                }
                if (action == ACTION_EVENTS) { // FIXME Must be improved and should use a wizard after processing data.
                    logMsg(INFO, "Processing events for " + fileName);
                    if (parseMib(new File(MIBS_COMPILED_DIR, fileName))) {
                        final Events events = mibParser.getEvents("uei.example/traps"); // FIXME
                        logMsg(INFO, "Found " + events.getEventCount() + " events.");
                        final Window w = new Window(fileName);
                        w.setScrollable(true);
                        w.setClosable(false);
                        w.setDraggable(false);
                        w.addStyleName(Runo.PANEL_LIGHT);
                        w.setSizeFull();
                        w.setContent(new EventPanel(events) {
                            void cancelProcessing() {
                                getApplication().getMainWindow().removeWindow(w);
                            }
                            void generateEventFile() { // FIXME This is not elegant.
                                StringWriter writer = new StringWriter();
                                try {
                                    events.setEvent(this.getOnmsEvents());
                                    events.marshal(writer);
                                    logMsg(INFO, "<pre>" + writer.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</pre>");
                                } catch (Exception e) {
                                    logMsg(ERROR, e.getMessage());
                                }
                                getApplication().getMainWindow().removeWindow(w);
                            }
                        });
                        getApplication().getMainWindow().addWindow(w);
                    }
                }
                if (action == ACTION_COLLECT) {
                    getApplication().getMainWindow().showNotification("Not yet but comming soon!");
                }
            }
        });
    }

    /**
     * Adds the tree item.
     *
     * @param label the label
     * @param parent the parent
     */
    private void addTreeItem(String label, String parent) {
        mibsContainer.addItem(label);
        if (parent == null) {
            LogUtils.debugf(this, "Adding root directory %s", label);
            mibsContainer.setChildrenAllowed(parent, true);
        } else {
            LogUtils.debugf(this, "Adding item %s to %s folder", label, parent);
            mibsContainer.setParent(label, parent);
            mibsContainer.setChildrenAllowed(label, false);
        }
    }

    /**
     * Parses the MIB.
     *
     * @param mibFile the MIB file
     * @return true, if successful
     */
    public boolean parseMib(File mibFile) {
        logMsg(INFO, "Parsing MIB file " + mibFile);
        if (mibParser.parseMib(mibFile)) {
            logMsg(INFO, "MIB parsed successfuly.");
            return true;
        } else {
            List<String> dependencies = mibParser.getMissingDependencies();
            if (dependencies.isEmpty()) {
                logMsg(ERROR, "Found problem when compiling the MIB: <pre>" + mibParser.getFormattedErrors() + "</pre>");
            } else {
                logMsg(ERROR, "Dependencies required: <b>" + dependencies + "</b>");
            }
        }
        return false;
    }

    /**
     * Log Message.
     *
     * @param level the level
     * @param message the message
     */
    public void logMsg(String level, String message) {
        String msg = new Date().toString() + level + message;
        Label error = new Label(msg, Label.CONTENT_XHTML);
        mibConsole.addComponent(error);
        LogUtils.infof(this, message);
    }

}
