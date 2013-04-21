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
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.datacollection.DataCollectionWindow;
import org.opennms.features.vaadin.events.EventWindow;
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.eventconf.Events;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Class MIB Compiler Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibCompilerPanel extends Panel {

    /** The Constant PENDING. */
    private static final String PENDING = "pending";

    /** The Constant COMPILED. */
    private static final String COMPILED = "compiled";

    /** The Constant MIBS_ROOT_DIR. */
    private static final File MIBS_ROOT_DIR = new File(ConfigFileConstants.getHome(),  "/share/mibs"); // TODO Must be configurable

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

    /** The MIBs container. */
    private final HierarchicalContainer mibsContainer;

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
            public void uploadHandler(String filename) {
                addTreeItem(filename, PENDING);
            }
        };
        addComponent(upload);

        // Initialize MIB Tree

        mibsContainer = new HierarchicalContainer();
        mibsTree = new Tree("MIB Tree");
        initMibTree(logger);
        final Label label = new Label("<p>Use the right-click context menu over the MIB tree files, to display the compiler operations.</p>"
                                      + "<p>The file name requires to be the same as the MIB to be processed.</p>");
        label.setContentMode(Label.CONTENT_XHTML);
        addComponent(label);
        addComponent(mibsTree);

        // Panel Setup

        setSizeFull();
        addStyleName(Runo.PANEL_LIGHT);
        ((VerticalLayout) getContent()).setComponentAlignment(upload, Alignment.TOP_RIGHT);
        ((VerticalLayout) getContent()).setExpandRatio(mibsTree, 1);
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
                    return new Action[] { ACTION_EVENTS, ACTION_COLLECT, ACTION_VIEW, ACTION_DELETE };
                } else {
                    return new Action[] { ACTION_EDIT, ACTION_DELETE, ACTION_COMPILE };
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                final String fileName = (String) target;
                if (action == ACTION_DELETE) {
                    MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                                   "Are you sure?",
                                                   MessageBox.Icon.QUESTION,
                                                   "Do you really want to delete " + fileName + "?<br/>This cannot be undone.",
                                                   new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                                   new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
                    mb.addStyleName(Runo.WINDOW_DIALOG);
                    mb.show(new EventListener() {
                        public void buttonClicked(ButtonType buttonType) {
                            if (buttonType == MessageBox.ButtonType.YES) {
                                String source = mibsTree.getParent(fileName).toString();
                                File file = new File(PENDING.equals(source) ? MIBS_PENDING_DIR : MIBS_COMPILED_DIR, fileName);
                                if (file.delete()) {
                                    mibsContainer.removeItem(fileName);
                                    logger.info("MIB " + file + " has been successfully removed.");
                                } else {
                                    getApplication().getMainWindow().showNotification("Can't delete " + file);
                                }
                            }
                        }
                    });
                }
                if (action == ACTION_EDIT) {
                    Window w = new FileEditorWindow(new File(MIBS_PENDING_DIR, fileName), logger, false);
                    getApplication().getMainWindow().addWindow(w);
                }
                if (action == ACTION_VIEW) {
                    Window w = new FileEditorWindow(new File(MIBS_COMPILED_DIR, fileName), logger, true);
                    getApplication().getMainWindow().addWindow(w);
                }
                if (action == ACTION_COMPILE) {
                    if (parseMib(logger, new File(MIBS_PENDING_DIR, fileName))) {
                        mibsTree.removeItem(target);
                        addTreeItem(fileName, COMPILED);
                        File file = new File(MIBS_PENDING_DIR, fileName);
                        file.renameTo(new File(MIBS_COMPILED_DIR, file.getName()));
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
     * Adds the tree item.
     *
     * @param label the label
     * @param parent the parent
     */
    private void addTreeItem(final String label, final String parent) {
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
                logger.error("Problem found when compiling the MIB: <pre>" + mibParser.getFormattedErrors() + "</pre>");
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
            getApplication().getMainWindow().addWindow(w);
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
            getApplication().getMainWindow().showNotification("The MIB couldn't be processed for events because: " + mibParser.getFormattedErrors(), Notification.TYPE_ERROR_MESSAGE);                
        } else {
            if (events.getEventCount() > 0) {
                try {
                    logger.info("Found " + events.getEventCount() + " events.");
                    final String eventsFileName = fileName.replaceFirst("\\..*$", ".events.xml");
                    final EventWindow w = new EventWindow(eventsDao, eventsProxy, eventsFileName, events, logger);
                    getApplication().getMainWindow().addWindow(w);
                } catch (Throwable t) {
                    getApplication().getMainWindow().showNotification(t.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                }
            } else {
                getApplication().getMainWindow().showNotification("The MIB doesn't contain any notification/trap", Notification.TYPE_WARNING_MESSAGE);
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
                getApplication().getMainWindow().showNotification("The MIB couldn't be processed for data collection because: " + mibParser.getFormattedErrors(), Notification.TYPE_ERROR_MESSAGE);
            } else {
                if (dcGroup.getGroupCount() > 0) {
                    try {
                        final String dataFileName = fileName.replaceFirst("\\..*$", ".xml");
                        final DataCollectionWindow w = new DataCollectionWindow(mibParser, dataCollectionDao, dataFileName, dcGroup, logger);
                        getApplication().getMainWindow().addWindow(w);
                    } catch (Throwable t) {
                        getApplication().getMainWindow().showNotification(t.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                    }
                } else {
                    getApplication().getMainWindow().showNotification("The MIB doesn't contain any metric for data collection.", Notification.TYPE_WARNING_MESSAGE);
                }
            }
        }
    }

}
