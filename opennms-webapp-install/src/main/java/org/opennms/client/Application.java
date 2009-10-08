package org.opennms.client;

import java.util.*;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTML;
// import com.google.gwt.user.client.Window;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.button.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.*; 

import org.apache.log4j.Logger;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint {
    // TODO: Figure out if there are any issues with having these components being
    // global inside the class.
    private final ContentPanel verifyOwnership = new ContentPanel();
    private final ContentPanel connectToDatabase = new ContentPanel();
    private final TextField<String> dbName = new TextField<String>();
    private final TextField<String> dbUser = new TextField<String>();
    private final TextField<String> dbPass = new TextField<String>();
    private final TextField<String> dbConfirm = new TextField<String>();
    private final TextField<String> dbDriver = new TextField<String>();
    private final TextField<String> dbUrl = new TextField<String>();
    private final TextField<String> dbBinDir = new TextField<String>();
    private final ContentPanel setAdminPassword = new ContentPanel();
    private final TextField<String> passwd = new TextField<String>();
    private final TextField<String> confirm = new TextField<String>();
    private final ContentPanel checkStoredProcedures = new ContentPanel();

    // Create the RemoteServiceServlet that acts as the controller for this GWT view
    // TODO: Make sure that it is OK to have a global instance of this service
    final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

    public interface InstallationCheck {
        public void check();
    }

    private class OwnershipFileCheck implements InstallationCheck {
        private final InstallationCheck m_next;
        public OwnershipFileCheck(InstallationCheck nextInChain) {
            m_next = nextInChain;
        }

        public void check() {
            // Start a spinner that indicates operation start
            verifyOwnership.setIconStyle("check-progress-icon");

            // Create the RemoteServiceServlet that acts as the controller for this GWT view
            // final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

            installService.checkOwnershipFileExists(new AsyncCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    if (result) {
                        verifyOwnership.setIconStyle("check-success-icon");
                        if (m_next != null) {
                            m_next.check();
                        }
                    } else {
                        verifyOwnership.setIconStyle("check-failure-icon");
                        MessageBox.alert("Failure", "The ownership file does not exist. Please create the ownership file in the OpenNMS home directory to prove ownership of this installation.", new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent event) {
                            }
                        });
                        verifyOwnership.expand();
                    }
                }

                public void onFailure(Throwable e) {
                    verifyOwnership.setIconStyle("check-failure-icon");
                    // TODO: Figure out better error handling for GWT-level failures
                    MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    verifyOwnership.expand();
                }
            });
        }
    }

    private class AdminPasswordCheck implements InstallationCheck {
        private final InstallationCheck m_next;
        public AdminPasswordCheck(InstallationCheck nextInChain) {
            m_next = nextInChain;
        }

        public void check() {
            // Create the RemoteServiceServlet that acts as the controller for this GWT view
            // final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

            installService.isAdminPasswordSet(new AsyncCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    if (result) {
                        setAdminPassword.setIconStyle("check-success-icon");
                        if (m_next != null) {
                            m_next.check();
                        }
                    } else {
                        setAdminPassword.setIconStyle("check-failure-icon");
                        // MessageBox.alert("Failure", "The administrator password is not set yet.", null);
                        setAdminPassword.expand();
                    }
                }

                public void onFailure(Throwable e) {
                    setAdminPassword.setIconStyle("check-failure-icon");
                    // TODO: Figure out better error handling for GWT-level failures
                    MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    setAdminPassword.expand();
                }
            });
        }
    }

    private class SetAdminPasswordCheck implements InstallationCheck {
        private final InstallationCheck m_next;
        public SetAdminPasswordCheck(InstallationCheck nextInChain) {
            m_next = nextInChain;
        }

        public void check() {
            // Start a spinner that indicates operation start
            setAdminPassword.setIconStyle("check-progress-icon");

            // Create the RemoteServiceServlet that acts as the controller for this GWT view
            // final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

            if (!passwd.validate()) {
                setAdminPassword.setIconStyle("check-failure-icon");
                MessageBox.alert("Password Validation Failed", "Blank passwords are not allowed. Please enter a new password.", null);
                return;
            }
            if (passwd.getValue() != null && passwd.getValue().equals(confirm.getValue())) {
                installService.setAdminPassword(passwd.getValue(), new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        setAdminPassword.setIconStyle("check-success-icon");
                        if (m_next != null) {
                            m_next.check();
                        }
                    }

                    public void onFailure(Throwable e) {
                        setAdminPassword.setIconStyle("check-failure-icon");
                        // TODO: Figure out better error handling for GWT-level failures
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                        setAdminPassword.expand();
                    }
                });
            } else {
                setAdminPassword.setIconStyle("check-failure-icon");
                MessageBox.alert("Password Entries Do Not Match", "The password and confirmation fields do not match. Please enter the new password in both fields again.", null);
                setAdminPassword.expand();
            }

        }
    }

    private class DatabaseConnectionCheck implements InstallationCheck {
        private final InstallationCheck m_next;
        public DatabaseConnectionCheck(InstallationCheck nextInChain) {
            m_next = nextInChain;
        }

        public void check() {
            // Start a spinner that indicates operation start
            connectToDatabase.setIconStyle("check-progress-icon");

            // Create the RemoteServiceServlet that acts as the controller for this GWT view
            // final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

            try {
                // Validation

                if (!dbName.validate()) {
                    connectToDatabase.setIconStyle("check-failure-icon");
                    MessageBox.alert("Invalid Database Name", "The database name cannot be left blank.", null);
                    return;
                } else if (!dbUser.validate()) {
                    connectToDatabase.setIconStyle("check-failure-icon");
                    MessageBox.alert("Invalid Database User", "The database username cannot be left blank.", null);
                    return;
                } else if (!dbPass.validate()) {
                    connectToDatabase.setIconStyle("check-failure-icon");
                    MessageBox.alert("Invalid Database Password", "The database password cannot be left blank.", null);
                    return;
                } else if (!dbDriver.validate()) {
                    connectToDatabase.setIconStyle("check-failure-icon");
                    MessageBox.alert("Invalid Database Driver", "Please choose a database driver from the list.", null);
                    return;
                }

                dbUrl.setValue("jdbc:postgresql://localhost:5432/" + dbName.getValue());

                // Make sure that the password and confirmation fields match
                if (dbPass.getValue() == null || !dbPass.getValue().equals(dbConfirm.getValue())) {
                    connectToDatabase.setIconStyle("check-failure-icon");
                    MessageBox.alert("Password Entries Do Not Match", "The password and confirmation fields do not match. Please enter the new password in both fields again.", null);
                    return;
                }

                installService.connectToDatabase(dbName.getValue(), dbUser.getValue(), dbPass.getValue(), dbDriver.getValue(), dbUrl.getValue(), dbBinDir.getValue(), new AsyncCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        if (result) {
                            connectToDatabase.setIconStyle("check-success-icon");
                            if (m_next != null) {
                                m_next.check();
                            }
                        } else {
                            connectToDatabase.setIconStyle("check-failure-icon");
                            MessageBox.alert("Failure", "Could not connect to the database with the specified parameters.", null);
                            connectToDatabase.expand();
                        }
                    }

                    public void onFailure(Throwable e) {
                        connectToDatabase.setIconStyle("check-failure-icon");
                        // TODO: Figure out better error handling for GWT-level failures
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                        connectToDatabase.expand();
                    }
                });
            } catch (IllegalStateException e) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("PostgreSQL JDBC Driver Missing", "The PostgreSQL JDBC driver could not be found in the classpath.", null);
                connectToDatabase.expand();
            }
        }
    }

    private class StoredProceduresCheck implements InstallationCheck {
        private final InstallationCheck m_next;
        public StoredProceduresCheck(InstallationCheck nextInChain) {
            m_next = nextInChain;
        }

        public void check() {
            // Start a spinner that indicates operation start
            checkStoredProcedures.setIconStyle("check-progress-icon");

            // Create the RemoteServiceServlet that acts as the controller for this GWT view
            // final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

            try {
                installService.checkIpLike(new AsyncCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        if (result) {
                            checkStoredProcedures.setIconStyle("check-success-icon");
                            if (m_next != null) {
                                m_next.check();
                            }
                        } else {
                            checkStoredProcedures.setIconStyle("check-failure-icon");
                            MessageBox.alert("Failure", "Could not find the <code>iplike</code> stored procedure in the database.", null);
                            checkStoredProcedures.expand();
                        }
                    }

                    public void onFailure(Throwable e) {
                        checkStoredProcedures.setIconStyle("check-failure-icon");
                        // TODO: Figure out better error handling for GWT-level failures
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                        checkStoredProcedures.expand();
                    }
                });
            } catch (IllegalStateException e) {
                MessageBox.alert("Failure", e.getMessage(), null);
                // Since this is always database-related, send them back to the database settings panel
                connectToDatabase.expand();
            }

        }
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        // Create the RemoteServiceServlet that acts as the controller for this GWT view
        // final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

        // Create a Dock Panel
        DockPanel dock = new DockPanel();
        // dock.setStyleName("cw-DockPanel");
        // dock.setSpacing(4);
        dock.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
        dock.setWidth("100%");

        VerticalPanel vertical = new VerticalPanel();
        vertical.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

        HTML contents = new HTML("<img src=\"../images/logo.gif\" alt=\"openNMS Installer\"/>");
        vertical.add(contents);

        /*
        DisclosurePanel verifyPanel = new DisclosurePanel("Verify ownership");
        verifyPanel.setAnimationEnabled(true);
        verifyPanel.add(new Button("Check"));
        vertical.add(verifyPanel);
        // vertical.add(new DisclosurePanel("Verify ownership"));

        vertical.add(new DisclosurePanel("Set admin password"));

        vertical.add(new DisclosurePanel("Connect to database"));

        vertical.add(new DisclosurePanel("Check stored procedures"));

        vertical.add(new DisclosurePanel("Additional tips"));

        vertical.add(new Button("Continue &#0187;"));
         */

        VerticalPanel gxtWrapper = new VerticalPanel();
        gxtWrapper.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);

        final ContentPanel gxtPanel = new ContentPanel();
        AccordionLayout gxtPanelLayout = new AccordionLayout();
        // gxtPanelLayout.setAutoWidth(true);
        gxtPanel.setLayout(gxtPanelLayout);
        gxtPanel.setHeaderVisible(false);
        gxtPanel.setBodyBorder(false);
        gxtPanel.setSize(400, 400);
        gxtPanel.setBodyStyleName("transparent-background");

        // final ContentPanel verifyOwnership = new ContentPanel();
        verifyOwnership.setHeading("Verify ownership");
        verifyOwnership.setIconStyle("check-failure-icon");
        verifyOwnership.setBodyStyleName("accordion-panel");
        // Create an empty caption for the panel that will be filled by an RPC call
        final Html verifyOwnershipCaption = verifyOwnership.addText("");

        // Add a caption that shows the user the ownership filename.
        installService.getOwnershipFilename(new AsyncCallback<String>() {
            public void onSuccess(String result) {
                verifyOwnershipCaption.setHtml("<p>To prove ownership of this appliance, please create a file named <code>" + result + "</code> in the OpenNMS home directory.</p>");
            }
            public void onFailure(Throwable e) {
                verifyOwnership.setIconStyle("check-failure-icon");
                // TODO: Figure out better error handling for GWT-level failures
                MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
            }
        });

        Button checkOwnershipButton = new Button("Check ownership file", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(new InstallationCheck() {
                    public void check() {
                        MessageBox.alert("Success", "The ownership file exists. You have permission to update the admin password and database settings.", null);
                    }
                }).check();
            }
        });
        verifyOwnership.add(checkOwnershipButton);

        // final ContentPanel setAdminPassword = new ContentPanel();
        setAdminPassword.setHeading("Set administrator password");
        setAdminPassword.setIconStyle("check-failure-icon");
        setAdminPassword.setBodyStyleName("accordion-panel");

        FormLayout setAdminPasswordFormLayout = new FormLayout();
        setAdminPasswordFormLayout.setLabelPad(10);
        setAdminPasswordFormLayout.setLabelWidth(120);
        setAdminPasswordFormLayout.setDefaultWidth(250);
        setAdminPassword.setLayout(setAdminPasswordFormLayout);

        // final TextField<String> passwd = new TextField<String>();
        passwd.setFieldLabel("New admin password");
        passwd.setAllowBlank(false);
        passwd.setMinLength(6);
        passwd.setPassword(true);
        setAdminPassword.add(passwd);

        // final TextField<String> confirm = new TextField<String>();
        confirm.setFieldLabel("Confirm password");
        confirm.setAllowBlank(false);
        // confirm.setMinLength(6);
        confirm.setPassword(true);
        setAdminPassword.add(confirm);

        setAdminPassword.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent e) {
                // Check to see if the admin password is already set and if so, update the icon
                new AdminPasswordCheck(null).check();
            }
        });

        Button updatePasswordButton = new Button("Update password", new SelectionListener<ButtonEvent>() {
            // Check the ownership file before allowing updates to the admin password
            // TODO: We should probably perform that check server-side
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new SetAdminPasswordCheck(new InstallationCheck() {
                        public void check() {
                            MessageBox.alert("Password Updated", "The administrator password has been updated.", null);
                        }
                    })
                ).check();
            }
        });
        setAdminPassword.add(updatePasswordButton);

        // final ContentPanel connectToDatabase = new ContentPanel();
        connectToDatabase.setHeading("Connect to database");
        connectToDatabase.setIconStyle("check-failure-icon");
        connectToDatabase.setBodyStyleName("accordion-panel");
        connectToDatabase.setScrollMode(Style.Scroll.AUTOY);

        FormLayout connectToDatabaseLayout = new FormLayout();
        connectToDatabaseLayout.setLabelPad(10);
        connectToDatabaseLayout.setLabelWidth(120);
        // Normally 150, but subtract 15 for the vertical scrollbar
        // Made the panel bigger, don't need a scrollbar any more
        connectToDatabaseLayout.setDefaultWidth(250);
        connectToDatabase.setLayout(connectToDatabaseLayout);

        // final TextField<String> dbName = new TextField<String>();
        dbName.setFieldLabel("Database name");
        dbName.setAllowBlank(false);
        connectToDatabase.add(dbName);

        // final TextField<String> dbUser = new TextField<String>();
        dbUser.setFieldLabel("Database user");
        dbUser.setAllowBlank(false);
        connectToDatabase.add(dbUser);

        // final TextField<String> dbPass = new TextField<String>();
        dbPass.setFieldLabel("Database password");
        dbPass.setAllowBlank(false);
        dbPass.setPassword(true);
        connectToDatabase.add(dbPass);

        // final TextField<String> dbConfirm = new TextField<String>();
        dbConfirm.setFieldLabel("Confirm password");
        dbConfirm.setAllowBlank(false);
        dbConfirm.setPassword(true);
        connectToDatabase.add(dbConfirm);

        // final TextField<String> dbDriver = new TextField<String>();
        dbDriver.setFieldLabel("Database driver");
        dbDriver.setAllowBlank(false);
        dbDriver.setValue("org.postgresql.Driver");
        dbDriver.hide();

        // final TextField<String> dbUrl = new TextField<String>();
        dbUrl.setFieldLabel("Database URL");
        dbUrl.setAllowBlank(false);
        dbUrl.hide();

        // final TextField<String> dbBinDir = new TextField<String>();
        dbBinDir.setFieldLabel("Database binary directory");
        dbBinDir.setAllowBlank(false);
        dbBinDir.setPassword(true);
        connectToDatabase.add(dbBinDir);

        Button connectButton = new Button("Connect to database", new SelectionListener<ButtonEvent>() {
            // Check the ownership file before allowing updates to the database configuration
            // TODO: We should probably perform that check server-side
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new DatabaseConnectionCheck(new InstallationCheck() {
                        public void check() {
                            MessageBox.alert("Success", "The connection to the database with the specified parameters was successful.", null);
                        }
                    })
                ).check();
            }
        });
        connectToDatabase.add(connectButton);

        // Add hidden panels under the button so they don't mess up the layout
        connectToDatabase.add(dbDriver);
        connectToDatabase.add(dbUrl);

        final ContentPanel updateDatabase = new ContentPanel();
        updateDatabase.setHeading("Update database");
        updateDatabase.setIconStyle("check-failure-icon");
        updateDatabase.setBodyStyleName("accordion-panel");

        Button updateButton = new Button("Update database", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                // Start a spinner that indicates operation start
                updateDatabase.setIconStyle("check-progress-icon");

                installService.updateDatabase(new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        // TODO: Spawn the log message window
                        // TODO: Start an interval that will read the log messages from the installer
                        // TODO: Start an interval that will monitor the installer thread
                        updateDatabase.setIconStyle("check-success-icon");
                        MessageBox.alert("Update Started", "The database update has been started.", new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent event) {
                            }
                        });
                    }

                    public void onFailure(Throwable e) {
                        updateDatabase.setIconStyle("check-failure-icon");
                        // TODO: Figure out better error handling for GWT-level failures
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    }
                });
            }
        });
        updateDatabase.add(updateButton);

        // final ContentPanel checkStoredProcedures = new ContentPanel();
        checkStoredProcedures.setHeading("Check stored procedures");
        checkStoredProcedures.setIconStyle("check-failure-icon");
        checkStoredProcedures.setBodyStyleName("accordion-panel");
        Button checkStoredProceduresButton = new Button("Check stored procedures", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new DatabaseConnectionCheck(
                        new StoredProceduresCheck(new InstallationCheck() {
                            public void check() {
                                MessageBox.alert("Success", "The <code>iplike</code> stored procedure is installed properly.", null);
                            }
                        })
                    )
                ).check();
            }
        });
        checkStoredProcedures.add(checkStoredProceduresButton);

        final ContentPanel tips = new ContentPanel();
        tips.setHeading("Additional tips");
        tips.setBodyStyleName("accordion-panel");
        tips.addText("Add additional tips here.");

        gxtPanel.add(verifyOwnership);
        gxtPanel.add(setAdminPassword);
        gxtPanel.add(connectToDatabase);
        gxtPanel.add(updateDatabase);
        gxtPanel.add(checkStoredProcedures);
        gxtPanel.add(tips);

        Button continueButton = new Button("Continue &#0187;", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                // Go through all of the installation checks before forwarding the user to
                // the main OpenNMS web UI.
                //
                // TODO: Tie this to a Jetty server command to deploy/restart OpenNMS and the main webapp
                new OwnershipFileCheck(
                    new AdminPasswordCheck(
                        new DatabaseConnectionCheck(
                            new StoredProceduresCheck(new InstallationCheck() {
                                public void check() {
                                    // TODO: Figure out best way to redirect, make it equivalent to clicking a link
                                    com.google.gwt.user.client.Window.Location.replace("/opennms");
                                }
                            })
                        )
                    )
                ).check();
            }
        });
        // continueButton.disable();
        gxtPanel.addButton(continueButton);
        // vertical.add(continueButton);

        // TODO: Link log message display to database update button action
        Button logButton = new Button("Show Log Messages", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {

                final Window w = new Window();
                w.setHeading("Database Update Logs");
                w.setModal(true);
                w.setSize(600, 400);
                // w.setMaximizable(true);

                // Use RpcProxy to fetch the list of LoggingEvent objects
                // {@link http://www.extjs.com/blog/2008/07/14/preview-java-bean-support-with-ext-gwt/ }
                // {@link http://www.extjs.com/blog/category/tutorials/ }
                RpcProxy<List<LoggingEvent>> proxy = new RpcProxy<List<LoggingEvent>>() {
                    public void load(Object loadConfig, AsyncCallback<List<LoggingEvent>> callback) {
                        installService.getDatabaseUpdateLogs(-1, callback);
                    }
                };

                // Create a loader that will load results for a Store. The BeanModelReader
                // will automatically introspect objects that implement BeanModelTag.
                ListLoader<ListLoadResult<LoggingEvent>> loader = new BaseListLoader<ListLoadResult<LoggingEvent>>(proxy, new BeanModelReader());
                ListStore<BeanModel> store = new ListStore<BeanModel>(loader);

                // Arguments to this are passed as loadConfig to the RpcProxy
                loader.load();

                // These column configs will automagically be converted into
                // bean accessor calls by the BeanModelReader. So getCategory(), 
                // getTimestamp(), ... end up being called on the list members.
                List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
                columns.add(new ColumnConfig("category", "Category", 150));
                columns.add(new ColumnConfig("level", "Severity", 50));
                columns.add(new ColumnConfig("timestamp", "Timestamp", 100));
                columns.add(new ColumnConfig("message", "Message", 300));
                ColumnModel cm = new ColumnModel(columns);

                Grid<BeanModel> grid = new Grid<BeanModel>(store, cm);
                grid.setAutoExpandColumn("message");
                // grid.setWidth(400);
                grid.setAutoHeight(true);
                grid.setBorders(true);

                w.add(grid);

                Button closeLogWindowButton = new Button("Close Window", new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        w.hide();
                        w.removeAll();
                    }
                });
                w.addButton(closeLogWindowButton);

                w.show();
            }
        });
        gxtPanel.addButton(logButton);

        gxtWrapper.add(gxtPanel);
        vertical.add(gxtWrapper);

        //dock.add(new HTML(), DockPanel.WEST);
        dock.add(vertical, DockPanel.CENTER);
        //dock.add(new HTML(), DockPanel.EAST);

        RootPanel.get().add(dock);
    }
}
