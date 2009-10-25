package org.opennms.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * GWT entry point classes define {@link #onModuleLoad()}.</code>.
 */
public class Application implements EntryPoint {
    // TODO: Figure out if there are any issues with having these components being
    // global inside the class.
    private final ContentPanel verifyOwnership = new ContentPanel();
    private final ContentPanel connectToDatabase = new ContentPanel();
    private final TextField<String> dbHost = new TextField<String>();
    private final NumberField dbPort = new NumberField();
    private final TextField<String> dbName = new TextField<String>();
    private final TextField<String> dbUser = new TextField<String>();
    private final TextField<String> dbPass = new TextField<String>();
    private final TextField<String> dbConfirm = new TextField<String>();
    private final TextField<String> dbDriver = new TextField<String>();
    private final TextField<String> dbAdminUrl = new TextField<String>();
    private final TextField<String> dbUrl = new TextField<String>();
    // private final TextField<String> dbBinDir = new TextField<String>();
    private final ContentPanel setAdminPassword = new ContentPanel();
    private final TextField<String> passwd = new TextField<String>();
    private final TextField<String> confirm = new TextField<String>();
    private final ContentPanel updateDatabase = new ContentPanel();
    private final ContentPanel checkStoredProcedures = new ContentPanel();
    private final Button continueButton = new Button();

    ListLoader<ListLoadResult<LoggingEvent>> m_logLoader = null;
    Button m_closeLogWindowButton = null;

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
        private final boolean m_alertOnThrow;
        public AdminPasswordCheck(InstallationCheck nextInChain, boolean alertOnThrow) {
            m_next = nextInChain;
            m_alertOnThrow = alertOnThrow;
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
                    if (m_alertOnThrow) {
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    }
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

    private DatabaseConnectionSettings m_databaseConnectionSettings = null;

    private class GetDatabaseConnectionSettingsCheck implements InstallationCheck {
        private final InstallationCheck m_next;
        public GetDatabaseConnectionSettingsCheck(InstallationCheck nextInChain) {
            m_next = nextInChain;
        }

        public void check() {
            // Create the RemoteServiceServlet that acts as the controller for this GWT view
            // final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

            installService.getDatabaseConnectionSettings(new AsyncCallback<DatabaseConnectionSettings>() {
                public void onSuccess(DatabaseConnectionSettings result) {
                    m_databaseConnectionSettings = result;
                    if (m_databaseConnectionSettings.getAdminPassword() != null) dbPass.setValue(m_databaseConnectionSettings.getAdminPassword());
                    if (m_databaseConnectionSettings.getAdminUrl() != null) dbAdminUrl.setValue(m_databaseConnectionSettings.getAdminUrl());
                    if (m_databaseConnectionSettings.getAdminUser() != null) dbUser.setValue(m_databaseConnectionSettings.getAdminUser());
                    if (m_databaseConnectionSettings.getDbName() != null) dbName.setValue(m_databaseConnectionSettings.getDbName());
                    // TODO: Probably should always use the hard-coded database driver value
                    // if (m_databaseConnectionSettings.getDriver() != null) dbDriver.setValue(m_databaseConnectionSettings.getDriver());
                    if (m_databaseConnectionSettings.getUrl() != null) dbUrl.setValue(m_databaseConnectionSettings.getUrl());

                    if (m_next != null) {
                        m_next.check();
                    }
                }

                public void onFailure(Throwable e) {
                    // TODO: Figure out what to do here, if anything
                }
            });
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

            final DatabaseConnectionCheck thisCheck = this;

            // Validation
            if (!dbHost.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Database Host", "The database host cannot be left blank. Please type in an IP address or hostname.", null);
                return;
            } else if (!dbPort.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Database Port", "The database port value cannot be left blank.", null);
                return;
            } else if (!dbName.validate()) {
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

            // TODO: Should we hard-code the value of the admin database?
            dbAdminUrl.setValue("jdbc:postgresql://" + dbHost.getValue() + ":" + String.valueOf(dbPort.getValue().intValue()) + "/template1");
            dbUrl.setValue("jdbc:postgresql://" + dbHost.getValue() + ":" + String.valueOf(dbPort.getValue().intValue()) + "/" + dbName.getValue());

            // Make sure that the password and confirmation fields match
            if (dbPass.getValue() == null || !dbPass.getValue().equals(dbConfirm.getValue())) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Password Entries Do Not Match", "The password and confirmation fields do not match. Please enter the new password in both fields again.", null);
                return;
            }

            // installService.connectToDatabase(dbName.getValue(), dbUser.getValue(), dbPass.getValue(), dbDriver.getValue(), dbUrl.getValue(), dbBinDir.getValue(), new AsyncCallback<Boolean>() {
            installService.connectToDatabase(dbName.getValue(), dbUser.getValue(), dbPass.getValue(), dbDriver.getValue(), dbAdminUrl.getValue(), dbUrl.getValue(), new AsyncCallback<Void>() {
                public void onSuccess(Void result) {
                    connectToDatabase.setIconStyle("check-success-icon");
                    if (m_next != null) {
                        m_next.check();
                    }
                }

                public void onFailure(Throwable e) {
                    connectToDatabase.setIconStyle("check-failure-icon");

                    if (e instanceof DatabaseDoesNotExistException) {
                        MessageBox.confirm("Database Does Not Exist", "The database does not exist yet. Would you like to create it?", new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent event) {
                                if (Dialog.YES.equals((event.getButtonClicked().getItemId()))) {
                                    connectToDatabase.setIconStyle("check-progress-icon");
                                    installService.createDatabase(dbName.getValue(), dbUser.getValue(), dbPass.getValue(), dbDriver.getValue(), dbAdminUrl.getValue(), new AsyncCallback<Void>() {
                                        public void onSuccess(Void result) {
                                            // Re-run the check now that the database has been created
                                            thisCheck.check();
                                        }

                                        public void onFailure(Throwable e) {
                                            connectToDatabase.setIconStyle("check-failure-icon");
                                            MessageBox.alert("Failure", "The database could not be created: " + e.getMessage(), null);
                                            connectToDatabase.expand();
                                        }
                                    });
                                } else {
                                    // Don't do anything, just fall back to the failed panel
                                    connectToDatabase.expand();
                                }
                            }
                        });
                    } else {
                        MessageBox.alert("Failure", "Could not connect to the database with the specified parameters: " + e.getMessage(), null);
                        connectToDatabase.expand();
                    }
                }
            });
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

    private abstract class AsyncCallbackWithReference<R,T> implements AsyncCallback<T> {
        protected final R m_reference;
        public AsyncCallbackWithReference(R o) {
            m_reference = o;
        }
    }

    /**
     * TODO: Make sure that this UI can handle a large amount of log messages properly.
     * TODO: Update look and feel to hide category and maybe color-code the messages
     */
    private Window spawnLogMessageWindow() {
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
        m_logLoader = new BaseListLoader<ListLoadResult<LoggingEvent>>(proxy, new BeanModelReader());
        ListStore<BeanModel> store = new ListStore<BeanModel>(m_logLoader);

        // Arguments to this are passed as loadConfig to the RpcProxy
        m_logLoader.load();

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

        m_closeLogWindowButton = new Button("Close Window", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
                w.removeAll();
            }
        });
        m_closeLogWindowButton.disable();
        w.addButton(m_closeLogWindowButton);

        w.show();
        return w;
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
                verifyOwnershipCaption.setHtml("<p>To prove your ownership of this OpenNMS installation, please create a file named <code>" + result + "</code> in the OpenNMS home directory.</p>");
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
                        MessageBox.alert("Success", "The ownership file exists. You have permission to update the admin password and database settings.", new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent event) {
                                setAdminPassword.expand();
                            }
                        });
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
        // passwd.setMinLength(6);
        passwd.setPassword(true);
        setAdminPassword.add(passwd);

        // final TextField<String> confirm = new TextField<String>();
        confirm.setFieldLabel("Confirm password");
        confirm.setAllowBlank(false);
        // confirm.setMinLength(6);
        confirm.setPassword(true);
        setAdminPassword.add(confirm);

        setAdminPassword.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                // Check to see if the admin password is already set and if so, update the icon
                new AdminPasswordCheck(null, false).check();
            }
        });

        Button updatePasswordButton = new Button("Update password", new SelectionListener<ButtonEvent>() {
            // Check the ownership file before allowing updates to the admin password
            // TODO: We should probably perform that check server-side
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new SetAdminPasswordCheck(new InstallationCheck() {
                        public void check() {
                            MessageBox.alert("Password Updated", "The administrator password has been updated.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                    connectToDatabase.expand();
                                }
                            });
                        }
                    })
                ).check();
            }
        });
        setAdminPassword.add(updatePasswordButton);

        // final ContentPanel connectToDatabase = new ContentPanel();
        connectToDatabase.setHeading("Verify database connection");
        connectToDatabase.setIconStyle("check-failure-icon");
        connectToDatabase.setBodyStyleName("accordion-panel");
        connectToDatabase.setScrollMode(Style.Scroll.AUTOY);

        FormLayout connectToDatabaseLayout = new FormLayout();
        connectToDatabaseLayout.setLabelPad(10);
        connectToDatabaseLayout.setLabelWidth(150);
        // Normally 150, but subtract 15 for the vertical scrollbar
        // Made the panel bigger, don't need a scrollbar any more
        connectToDatabaseLayout.setDefaultWidth(220);
        connectToDatabase.setLayout(connectToDatabaseLayout);

        // final TextField<String> dbName = new TextField<String>();
        dbHost.setFieldLabel("Hostname/IP address");
        dbHost.setAllowBlank(false);
        dbHost.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                if ("127.0.0.1".equals(dbHost.getValue()) || "localhost".equals(dbHost.getValue())) {
                    dbHost.removeStyleName("font-style-normal");
                    dbHost.addStyleName("font-style-italic");
                } else { 
                    dbHost.removeStyleName("font-style-italic");
                    dbHost.addStyleName("font-style-normal");
                }
            }
        });
        connectToDatabase.add(dbHost);

        // final TextField<String> dbName = new TextField<String>();
        dbPort.setFieldLabel("Port");
        dbPort.setAllowBlank(false);
        dbPort.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                if (dbPort.getValue().intValue() == 5432) {
                    dbPort.removeStyleName("font-style-normal");
                    dbPort.addStyleName("font-style-italic");
                } else { 
                    dbPort.removeStyleName("font-style-italic");
                    dbPort.addStyleName("font-style-normal");
                }
            }
        });
        connectToDatabase.add(dbPort);

        // final TextField<String> dbName = new TextField<String>();
        dbName.setFieldLabel("Database name");
        dbName.setAllowBlank(false);
        dbName.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                if ("opennms".equals(dbName.getValue())) {
                    dbName.removeStyleName("font-style-normal");
                    dbName.addStyleName("font-style-italic");
                } else { 
                    dbName.removeStyleName("font-style-italic");
                    dbName.addStyleName("font-style-normal");
                }
            }
        });
        connectToDatabase.add(dbName);

        // final TextField<String> dbUser = new TextField<String>();
        dbUser.setFieldLabel("Database admin user");
        dbUser.setAllowBlank(false);
        dbUser.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                if ("postgres".equals(dbUser.getValue())) {
                    dbUser.removeStyleName("font-style-normal");
                    dbUser.addStyleName("font-style-italic");
                } else { 
                    dbUser.removeStyleName("font-style-italic");
                    dbUser.addStyleName("font-style-normal");
                }
            }
        });
        connectToDatabase.add(dbUser);

        // final TextField<String> dbPass = new TextField<String>();
        dbPass.setFieldLabel("Database admin password");
        // dbPass.setAllowBlank(false);
        dbPass.setPassword(true);
        connectToDatabase.add(dbPass);

        // final TextField<String> dbConfirm = new TextField<String>();
        dbConfirm.setFieldLabel("Confirm admin password");
        // dbConfirm.setAllowBlank(false);
        dbConfirm.setPassword(true);
        connectToDatabase.add(dbConfirm);

        // final TextField<String> dbDriver = new TextField<String>();
        dbDriver.setFieldLabel("Database driver");
        dbDriver.setAllowBlank(false);
        dbDriver.setValue("org.postgresql.Driver");
        dbDriver.hide();

        // final TextField<String> dbAdminUrl = new TextField<String>();
        dbAdminUrl.setFieldLabel("Database admin URL");
        dbAdminUrl.setAllowBlank(false);
        dbAdminUrl.hide();

        // final TextField<String> dbUrl = new TextField<String>();
        dbUrl.setFieldLabel("Database URL");
        dbUrl.setAllowBlank(false);
        dbUrl.hide();

        /*
        // final TextField<String> dbBinDir = new TextField<String>();
        dbBinDir.setFieldLabel("Database binary directory");
        dbBinDir.setAllowBlank(false);
        connectToDatabase.add(dbBinDir);
         */

        connectToDatabase.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                // Fetch the current database settings (if any)
                new GetDatabaseConnectionSettingsCheck(null).check();
            }
        });

        Button connectButton = new Button("Connect to database", new SelectionListener<ButtonEvent>() {
            // Check the ownership file before allowing updates to the database configuration
            // TODO: We should probably perform that check server-side
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new DatabaseConnectionCheck(new InstallationCheck() {
                        public void check() {
                            MessageBox.alert("Success", "The connection to the database with the specified parameters was successful.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                    updateDatabase.expand();
                                }
                            });
                        }
                    })
                ).check();
            }
        });
        connectToDatabase.add(connectButton);

        // Add hidden panels under the button so they don't mess up the layout
        connectToDatabase.add(dbDriver);
        connectToDatabase.add(dbAdminUrl);
        connectToDatabase.add(dbUrl);

        // final ContentPanel updateDatabase = new ContentPanel();
        updateDatabase.setHeading("Initialize database");
        updateDatabase.setIconStyle("check-failure-icon");
        updateDatabase.setBodyStyleName("accordion-panel");

        Button updateButton = new Button("Update database", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                // Start a spinner that indicates operation start
                updateDatabase.setIconStyle("check-progress-icon");

                installService.clearDatabaseUpdateLogs(new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        installService.updateDatabase(new AsyncCallback<Void>() {
                            public void onSuccess(Void result) {
                                final Window logWindow = spawnLogMessageWindow();
                                logWindow.setIconStyle("check-progress-icon");

                                final Timer logTimer = new Timer() {
                                    public void run() {
                                        installService.isUpdateInProgress(new AsyncCallbackWithReference<Timer,Boolean>(this) {
                                            public void onSuccess(Boolean result) {
                                                if (result) {
                                                    // The installer is still in progress, just update the logs
                                                    m_logLoader.load();
                                                } else {
                                                    // The installer has completed! 
                                                    // Cancel the timer
                                                    m_reference.cancel();
                                                    // Load the final log entries
                                                    m_logLoader.load();

                                                    // Check to see if the installer operation succeeded
                                                    installService.didLastUpdateSucceed(new AsyncCallback<Boolean>() {
                                                        public void onSuccess(Boolean result) {
                                                            if (result) {
                                                                // Change the panel icon to a success icon
                                                                logWindow.setIconStyle("check-success-icon");
                                                                updateDatabase.setIconStyle("check-success-icon");
                                                            } else {
                                                                logWindow.setIconStyle("check-failure-icon");
                                                                updateDatabase.setIconStyle("check-failure-icon");
                                                            }
                                                        }

                                                        public void onFailure(Throwable e) {
                                                            logWindow.setIconStyle("check-failure-icon");
                                                            updateDatabase.setIconStyle("check-failure-icon");
                                                            // TODO: Figure out better error handling for GWT-level failures
                                                            MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                                                        }
                                                    });

                                                    // Enable the window close button
                                                    m_closeLogWindowButton.enable();
                                                }
                                            }

                                            public void onFailure(Throwable e) {
                                                updateDatabase.setIconStyle("check-failure-icon");
                                                // TODO: Figure out better error handling for GWT-level failures
                                                MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                                            }
                                        });
                                    }
                                };
                                logTimer.scheduleRepeating(2000);

                                /*
                                updateDatabase.setIconStyle("check-success-icon");
                                MessageBox.alert("Update Started", "The database update has been started.", null);
                                 */
                            }

                            public void onFailure(Throwable e) {
                                updateDatabase.setIconStyle("check-failure-icon");
                                // TODO: Figure out better error handling for GWT-level failures
                                MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
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
                                MessageBox.alert("Success", "The <code>iplike</code> stored procedure is installed properly.", new Listener<MessageBoxEvent>() {
                                    public void handleEvent(MessageBoxEvent event) {
                                        continueButton.focus();
                                    }
                                });
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

        continueButton.setText("Continue &#0187;");
        continueButton.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
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
                        ),
                        true
                    )
                ).check();
            }
        });
        // continueButton.disable();
        gxtPanel.addButton(continueButton);
        // vertical.add(continueButton);

        gxtWrapper.add(gxtPanel);
        vertical.add(gxtWrapper);

        //dock.add(new HTML(), DockPanel.WEST);
        dock.add(vertical, DockPanel.CENTER);
        //dock.add(new HTML(), DockPanel.EAST);

        RootPanel.get().add(dock);
    }
}
