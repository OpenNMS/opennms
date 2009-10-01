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

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.button.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.*; 

import org.apache.log4j.Logger;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint {

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        // Create the RemoteServiceServlet that acts as the controller for this GWT view
        final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

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
        gxtPanel.setSize(300, 300);
        gxtPanel.setBodyStyleName("transparent-background");
        // gxtPanel.setHorizontalAlignment(ContentPanel.);

        final ContentPanel verifyOwnership = new ContentPanel();
        verifyOwnership.setHeading("Verify ownership");
        // verifyOwnership.setLayout(new FitLayout());
        verifyOwnership.setIconStyle("check-failure-icon");
        verifyOwnership.setBodyStyleName("accordion-panel");
        // verifyOwnership.addText("Add form controls here.");
        final Html verifyOwnershipCaption = verifyOwnership.addText("");
        verifyOwnership.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent e) {
            }
        });

        installService.getOwnershipFilename(new AsyncCallback<String>() {
            public void onSuccess(String result) {
                verifyOwnershipCaption.setHtml("<p>To prove ownership of this appliance, please create a file named <code>" + result + "</code> in the OpenNMS home directory.</p>");
            }
            public void onFailure(Throwable e) {
                verifyOwnership.setIconStyle("check-failure-icon");
                MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
            }
        });
        Button checkOwnershipButton = new Button("Check ownership file", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                // Start a spinner that indicates operation start
                verifyOwnership.setIconStyle("check-progress-icon");

                installService.checkOwnershipFileExists(new AsyncCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        if (result) {
                            verifyOwnership.setIconStyle("check-success-icon");
                            MessageBox.alert("Success", "The ownership file exists. You have permission to update the admin password and database settings.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                }
                            });
                        } else {
                            verifyOwnership.setIconStyle("check-failure-icon");
                            MessageBox.alert("Failure", "The ownership file does not exist. Please create the ownership file in the OpenNMS home directory to prove ownership of this installation.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                }
                            });
                        }
                    }

                    public void onFailure(Throwable e) {
                        verifyOwnership.setIconStyle("check-failure-icon");
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    }
                });
            }
        });
        verifyOwnership.add(checkOwnershipButton);

        final ContentPanel setAdminPassword = new ContentPanel();
        setAdminPassword.setHeading("Set administrator password");
        setAdminPassword.setIconStyle("check-failure-icon");
        setAdminPassword.setBodyStyleName("accordion-panel");

        FormLayout setAdminPasswordFormLayout = new FormLayout();
        setAdminPasswordFormLayout.setLabelPad(10);
        setAdminPasswordFormLayout.setLabelWidth(120);
        setAdminPasswordFormLayout.setDefaultWidth(150);
        setAdminPassword.setLayout(setAdminPasswordFormLayout);

        final TextField<String> passwd = new TextField<String>();
        passwd.setFieldLabel("New admin password");
        passwd.setAllowBlank(false);
        passwd.setMinLength(6);
        passwd.setPassword(true);
        setAdminPassword.add(passwd);

        final TextField<String> confirm = new TextField<String>();
        confirm.setFieldLabel("Confirm password");
        confirm.setAllowBlank(false);
        // confirm.setMinLength(6);
        confirm.setPassword(true);
        setAdminPassword.add(confirm);

        setAdminPassword.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent e) {
                setAdminPassword.layout();
            }
        });

        Button updatePasswordButton = new Button("Update password", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                if (!passwd.validate()) {
                    MessageBox.alert("Password Validation Failed", "Blank passwords are not allowed. Please enter a new password.", null);
                    return;
                }
                if (passwd.getValue() != null && passwd.getValue().equals(confirm.getValue())) {
                    installService.setAdminPassword(passwd.getValue(), new AsyncCallback<Void>() {
                        public void onSuccess(Void result) {
                            // Start a spinner that will read the log messages from the installer
                            // Start an interval that will monitor the installer thread
                            MessageBox.alert("Password Updated", "The administrator password has been updated.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                }
                            });
                        }

                        public void onFailure(Throwable e) {
                            MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                        }
                    });
                } else {
                    MessageBox.alert("Password Entries Do Not Match", "The password and confirmation fields do not match. Please enter the new password in both fields again.", null);
                }
            }
        });
        setAdminPassword.add(updatePasswordButton);

        final ContentPanel connectToDatabase = new ContentPanel();
        connectToDatabase.setHeading("Connect to database");
        connectToDatabase.setIconStyle("check-failure-icon");
        connectToDatabase.setBodyStyleName("accordion-panel");
        connectToDatabase.setScrollMode(Style.Scroll.AUTOY);

        FormLayout connectToDatabaseLayout = new FormLayout();
        connectToDatabaseLayout.setLabelPad(10);
        connectToDatabaseLayout.setLabelWidth(120);
        // Normally 150, but subtract 15 for the vertical scrollbar
        connectToDatabaseLayout.setDefaultWidth(135);
        connectToDatabase.setLayout(connectToDatabaseLayout);

        final TextField<String> dbName = new TextField<String>();
        dbName.setFieldLabel("Database name");
        dbName.setAllowBlank(false);
        connectToDatabase.add(dbName);

        final TextField<String> dbUser = new TextField<String>();
        dbUser.setFieldLabel("Database user");
        dbUser.setAllowBlank(false);
        connectToDatabase.add(dbUser);

        final TextField<String> dbPass = new TextField<String>();
        dbPass.setFieldLabel("Database password");
        dbPass.setAllowBlank(false);
        dbPass.setPassword(true);
        connectToDatabase.add(dbPass);

        final TextField<String> dbConfirm = new TextField<String>();
        dbConfirm.setFieldLabel("Confirm password");
        dbConfirm.setAllowBlank(false);
        dbConfirm.setPassword(true);
        connectToDatabase.add(dbConfirm);

        final TextField<String> dbDriver = new TextField<String>();
        dbDriver.setFieldLabel("Database driver");
        dbDriver.setAllowBlank(false);
        dbDriver.setPassword(true);
        connectToDatabase.add(dbDriver);

        final TextField<String> dbUrl = new TextField<String>();
        dbUrl.setFieldLabel("Database URL");
        dbUrl.setAllowBlank(false);
        dbUrl.setPassword(true);
        connectToDatabase.add(dbUrl);

        final TextField<String> dbBinDir = new TextField<String>();
        dbBinDir.setFieldLabel("Database binary directory");
        dbBinDir.setAllowBlank(false);
        dbBinDir.setPassword(true);
        connectToDatabase.add(dbBinDir);

        Button connectButton = new Button("Connect to database", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                installService.connectToDatabase(new AsyncCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        if (result) {
                            connectToDatabase.setIconStyle("check-success-icon");
                            MessageBox.alert("Success", "The connection to the database with the specified parameters was successful.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                }
                            });
                        } else {
                            connectToDatabase.setIconStyle("check-failure-icon");
                            MessageBox.alert("Failure", "Could not connect to the database with the specified parameters.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                }
                            });
                        }
                    }

                    public void onFailure(Throwable e) {
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    }
                });
            }
        });
        connectToDatabase.add(connectButton);

        final ContentPanel updateDatabase = new ContentPanel();
        updateDatabase.setHeading("Update database");
        updateDatabase.setIconStyle("check-failure-icon");
        updateDatabase.setBodyStyleName("accordion-panel");

        Button updateButton = new Button("Update database", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                installService.updateDatabase(new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        // Start a spinner that will read the log messages from the installer
                        // Start an interval that will monitor the installer thread
                        MessageBox.alert("Update Started", "The database update has been started.", new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent event) {
                            }
                        });
                    }

                    public void onFailure(Throwable e) {
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    }
                });
            }
        });
        updateDatabase.add(updateButton);
        updateDatabase.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent e) {
            }
        });

        final ContentPanel checkStoredProcedures = new ContentPanel();
        checkStoredProcedures.setHeading("Check stored procedures");
        checkStoredProcedures.setIconStyle("check-failure-icon");
        checkStoredProcedures.setBodyStyleName("accordion-panel");
        checkStoredProcedures.addListener(Events.BeforeExpand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent e) {
            }
        });
        Button checkStoredProceduresButton = new Button("Check stored procedures", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                installService.checkIpLike(new AsyncCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        if (result) {
                            checkStoredProcedures.setIconStyle("check-success-icon");
                            MessageBox.alert("Success", "The <code>iplike</code> stored procedure is installed properly.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                }
                            });
                        } else {
                            checkStoredProcedures.setIconStyle("check-failure-icon");
                            MessageBox.alert("Failure", "Could not find the <code>iplike</code> stored procedure in the database.", new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent event) {
                                }
                            });
                        }
                    }

                    public void onFailure(Throwable e) {
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    }
                });
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

        // ExtJS button
        Button continueButton = new Button("Continue &#0187;", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                installService.checkIpLike(new AsyncCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        MessageBox.alert("Alert", "iplike installed correctly: " + result.toString(), new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent event) {
                                installService.getDatabaseUpdateLogs(-1, new AsyncCallback<List<LoggingEvent>>() {
                                    public void onSuccess(List<LoggingEvent> result) {
                                        String message = "";
                                        for (LoggingEvent event : result) {
                                            message += event.getMessage().trim() + "\n";
                                        }
                                        MessageBox.alert("Alert", message, new Listener<MessageBoxEvent>() {
                                            public void handleEvent(MessageBoxEvent event) {
                                            }
                                        });
                                    }

                                    public void onFailure(Throwable e) {
                                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                                    }
                                });
                            }
                        });
                    }

                    public void onFailure(Throwable e) {
                        MessageBox.alert("Alert", "Something failed: " + e.getMessage().trim(), null);
                    }
                });
                // MessageBox.alert("Alert", "Button pressed on OpenNMS install wizard.", null);
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
