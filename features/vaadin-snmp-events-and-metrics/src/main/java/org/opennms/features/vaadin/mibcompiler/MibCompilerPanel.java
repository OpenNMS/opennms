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

package org.opennms.features.vaadin.mibcompiler;

import java.io.File;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.datacollection.DataCollectionWindow;
import org.opennms.features.vaadin.events.EventWindow;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.event.Action;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

/**
 * The Class MIB Compiler Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibCompilerPanel extends Panel {

    /** The Constant LOG. */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MibCompilerPanel.class);

    /** The Constant PENDING. */
    private static final String PENDING = "pending";

    /** The Constant COMPILED. */
    private static final String COMPILED = "compiled";

    /** The Constant MIB_FILE_EXTENTION. */
    private static final String MIB_FILE_EXTENTION = ".mib";

    /** The Constant MIBS_ROOT_DIR. */
    // TODO Make the MIBs directory configurable
    private static final File MIBS_ROOT_DIR = new File(ConfigFileConstants.getHome(),  "share" + File.separatorChar + "mibs");

    /** The Constant MIBS_COMPILED_DIR. */
    private static final File MIBS_COMPILED_DIR = new File(MIBS_ROOT_DIR, COMPILED);

    /** The Constant MIBS_PENDING_DIR. */
    private static final File MIBS_PENDING_DIR = new File(MIBS_ROOT_DIR, PENDING);

    /** The Constant ACTION_EDIT. */
    private static final Action ACTION_EDIT = new Action("Edit MIB");

    /** The Constant ACTION_DELETE. */
    private static final Action ACTION_DELETE = new Action("Delete MIB");

    /** The Constant ACTION_VIEW. */
    private static final Action ACTION_VIEW = new Action("View MIB");

    /** The Constant ACTION_COMPILE. */
    private static final Action ACTION_COMPILE = new Action("Compile MIB");

    /** The Constant ACTION_EVENTS. */
    private static final Action ACTION_EVENTS = new Action("Generate Events");

    /** The Constant ACTION_COLLECT. */
    private static final Action ACTION_COLLECT = new Action("Generate Data Collection");

    /** The MIBs tree. */
    private final Tree mibsTree;

    /** The MIB parser. */
    private final MibParser mibParser;

    /** The Events Configuration DAO. */
    private EventConfDao eventsDao;

    /** The Events Proxy. */
    private EventProxy eventsProxy;

    /** The Data Collection Configuration DAO. */
    private DataCollectionConfigDao dataCollectionDao;

    /**
     * Instantiates a new MIB tree panel.
     *
     * @param dataCollectionDao the OpenNMS Data Collection Configuration DAO 
     * @param eventsDao the OpenNMS Events Configuration DAO
     * @param eventsProxy the OpenNMS Events Proxy
     * @param mibParser the MIB parser
     * @param logger the logger
     */
    public MibCompilerPanel(final DataCollectionConfigDao dataCollectionDao, final EventConfDao eventsDao, final EventProxy eventsProxy, final MibParser mibParser, final Logger logger) {
        super("MIB Compiler");

        if (dataCollectionDao == null)
            throw new RuntimeException("dataCollectionDao cannot be null.");
        if (eventsProxy == null)
            throw new RuntimeException("eventProxy cannot be null.");
        if (eventsDao == null)
            throw new RuntimeException("eventsDao cannot be null.");

        this.eventsDao = eventsDao;
        this.eventsProxy = eventsProxy;
        this.dataCollectionDao = dataCollectionDao;

        logger.info("Reading MIBs from " + MIBS_ROOT_DIR);

        // Make sure MIB directories exist

        if (!MIBS_COMPILED_DIR.exists()) {
            if (!MIBS_COMPILED_DIR.mkdirs()) {
                throw new RuntimeException("Unable to create directory for compiled MIBs (" + MIBS_COMPILED_DIR + ")");
            }
        }
        if (!MIBS_PENDING_DIR.exists()) {
            if (!MIBS_PENDING_DIR.mkdirs()) {
                throw new RuntimeException("Unable to create directory for pending MIBs (" + MIBS_PENDING_DIR + ")");
            }
        }

        // Parser Configuration

        this.mibParser = mibParser;
        mibParser.setMibDirectory(MIBS_COMPILED_DIR);

        // Initialize Toolbar

        MibUploadButton upload = new MibUploadButton(MIBS_PENDING_DIR, MIBS_COMPILED_DIR, logger) {
            @Override
            public void uploadHandler(String filename) {
                addTreeItem(filename, PENDING);
            }
        };

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(upload);

        // Initialize MIB Tree

        mibsTree = new Tree("MIB Tree");
        initMibTree(logger);
        final Label label = new Label("<p>Use the right-click context menu over the MIB tree files, to display the compiler operations.</p>");
        label.setContentMode(ContentMode.HTML);


        layout.addComponent(label);
        layout.addComponent(mibsTree);

        // Panel Setup
        setSizeFull();
        addStyleName("light");
        layout.setComponentAlignment(upload, Alignment.TOP_RIGHT);
        layout.setExpandRatio(mibsTree, 1);

        setContent(layout);
    }

    /**
     * Initialize the MIB tree.
     *
     * @param logger the logger
     */
    private void initMibTree(final Logger logger) {
        File[] folders = new File[] { MIBS_COMPILED_DIR, MIBS_PENDING_DIR };
        for (File folder : folders) {
            addTreeItem(folder.getName(), null);
        }
        for (File folder : folders) {
            String[] files = folder.list();
            if (files == null) continue;
            for (String file : files) {
                addTreeItem(file, folder.getName());
            }
        }

        mibsTree.expandItemsRecursively(COMPILED);
        mibsTree.expandItemsRecursively(PENDING);

        mibsTree.addActionHandler(new Action.Handler() {

            @Override
            public Action[] getActions(Object target, Object sender) {
                if (target == null) {
                    return new Action[] {};
                }
                Object parent = mibsTree.getParent(target);
                if (parent == null) {
                    return new Action[] {};
                }
                if (parent.equals(COMPILED)) {
                    return new Action[] { ACTION_EVENTS, ACTION_COLLECT, ACTION_VIEW, ACTION_DELETE };
                } else {
                    return new Action[] { ACTION_EDIT, ACTION_DELETE, ACTION_COMPILE };
                }
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                final String fileName = (String) target;
                if (action == ACTION_DELETE) {
                    ConfirmDialog.show(getUI(),
                                       "Are you sure?",
                                       "Do you really want to delete " + fileName + "?\nThis cannot be undone.",
                                       "Yes",
                                       "No",
                                       new ConfirmDialog.Listener() {
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                String source = mibsTree.getParent(fileName).toString();
                                File file = new File(PENDING.equals(source) ? MIBS_PENDING_DIR : MIBS_COMPILED_DIR, fileName);
                                if (file.delete()) {
                                    mibsTree.removeItem(fileName);
                                    logger.info("MIB " + file + " has been successfully removed.");
                                } else {
                                    Notification.show("Can't delete " + file);
                                }
                            }
                        }
                    });
                }
                if (action == ACTION_EDIT) {
                    Window w = new FileEditorWindow(new File(MIBS_PENDING_DIR, fileName), logger, false);
                    getUI().addWindow(w);
                }
                if (action == ACTION_VIEW) {
                    Window w = new FileEditorWindow(new File(MIBS_COMPILED_DIR, fileName), logger, true);
                    getUI().addWindow(w);
                }
                if (action == ACTION_COMPILE) {
                    if (parseMib(logger, new File(MIBS_PENDING_DIR, fileName))) {
                        // Renaming the file to be sure that the target name is correct and always has a file extension.
                        final String mibFileName = mibParser.getMibName() + MIB_FILE_EXTENTION;
                        final File currentFile = new File(MIBS_PENDING_DIR, fileName);
                        final File suggestedFile = new File(MIBS_COMPILED_DIR, mibFileName);
                        if (suggestedFile.exists()) {
                            ConfirmDialog.show(getUI(),
                                               "Are you sure?",
                                                   "The MIB " + mibFileName + " already exist on the compiled directory?<br/>Override the existing file could break other compiled mibs, so proceed with caution.<br/>This cannot be undone.",
                                                   "Yes",
                                                   "No",
                                                   new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        renameFile(logger, currentFile, suggestedFile);
                                    }
                                }
                            });
                        } else {
                            renameFile(logger, currentFile, suggestedFile);
                        }
                    }
                }
                if (action == ACTION_EVENTS) {
                    generateEvents(logger, fileName);
                }
                if (action == ACTION_COLLECT) {
                    generateDataCollection(logger, fileName);
                }
            }
        });
    }

    /**
     * Rename file.
     *
     * @param logger the logger
     * @param currentFile the current file
     * @param suggestedFile the suggested file
     */
    private void renameFile(Logger logger, File currentFile, File suggestedFile) {
        logger.info("Renaming file " + currentFile.getName() + " to " + suggestedFile.getName());
        mibsTree.removeItem(currentFile.getName());
        addTreeItem(suggestedFile.getName(), COMPILED);
        if(!currentFile.renameTo(suggestedFile)) {
        	LOG.warn("Could not rename file: {}", currentFile.getPath());
        }
    }

    /**
     * Adds the tree item.
     *
     * @param label the label
     * @param parent the parent
     */
    // FIXME: It sounds reasonable to sort the tree after adding a new MIB ?
    private void addTreeItem(final String label, final String parent) {
        mibsTree.addItem(label);
        if (parent == null) {
            LOG.debug("Adding root directory {}", label);
            mibsTree.setChildrenAllowed(parent, true);
        } else {
            LOG.debug("Adding item {} to {} folder", label, parent);
            mibsTree.setParent(label, parent);
            mibsTree.setChildrenAllowed(label, false);
        }
    }

    /**
     * Parses the MIB.
     *
     * @param logger the logger
     * @param mibFile the MIB file
     * @return true, if successful
     */
    private boolean parseMib(final Logger logger, final File mibFile) {
        logger.info("Parsing MIB file " + mibFile);
        if (mibParser.parseMib(mibFile)) {
            logger.info("MIB parsed successfuly.");
            return true;
        } else {
            List<String> dependencies = mibParser.getMissingDependencies();
            if (dependencies.isEmpty()) {
                // FIXME Is this the best way to add a custom CSS ?
                String preStyle = "white-space: pre-wrap; white-space: -moz-pre-wrap; white-space: -pre-wrap; white-space: -o-pre-wrap; word-wrap: break-word;";
                logger.error("Problem found when compiling the MIB: <pre style=\"" + preStyle + "\">" + mibParser.getFormattedErrors() + "</pre>");
            } else {
                logger.error("Dependencies required: <b>" + dependencies + "</b>");
            }
        }
        return false;
    }

    /**
     * Generate events.
     *
     * @param logger the logger
     * @param fileName the file name
     */
    private void generateEvents(final Logger logger, final String fileName) {
        if (parseMib(logger, new File(MIBS_COMPILED_DIR, fileName))) {
            final EventUeiWindow w = new EventUeiWindow("uei.opennms.org/traps/" + mibParser.getMibName()) {
                @Override
                public void changeUeiHandler(String ueiBase) {
                    showEventsWindow(logger, fileName, ueiBase);
                }
            };
            getUI().addWindow(w);
        }
    }

    /**
     * Shows the events window.
     *
     * @param logger the logger
     * @param fileName the file name
     * @param ueiBase the UEI base
     */
    private void showEventsWindow(final Logger logger, final String fileName, final String ueiBase) {
        final Events events =  mibParser.getEvents(ueiBase);
        if (events == null) {
            Notification.show("The MIB couldn't be processed for events because: " + mibParser.getFormattedErrors(), Notification.Type.ERROR_MESSAGE);                
        } else {
            if (events.getEventCount() > 0) {
                try {
                    logger.info("Found " + events.getEventCount() + " events.");
                    final String eventsFileName = fileName.replaceFirst("\\..*$", ".events.xml");
                    final File configDir = new File(ConfigFileConstants.getHome(), "etc" + File.separatorChar + "events");
                    final File eventFile = new File(configDir, eventsFileName);
                    final EventWindow w = new EventWindow(eventsDao, eventsProxy, eventFile, events, logger);
                    getUI().addWindow(w);
                } catch (Throwable t) {
                    Notification.show(t.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            } else {
                Notification.show("The MIB doesn't contain any notification/trap", Notification.Type.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Generate data collection.
     *
     * @param logger the logger
     * @param fileName the file name
     */
    private void generateDataCollection(final Logger logger, final String fileName) {
        if (parseMib(logger, new File(MIBS_COMPILED_DIR, fileName))) {
            final DatacollectionGroup dcGroup = mibParser.getDataCollection();
            if (dcGroup == null) {
                Notification.show("The MIB couldn't be processed for data collection because: " + mibParser.getFormattedErrors(), Notification.Type.ERROR_MESSAGE);
            } else {
                if (dcGroup.getGroups().size() > 0) {
                    try {
                        final String dataFileName = fileName.replaceFirst("\\..*$", ".xml");
                        final DataCollectionWindow w = new DataCollectionWindow(mibParser, dataCollectionDao, dataFileName, dcGroup, logger);
                        getUI().addWindow(w);
                    } catch (Throwable t) {
                        Notification.show(t.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                } else {
                    Notification.show("The MIB doesn't contain any metric for data collection.", Notification.Type.WARNING_MESSAGE);
                }
            }
        }
    }

}
