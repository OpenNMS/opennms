package org.opennms.client;

import java.util.ArrayList;
import java.util.Collection;
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
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.IconSupport;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main GWT entry point class for the installation UI. The {@link #onModuleLoad()} method
 * contains all of the UI generation code; the UI is generated completely dynamically from this
 * method call.
 *
 * @author ranger
 * @version $Id: $
 */
public class Application implements EntryPoint {
    // TODO: Figure out if there are any issues with having these components being
    // global inside the class.
    private final ContentPanel verifyOwnership = new ContentPanel();
    private final ContentPanel connectToDatabase = new ContentPanel();

    /**
     * MessageBox listener that can be used to expand the connectToDatabase panel
     * when database-related exceptions are thrown.
     */ 
    private final Listener<MessageBoxEvent> expandConnectToDatabase = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent event) {
            connectToDatabase.expand();
        }
    };

    private final TextField<String> dbHost = new TextField<String>();
    private final NumberField dbPort = new NumberField();
    private final TextField<String> dbDriver = new TextField<String>();
    private final TextField<String> dbName = new TextField<String>();
    private final TextField<String> dbAdminUser = new TextField<String>();
    private final TextField<String> dbAdminPass = new TextField<String>();
    private final TextField<String> dbAdminConfirm = new TextField<String>();
    private final TextField<String> dbAdminUrl = new TextField<String>();
    private final TextField<String> dbNmsUser = new TextField<String>();
    private final TextField<String> dbNmsPass = new TextField<String>();
    private final TextField<String> dbNmsConfirm = new TextField<String>();
    private final TextField<String> dbNmsUrl = new TextField<String>();
    // private final TextField<String> dbBinDir = new TextField<String>();

    private final ContentPanel setAdminPassword = new ContentPanel();

    /**
     * MessageBox listener that can be used to expand the setAdminPassword panel
     * when password-related exceptions are thrown.
     */ 
    private final Listener<MessageBoxEvent> expandSetAdminPassword = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent event) {
            setAdminPassword.expand();
        }
    };

    private final TextField<String> passwd = new TextField<String>();
    private final TextField<String> confirm = new TextField<String>();
    private final ContentPanel updateDatabase = new ContentPanel();
    private final Listener<MessageBoxEvent> expandUpdateDatabase = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent event) {
            updateDatabase.expand();
        }
    };
    private final ContentPanel checkStoredProcedures = new ContentPanel();

    /**
     * MessageBox listener that can be used to expand the checkStoredProcedures panel
     * when exceptions are thrown.
     */ 
    private final Listener<MessageBoxEvent> expandCheckStoredProcedures = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent event) {
            checkStoredProcedures.expand();
        }
    };

    private final Button continueButton = new Button();
    private final Listener<MessageBoxEvent> focusContinueButton = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent event) {
            continueButton.focus();
        }
    };

    // Create the RemoteServiceServlet that acts as the controller for this GWT view
    // TODO: Make sure that it is OK to have a global instance of this service
    private final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

    /**
     * Interface that defines internal checks that the UI performs when users take actions
     * in the installation UI.
     */
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

            installService.checkOwnershipFileExists(new AsyncCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    if (result) {
                        verifyOwnership.setIconStyle("check-success-icon");
                        if (m_next != null) {
                            m_next.check();
                        }
                    } else {
                        handleOwnershipNotConfirmed();
                    }
                }

                public void onFailure(Throwable e) {
                    handleUnexpectedExceptionInPanel(e, verifyOwnership);
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
                    if (m_alertOnThrow) {
                        handleUnexpectedExceptionInPanel(e, setAdminPassword);
                    } else {
                        setAdminPassword.setIconStyle("check-failure-icon");
                        setAdminPassword.expand();
                    }
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

            if (!passwd.validate()) {
                setAdminPassword.setIconStyle("check-failure-icon");
                MessageBox.alert("Password Validation Failed", "Blank passwords are not allowed. Please enter a new password.", expandSetAdminPassword);
            } else if ("admin".equals(passwd.getValue())) {
                setAdminPassword.setIconStyle("check-failure-icon");
                MessageBox.alert("Password Validation Failed", "You entered the default admin user password. Please enter a new password.", expandSetAdminPassword);
            } else if (passwd.getValue() != null && passwd.getValue().equals(confirm.getValue())) {
                installService.setAdminPassword(passwd.getValue(), new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        setAdminPassword.setIconStyle("check-success-icon");
                        if (m_next != null) {
                            m_next.check();
                        }
                    }

                    public void onFailure(Throwable e) {
                        if (e instanceof OwnershipNotConfirmedException) {
                            handleOwnershipNotConfirmed();
                        } else if (e instanceof UserConfigFileException) {
                            setAdminPassword.setIconStyle("check-failure-icon");
                            MessageBox.alert("Config File Error", "One of the user configuration files (<code>groups.xml</code>, <code>users.xml</code>) is corrupt or missing.", expandSetAdminPassword);
                        } else if (e instanceof UserUpdateException) {
                            setAdminPassword.setIconStyle("check-failure-icon");
                            MessageBox.alert("Error Storing Admin User", "The updated <code>admin</code> user could not be stored.", expandSetAdminPassword);
                        } else {
                            handleUnexpectedExceptionInPanel(e, setAdminPassword);
                        }
                    }
                });
            } else {
                setAdminPassword.setIconStyle("check-failure-icon");
                MessageBox.alert("Password Entries Do Not Match", "The password and confirmation fields do not match. Please enter the new password in both fields again.", expandSetAdminPassword);
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
            installService.getDatabaseConnectionSettings(new AsyncCallback<DatabaseConnectionSettings>() {
                public void onSuccess(DatabaseConnectionSettings result) {
                    m_databaseConnectionSettings = result;
                    if (m_databaseConnectionSettings.getAdminUrl() != null) {
                        dbAdminUrl.setValue(m_databaseConnectionSettings.getAdminUrl());
                        dbAdminUrl.fireEvent(Events.Change);
                    }
                    if (m_databaseConnectionSettings.getAdminUser() != null) {
                        dbAdminUser.setValue(m_databaseConnectionSettings.getAdminUser());
                        dbAdminUser.fireEvent(Events.Change);
                    }
                    if (m_databaseConnectionSettings.getDbName() != null) {
                        dbName.setValue(m_databaseConnectionSettings.getDbName());
                        dbName.fireEvent(Events.Change);
                    }
                    // TODO: Probably should always use the hard-coded database driver value
                    // if (m_databaseConnectionSettings.getDriver() != null) dbDriver.setValue(m_databaseConnectionSettings.getDriver());

                    if (m_databaseConnectionSettings.getNmsUser() != null) {
                        dbNmsUser.setValue(m_databaseConnectionSettings.getNmsUser());
                        dbNmsUser.fireEvent(Events.Change);
                    }
                    if (m_databaseConnectionSettings.getNmsUrl() != null) {
                        dbNmsUrl.setValue(m_databaseConnectionSettings.getNmsUrl());
                        dbNmsUrl.fireEvent(Events.Change);
                        // Extract the server name and port number from the URL
                        dbHost.setValue(getServerFromUrl(m_databaseConnectionSettings.getNmsUrl()));
                        dbHost.fireEvent(Events.Change);
                        dbPort.setValue(getPortFromUrl(m_databaseConnectionSettings.getNmsUrl()));
                        dbPort.fireEvent(Events.Change);
                    }

                    if (m_next != null) {
                        m_next.check();
                    }
                }

                public void onFailure(Throwable e) {
                    // TODO: Figure out what to do here, if anything
                    if (e instanceof OwnershipNotConfirmedException) {
                    } else if (e instanceof DatabaseConfigFileException) {
                    } else {
                    }
                }
            });
        }

        private String getServerFromUrl(String url) {
            // jdbc:postgresql://localhost:5432/opennms
            String retval = url.split("//")[1];
            retval = retval.split(":")[0];
            return retval;
        }

        private int getPortFromUrl(String url) {
            // jdbc:postgresql://localhost:5432/opennms
            String retval = url.split("//")[1];
            retval = retval.split(":")[1];
            retval = retval.split("/")[0];
            return Integer.parseInt(retval);
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

            final DatabaseConnectionCheck thisCheck = this;

            // Validation
            if (!dbHost.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Database Host", "The database host cannot be left blank. Please type in an IP address or hostname.", expandConnectToDatabase);
                return;
            } else if (!dbPort.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Database Port", "The database port value cannot be left blank.", expandConnectToDatabase);
                return;
            } else if (!dbDriver.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Database Driver", "Please choose a database driver from the list.", expandConnectToDatabase);
                return;
            } else if (!dbName.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Database Name", "The database name cannot be left blank.", expandConnectToDatabase);
                return;
            } else if (!dbAdminUser.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Admin User", "The admin username cannot be left blank.", expandConnectToDatabase);
                return;
            } else if (!dbAdminPass.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid Admin Password", "The admin password cannot be left blank.", expandConnectToDatabase);
                return;
            } else if (!dbNmsUser.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid OpenNMS User", "The OpenNMS username cannot be left blank.", expandConnectToDatabase);
                return;
            } else if (!dbNmsPass.validate()) {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Invalid OpenNMS Password", "The OpenNMS password cannot be left blank.", expandConnectToDatabase);
                return;
            }

            // TODO: Should we hard-code the value of the admin database?
            dbAdminUrl.setValue("jdbc:postgresql://" + dbHost.getValue() + ":" + String.valueOf(dbPort.getValue().intValue()) + "/template1");
            dbNmsUrl.setValue("jdbc:postgresql://" + dbHost.getValue() + ":" + String.valueOf(dbPort.getValue().intValue()) + "/" + dbName.getValue());

            // Make sure that the password and confirmation fields match
            if (dbAdminPass.getValue() == null) {
                if (dbAdminConfirm.getValue() == null) {
                    // Valid case: both passwords are blank
                } else {
                    connectToDatabase.setIconStyle("check-failure-icon");
                    MessageBox.alert("Admin Password Entries Do Not Match", "The admin password and confirmation fields do not match. Please enter the password in both fields again.", expandConnectToDatabase);
                    return;
                }
            } else if (dbAdminPass.getValue().equals(dbAdminConfirm.getValue())) {
                // Password entries match
            } else {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("Admin Password Entries Do Not Match", "The admin password and confirmation fields do not match. Please enter the password in both fields again.", expandConnectToDatabase);
                return;
            }

            // Make sure that the password and confirmation fields match
            if (dbNmsPass.getValue() == null) {
                if (dbNmsConfirm.getValue() == null) {
                    // Valid case: both passwords are blank
                } else {
                    connectToDatabase.setIconStyle("check-failure-icon");
                    MessageBox.alert("OpenNMS Password Entries Do Not Match", "The OpenNMS password and confirmation fields do not match. Please enter the password in both fields again.", expandConnectToDatabase);
                    return;
                }
            } else if (dbNmsPass.getValue().equals(dbNmsConfirm.getValue())) {
                // Password entries match
            } else {
                connectToDatabase.setIconStyle("check-failure-icon");
                MessageBox.alert("OpenNMS Password Entries Do Not Match", "The OpenNMS password and confirmation fields do not match. Please enter the password in both fields again.", expandConnectToDatabase);
                return;
            }

            installService.connectToDatabase(dbDriver.getValue(), dbName.getValue(), dbAdminUser.getValue(), dbAdminPass.getValue(), dbAdminUrl.getValue(), dbNmsUser.getValue(), dbNmsPass.getValue(), dbNmsUrl.getValue(), new AsyncCallback<Void>() {
                public void onSuccess(Void result) {
                    connectToDatabase.setIconStyle("check-success-icon");
                    if (m_next != null) {
                        m_next.check();
                    }
                }

                public void onFailure(Throwable e) {
                    connectToDatabase.setIconStyle("check-failure-icon");

                    // If the database does not exist, give the user the option of creating the database
                    if (e instanceof DatabaseDoesNotExistException) {
                        MessageBox.confirm("Database Does Not Exist", "The database does not exist yet. Would you like to create it?", new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent event) {
                                if (Dialog.YES.equals((event.getButtonClicked().getItemId()))) {
                                    connectToDatabase.setIconStyle("check-progress-icon");
                                    installService.createDatabase(dbDriver.getValue(), dbName.getValue(), dbAdminUser.getValue(), dbAdminPass.getValue(), dbAdminUrl.getValue(), dbNmsUser.getValue(), dbNmsPass.getValue(), new AsyncCallback<Void>() {
                                        public void onSuccess(Void result) {
                                            // Re-run the check now that the database has been created
                                            thisCheck.check();
                                        }

                                        public void onFailure(Throwable e) {
                                            connectToDatabase.setIconStyle("check-failure-icon");
                                            if (e instanceof OwnershipNotConfirmedException) {
                                                handleOwnershipNotConfirmed();
                                            } else if (e instanceof DatabaseDriverException) {
                                                handleDatabaseDriverException(e);
                                            } else if (e instanceof IllegalDatabaseArgumentException) {
                                                MessageBox.alert("Invalid Database Parameter", e.getMessage(), expandConnectToDatabase);
                                            } else if (e instanceof DatabaseAccessException) {
                                                MessageBox.alert("Could Not Access Database", "The database could not be accessed: " + e.getMessage(), expandConnectToDatabase);
                                            } else if (e instanceof DatabaseAlreadyExistsException) {
                                                MessageBox.alert("Database Already Exists", "The database already exists. Please retry your connection to the database.", expandConnectToDatabase);
                                            } else if (e instanceof DatabaseUserCreationException) {
                                                MessageBox.alert("User Creation Error", "The database user for OpenNMS could not be created: " + e.getMessage(), expandConnectToDatabase);
                                            } else if (e instanceof DatabaseCreationException) {
                                                MessageBox.alert("Database Creation Error", "The OpenNMS database could not be created: " + e.getMessage(), expandConnectToDatabase);
                                            } else {
                                                handleUnexpectedExceptionInPanel(e, connectToDatabase);
                                            }
                                        }
                                    });
                                } else {
                                    // Don't do anything, just fall back to the failed panel
                                    connectToDatabase.expand();
                                }
                            }
                        });
                    } else if (e instanceof OwnershipNotConfirmedException) {
                        handleOwnershipNotConfirmed();
                    } else if (e instanceof DatabaseDriverException) {
                        handleDatabaseDriverException(e);
                    } else if (e instanceof IllegalDatabaseArgumentException) {
                        // Check for server-side validation errors
                        MessageBox.alert("Invalid Database Parameter", e.getMessage(), expandConnectToDatabase);
                    } else if (e instanceof DatabaseAccessException) {
                        MessageBox.alert("Error Accessing Database", "The database could not be accessed: " + e.getMessage(), expandConnectToDatabase);
                    } else if (e instanceof DatabaseConfigFileException) {
                        MessageBox.alert("Error Storing Database Settings", "The database configuration could not be stored.", expandConnectToDatabase);
                    } else {
                        handleUnexpectedExceptionInPanel(e, connectToDatabase);
                    }
                }
            });
        }
    }

    private class StoredProceduresCheck implements InstallationCheck {
        private final InstallationCheck m_next;
        private final boolean m_alertOnSuccess;
        public StoredProceduresCheck(InstallationCheck nextInChain, boolean alertOnSuccess) {
            m_next = nextInChain;
            m_alertOnSuccess = alertOnSuccess;
        }

        public void check() {
            // Start a spinner that indicates operation start
            checkStoredProcedures.setIconStyle("check-progress-icon");

            installService.checkIpLike(new AsyncCallback<IpLikeStatus>() {
                public void onSuccess(IpLikeStatus result) {
                    if (IpLikeStatus.C.equals(result)) {
                        checkStoredProcedures.setIconStyle("check-success-icon");
                        if (m_alertOnSuccess) {
                            MessageBox.alert("<code>iplike</code> Found", "The native C <code>iplike</code> function was tested successfully.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                    focusContinueButton.handleEvent(event);
                                    if (m_next != null) {
                                        m_next.check();
                                    }
                                }
                            });
                        } else {
                            if (m_next != null) {
                                m_next.check();
                            }
                        }
                    } else if (IpLikeStatus.PLPGSQL.equals(result)) {
                        checkStoredProcedures.setIconStyle("check-success-icon");
                        if (m_alertOnSuccess) {
                            MessageBox.alert("<code>iplike</code> Found", "The PL/pgSQL <code>iplike</code> function was tested successfully. Please note that you can achieve higher database performance by installing the native C version of <code>iplike</code>.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                    focusContinueButton.handleEvent(event);
                                    if (m_next != null) {
                                        m_next.check();
                                    }
                                }
                            });
                        } else {
                            if (m_next != null) {
                                m_next.check();
                            }
                        }
                    } else {
                        checkStoredProcedures.setIconStyle("check-failure-icon");
                        if (IpLikeStatus.MISSING.equals(result)) {
                            // TODO: Change to allow the user to install the SQL version of iplike?
                            MessageBox.alert("<code>iplike</code> Not Found", "Could not find the <code>iplike</code> stored procedure in the database. You should reinstall the <code>iplike</code> package.", expandCheckStoredProcedures);
                        } else if (IpLikeStatus.UNUSABLE.equals(result)) {
                            MessageBox.alert("<code>iplike</code> Unusable", "The <code>iplike</code> function was found but basic tests of the function failed. You should reinstall the <code>iplike</code> package.", expandCheckStoredProcedures);
                        } else {
                            MessageBox.alert("Unexpected <code>iplike</code> Language", "<code>iplike</code> appears to be usable in the database but the language cannot be determined. You should probably reinstall the <code>iplike</code> package.", expandCheckStoredProcedures);
                        }
                    }
                }

                public void onFailure(Throwable e) {
                    checkStoredProcedures.setIconStyle("check-failure-icon");
                    if (e instanceof OwnershipNotConfirmedException) {
                        handleOwnershipNotConfirmed();
                    } else if (e instanceof DatabaseDriverException) {
                        handleDatabaseDriverException(e);
                    } else if (e instanceof DatabaseConfigFileException) {
                        MessageBox.alert("Error Reading Database Settings", "The database configuration could not be read.", expandConnectToDatabase);
                    } else if (e instanceof DatabaseAccessException) {
                        MessageBox.alert("Could Not Access Database", "The database could not be accessed: " + e.getMessage(), expandConnectToDatabase);
                    } else {
                        handleUnexpectedExceptionInPanel(e, checkStoredProcedures);
                    }
                }
            });
        }
    }

    private abstract class AsyncCallbackWithReference<R,T> implements AsyncCallback<T> {
        protected final R m_reference;
        public AsyncCallbackWithReference(R o) {
            m_reference = o;
        }
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // Create a Dock Panel
        DockPanel dock = new DockPanel();
        // dock.setStyleName("cw-DockPanel");
        // dock.setSpacing(4);
        dock.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
        dock.setWidth("100%");

        VerticalPanel vertical = new VerticalPanel();
        vertical.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

        HTML contents = new HTML("<img style=\"padding: 20px;\" src=\"../images/logo.gif\" alt=\"openNMS Installer\"/>");
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
        gxtPanel.setSize(400, 500);
        gxtPanel.setBodyStyleName("transparent-background");

        // final ContentPanel verifyOwnership = new ContentPanel();
        verifyOwnership.setHeading("Verify ownership");
        verifyOwnership.setIconStyle("check-failure-icon");
        verifyOwnership.setBodyStyleName("accordion-panel");
        // Create an empty caption for the panel that will be filled by an RPC call
        final Html verifyOwnershipCaption = verifyOwnership.addText("");

        // Also reload the caption every time the panel is expanded. This
        // will allow us to handle cases where the session times out.
        verifyOwnership.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                updateOwnershipFileCaption(verifyOwnershipCaption);
            }
        });

        Button checkOwnershipButton = new Button("Check ownership file", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(new InstallationCheck() {
                    public void check() {
                        MessageBox.alert("Success", "The ownership file exists. You have permission to update the admin password and database settings.", expandSetAdminPassword);
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
        setAdminPasswordFormLayout.setDefaultWidth(230);
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
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new SetAdminPasswordCheck(new InstallationCheck() {
                        public void check() {
                            MessageBox.alert("Password Updated", "The administrator password has been updated.", expandConnectToDatabase);
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
        connectToDatabaseLayout.setLabelWidth(170);
        // Normally 150, but subtract 15 for the vertical scrollbar
        // Made the panel bigger, don't need a scrollbar any more
        connectToDatabaseLayout.setDefaultWidth(180);
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
        dbAdminUser.setFieldLabel("Admin user");
        dbAdminUser.setAllowBlank(false);
        dbAdminUser.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                if ("postgres".equals(dbAdminUser.getValue())) {
                    dbAdminUser.removeStyleName("font-style-normal");
                    dbAdminUser.addStyleName("font-style-italic");
                } else { 
                    dbAdminUser.removeStyleName("font-style-italic");
                    dbAdminUser.addStyleName("font-style-normal");
                }
            }
        });
        connectToDatabase.add(dbAdminUser);

        // final TextField<String> dbAdminPass = new TextField<String>();
        dbAdminPass.setFieldLabel("Admin password");
        dbAdminPass.setAllowBlank(false);
        dbAdminPass.setPassword(true);
        connectToDatabase.add(dbAdminPass);

        // final TextField<String> dbAdminConfirm = new TextField<String>();
        dbAdminConfirm.setFieldLabel("Confirm admin password");
        dbAdminConfirm.setAllowBlank(false);
        dbAdminConfirm.setPassword(true);
        connectToDatabase.add(dbAdminConfirm);

        // final TextField<String> dbAdminUrl = new TextField<String>();
        dbAdminUrl.setFieldLabel("Admin URL");
        dbAdminUrl.setAllowBlank(false);
        dbAdminUrl.hide();

        // final TextField<String> dbUser = new TextField<String>();
        dbNmsUser.setFieldLabel("OpenNMS user");
        dbNmsUser.setAllowBlank(false);
        dbNmsUser.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                if ("opennms".equals(dbNmsUser.getValue())) {
                    dbNmsUser.removeStyleName("font-style-normal");
                    dbNmsUser.addStyleName("font-style-italic");
                } else { 
                    dbNmsUser.removeStyleName("font-style-italic");
                    dbNmsUser.addStyleName("font-style-normal");
                }
            }
        });
        connectToDatabase.add(dbNmsUser);

        // final TextField<String> dbNmsPass = new TextField<String>();
        dbNmsPass.setFieldLabel("OpenNMS password");
        dbNmsPass.setAllowBlank(false);
        dbNmsPass.setPassword(true);
        connectToDatabase.add(dbNmsPass);

        // final TextField<String> dbNmsConfirm = new TextField<String>();
        dbNmsConfirm.setFieldLabel("Confirm OpenNMS password");
        dbNmsConfirm.setAllowBlank(false);
        dbNmsConfirm.setPassword(true);
        connectToDatabase.add(dbNmsConfirm);

        // final TextField<String> dbUrl = new TextField<String>();
        dbNmsUrl.setFieldLabel("OpenNMS URL");
        dbNmsUrl.setAllowBlank(false);
        dbNmsUrl.hide();

        // final TextField<String> dbDriver = new TextField<String>();
        dbDriver.setFieldLabel("Database driver");
        dbDriver.setAllowBlank(false);
        dbDriver.setValue("org.postgresql.Driver");
        dbDriver.hide();

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
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new DatabaseConnectionCheck(new InstallationCheck() {
                        public void check() {
                            MessageBox.alert("Success", "The connection to the database with the specified parameters was successful.", expandUpdateDatabase);
                        }
                    })
                ).check();
            }
        });
        connectToDatabase.add(connectButton);

        // Add hidden panels under the button so they don't mess up the layout
        connectToDatabase.add(dbDriver);
        connectToDatabase.add(dbAdminUrl);
        connectToDatabase.add(dbNmsUrl);

        // final ContentPanel updateDatabase = new ContentPanel();
        updateDatabase.setHeading("Initialize database");
        updateDatabase.setIconStyle("check-failure-icon");
        updateDatabase.setBodyStyleName("accordion-panel");

        /*
        Button updateButton = new Button("Update database", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                // Start a spinner that indicates operation start
                updateDatabase.setIconStyle("check-progress-icon");

                installService.clearDatabaseUpdateLogs(new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        installService.updateDatabase(new AsyncCallback<Void>() {
                            private ListLoader<ListLoadResult<LoggingEvent>> m_logLoader = null;
                            private Button m_closeLogWindowButton = null;

                            // TODO: Make sure that this UI can handle a large amount of log messages properly.
                            // TODO: Update look and feel to hide category and maybe color-code the messages
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
         */

        final FieldSet progressFields = new FieldSet();
        progressFields.hide();
        progressFields.setHeading("Database update progress");
        progressFields.addStyleName("progress-panel");

        final VerticalProgressPanel dbProgressPanel = new VerticalProgressPanel();
        dbProgressPanel.setWidth("100%");

        Button updateButton = new Button("Update database", new SelectionListener<ButtonEvent>() {
            private void handleDatabaseUpdateFailure(Throwable e) {
                // Update the panel with a failure icon
                updateDatabase.setIconStyle("check-failure-icon");
                // Mark any in-progress tasks as failed
                dbProgressPanel.markInProgressHeadersAsFailed();
                if (e instanceof OwnershipNotConfirmedException) {
                    handleOwnershipNotConfirmed(new Listener<MessageBoxEvent>() {
                        public void handleEvent(MessageBoxEvent event) {
                            // Hide any collected progress items
                            progressFields.el().fadeOut(new FxConfig(300));
                        }
                    });
                } else {
                    handleUnexpectedException(e, new Listener<MessageBoxEvent>() {
                        public void handleEvent(MessageBoxEvent event) {
                            // Hide any collected progress items
                            progressFields.el().fadeOut(new FxConfig(300));
                        }
                    });
                }
            };

            public void componentSelected(ButtonEvent event) {
                // Start a spinner that indicates operation start
                updateDatabase.setIconStyle("check-progress-icon");
                // Clear all of the bullet items from the panel
                dbProgressPanel.clear();
                // Display the bullet items if they are not yet visible
                progressFields.show();
                // Fade in the bullet items if they have been faded out
                progressFields.el().fadeIn(new FxConfig(300));

                installService.clearDatabaseUpdateLogs(new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        installService.updateDatabase(new AsyncCallback<Void>() {
                            public void onSuccess(Void result) {
                                final Timer logTimer = new Timer() {
                                    public void run() {
                                        installService.getDatabaseUpdateProgress(new AsyncCallbackWithReference<Timer,List<InstallerProgressItem>>(this) {
                                            public void onSuccess(List<InstallerProgressItem> result) {
                                                dbProgressPanel.updateProgress(result);
                                                installService.isUpdateInProgress(new AsyncCallbackWithReference<Timer,Boolean>(m_reference) {
                                                    public void onSuccess(Boolean result) {
                                                        if (result) {
                                                            // Do nothing; the Timer continues to execute
                                                        } else {
                                                            // The installer has completed! 
                                                            // Cancel the timer
                                                            m_reference.cancel();
                                                            // Check to see if the installer operation succeeded
                                                            installService.didLastUpdateSucceed(new AsyncCallback<Boolean>() {
                                                                public void onSuccess(Boolean result) {
                                                                    if (result) {
                                                                        // Update all of the progress icons with success icons
                                                                        dbProgressPanel.markInProgressHeadersAsSucceeded();
                                                                        // Change the panel icon to a success icon
                                                                        updateDatabase.setIconStyle("check-success-icon");
                                                                        MessageBox.alert("Update Succeeded", "The database update succeeded.", new Listener<MessageBoxEvent>() {
                                                                            public void handleEvent(MessageBoxEvent event) {
                                                                                // Expand the next panel
                                                                                checkStoredProcedures.expand();
                                                                                // Hide any collected progress items
                                                                                progressFields.el().fadeOut(new FxConfig(300));
                                                                            }
                                                                        });
                                                                    } else {
                                                                        // Update all of the progress icons with failure icons
                                                                        dbProgressPanel.markInProgressHeadersAsFailed();
                                                                        // Change the panel icon to a failure icon
                                                                        updateDatabase.setIconStyle("check-failure-icon");
                                                                        MessageBox.alert("Update Failed", "The database update failed. Please check the log messages for more details.", new Listener<MessageBoxEvent>() {
                                                                            public void handleEvent(MessageBoxEvent event) {
                                                                                // Hide any collected progress items
                                                                                progressFields.el().fadeOut(new FxConfig(300));
                                                                            }
                                                                        });
                                                                    }
                                                                }

                                                                public void onFailure(Throwable e) {
                                                                    handleDatabaseUpdateFailure(e);
                                                                }
                                                            });
                                                        }
                                                    }

                                                    public void onFailure(Throwable e) {
                                                        handleDatabaseUpdateFailure(e);
                                                    }
                                                });
                                            }

                                            public void onFailure(Throwable e) {
                                                handleDatabaseUpdateFailure(e);
                                            }
                                        });
                                    }
                                };
                                logTimer.scheduleRepeating(2000);
                            }

                            public void onFailure(Throwable e) {
                                handleDatabaseUpdateFailure(e);
                            }
                        });
                    }

                    public void onFailure(Throwable e) {
                        handleDatabaseUpdateFailure(e);
                    }
                });
            }
        });

        Button showLogsButton = new Button("Show log messages", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                installService.getDatabaseUpdateLogsAsStrings(new AsyncCallback<List<String>>() {
                    public void onSuccess(List<String> result) {
                        if (result == null || result.size() == 0) {
                            MessageBox.alert("No Log Messages", "There are no log messages to display.", null);
                        } else {
                            final StringBuffer html = new StringBuffer();
                            html.append("<div>\n");

                            for (String message : result) {
                                html.append("<p class=\"log-message\">" + message + "</p>\n");
                            }

                            html.append("</div>");

                            final Window w = new Window();
                            w.setHeading("Database Update Logs");
                            w.setModal(true);
                            w.setSize(600, 400);
                            w.setScrollMode(Style.Scroll.AUTO);
                            // w.setMaximizable(true);

                            /* Doesn't work
                            final Html text = w.addText(html.toString());

                            w.addButton(new Button("Select all", new SelectionListener<ButtonEvent>() {
                                public void componentSelected(ButtonEvent event) {
                                    text.focus();
                                }
                            }));
                             */

                            w.show();
                        }
                    }

                    public void onFailure(Throwable e) {
                        if (e instanceof OwnershipNotConfirmedException) {
                            handleOwnershipNotConfirmed();
                        } else {
                            handleUnexpectedException(e);
                        }
                    }
                });

                /*
                Button closeLogWindowButton = new Button("Close Window", new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        w.hide();
                        w.removeAll();
                    }
                });
                closeLogWindowButton.disable();
                w.addButton(closeLogWindowButton);
                 */
            }
        });

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(updateButton);
        buttonPanel.add(showLogsButton);
        updateDatabase.add(buttonPanel);
        progressFields.add(dbProgressPanel);
        updateDatabase.add(progressFields);

        // final ContentPanel checkStoredProcedures = new ContentPanel();
        checkStoredProcedures.setHeading("Check stored procedures");
        checkStoredProcedures.setIconStyle("check-failure-icon");
        checkStoredProcedures.setBodyStyleName("accordion-panel");
        Button checkStoredProceduresButton = new Button("Check stored procedures", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new OwnershipFileCheck(
                    new DatabaseConnectionCheck(
                        new StoredProceduresCheck(null, true)
                    )
                ).check();
            }
        });
        checkStoredProcedures.add(checkStoredProceduresButton);

        final ContentPanel tips = new ContentPanel();
        tips.setHeading("Additional tips");
        tips.setBodyStyleName("accordion-panel");
        tips.addText("<p>After you complete this wizard, the OpenNMS services will restart and the regular web UI will be launched.</p><p>To begin collecting information with OpenNMS, you need to set up IP address ranges for OpenNMS to discover by visiting the Discovery section of the Admin web interface.</p><p>To receive alerts when problems occur, you will also need to configure Notifications in the Operations section of the Admin web interface.</p><p>For more tips on getting OpenNMS up and running, please visit the <a href=\"http://www.opennms.org/index.php/QuickStart\">OpenNMS QuickStart guide</a>.</p>");

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
                            // Don't show iplike alerts on success
                            new StoredProceduresCheck(new InstallationCheck() {
                                public void check() {
                                    // TODO: Figure out best way to redirect, make it equivalent to clicking a link
                                    // com.google.gwt.user.client.Window.Location.replace("/opennms");
                                    MessageBox.alert("OpenNMS Configured Successfully", "All configuration steps are complete. Please restart OpenNMS.", null);
                                }
                            }, false)
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

        // Load all initial data from RPC services. This needs to be
        // done in series or you will end up with multiple session
        // cookie sets from the webserver on the first visit, leading
        // to multiple sessions being created but only one winning.
        //
        this.loadInitialSettings(verifyOwnershipCaption);

        RootPanel.get().add(dock);
    }

    private void loadInitialSettings(final Html caption) {
        // Add a caption that shows the user the ownership filename.
        installService.getOwnershipFilename(new AsyncCallback<String>() {
            public void onSuccess(String result) {
                caption.setHtml("<p>To prove your ownership of this OpenNMS installation, please create a file named <code>" + result + "</code> in the OpenNMS home directory.</p>");
                // Fetch the current database settings (if any)
                new GetDatabaseConnectionSettingsCheck(null).check();
            }
            public void onFailure(Throwable e) {
                handleUnexpectedException(e);
                // Fetch the current database settings (if any)
                new GetDatabaseConnectionSettingsCheck(null).check();
            }
        });
    }

    /**
     * Add a caption that shows the user the ownership filename.
     */
    private void updateOwnershipFileCaption(final Html caption) {
        installService.getOwnershipFilename(new AsyncCallback<String>() {
            public void onSuccess(String result) {
                caption.setHtml("<p>To prove your ownership of this OpenNMS installation, please create a file named <code>" + result + "</code> in the OpenNMS home directory.</p>");
            }
            public void onFailure(Throwable e) {
                handleUnexpectedException(e);
            }
        });
    }

    private void handleOwnershipNotConfirmed() {
        handleOwnershipNotConfirmed(null);
    }

    private void handleOwnershipNotConfirmed(final Listener<MessageBoxEvent> beforeHandle) {
        verifyOwnership.setIconStyle("check-failure-icon");
        MessageBox.alert("Ownership Not Confirmed", "The ownership file does not exist. Please create the ownership file in the OpenNMS home directory to prove ownership of this installation.", new Listener<MessageBoxEvent>() {
            public void handleEvent(MessageBoxEvent event) {
                if (beforeHandle != null) {
                    beforeHandle.handleEvent(event);
                }
                verifyOwnership.expand();
            }
        });
    }

    private void handleUnexpectedException(final Throwable e) {
        handleUnexpectedException(e, null);
    }

    private void handleUnexpectedException(final Throwable e, Listener<MessageBoxEvent> listener) {
        String errorMessage = "";
        // GWT throws this exception if an HTTP or connectivity problem occurs
        if (e instanceof StatusCodeException) {
            int statusCode = ((StatusCodeException)e).getStatusCode();
            switch (statusCode) {
            case (0):
                errorMessage = "Could not connect to server.";
                break;
            default:
                errorMessage = "HTTP error code: " + statusCode;
            }
        } else {
            errorMessage = e.getMessage();
            if (errorMessage == null || "".equals(errorMessage.trim())) {
                errorMessage = e.toString();
            }
        }
        MessageBox.alert("Unexpected Error", "An unexpected error occurred: " + errorMessage, listener);
    }

    private void handleUnexpectedExceptionInPanel(final Throwable e, final ContentPanel panel) {
        panel.setIconStyle("check-failure-icon");
        handleUnexpectedException(e, new Listener<MessageBoxEvent>() {
            public void handleEvent(MessageBoxEvent event) {
                panel.expand();
            }
        });
    }

    private void handleDatabaseDriverException(final Throwable e) {
        connectToDatabase.setIconStyle("check-failure-icon");
        MessageBox.alert("Database Driver Missing", "The database driver could not be loaded: " + e.getMessage(), expandConnectToDatabase);
    }

    private static class VerticalProgressPanel extends VerticalPanel {
        private final List<InstallerProgressItem> m_items = new ArrayList<InstallerProgressItem>();

        public VerticalProgressPanel() {
            this.clear();
        }

        /**
         * Clear the widgets attached to the panel and the internal list of progress items
         * that each correspond to one widget in the panel.
         */
        public void clear() {
            this.clear(true);
        }

        public void clear(boolean addCaption) {
            super.clear();
            m_items.clear();
            if (addCaption) {
                Header header = new Header();
                header.setText("Loading progress items...");
                header.setTextStyle("progress-header");
                header.setIconStyle("check-progress-icon");
                this.add(header);
            }
        }

        public void updateProgress(Collection<InstallerProgressItem> progressItems) {
            this.clear(false);

            for (InstallerProgressItem item : progressItems) {
                m_items.add(item);
                Header header = new Header();
                header.setText(item.getName());
                header.setTextStyle("progress-header");
                if (Progress.COMPLETE.equals(item.getProgress())) {
                    header.setIconStyle("check-success-icon");
                } else if (Progress.IN_PROGRESS.equals(item.getProgress())) {
                    header.setIconStyle("check-progress-icon");
                } else if (Progress.INCOMPLETE.equals(item.getProgress())) {
                    header.setIconStyle("check-incomplete-icon");
                } else if (Progress.INDETERMINATE.equals(item.getProgress())) {
                    header.setIconStyle("check-incomplete-icon");
                }
                this.add(header);
            }

            // Clear, and add a default caption
            if (m_items.size() == 0) {
                this.clear();
            }
        }

        public void markInProgressHeadersAsFailed() {
            if (m_items.size() == 0) {
                // Nothing to do
            } else { 
                if (this.getWidgetCount() != m_items.size()) {
                    throw new IllegalStateException("Panel items are inconsistent: " + this.getWidgetCount() + " != " + m_items.size());
                }
                int i = 0;
                for (InstallerProgressItem item : m_items) {
                    if (Progress.IN_PROGRESS.equals(item.getProgress())) {
                        ((IconSupport)this.getWidget(i)).setIconStyle("check-failure-icon");
                    }
                    i++;
                }
            }
        }

        public void markInProgressHeadersAsSucceeded() {
            if (m_items.size() == 0) {
                // Nothing to do
            } else { 
                if (this.getWidgetCount() != m_items.size()) {
                    throw new IllegalStateException("Panel items are inconsistent: " + this.getWidgetCount() + " != " + m_items.size());
                }
                int i = 0;
                for (InstallerProgressItem item : m_items) {
                    if (Progress.IN_PROGRESS.equals(item.getProgress())) {
                        ((IconSupport)this.getWidget(i)).setIconStyle("check-success-icon");
                    }
                    i++;
                }
            }
        }
    }
}
