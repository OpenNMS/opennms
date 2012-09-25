/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.ssh.internal;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

/**
 * This class creates a window to authorize usernames
 * and passwords for the SSH server. 
 * @author pdgrenon
 * @author lmbell
 *
 */
@SuppressWarnings("serial")
public class AuthWindow extends Window implements Button.ClickListener{

    SSHWindow sshWindow; // The SSH window that will arise after the auth window connects
    private String m_host;  // The hostname to connect to
    private int m_port;  // The port to connect to
    private int TERM_WIDTH = 800;  // The width of the terminal
    private int TERM_HEIGHT = 520;   // The height of the terminal
    private ClientSession session = null; // The ClientSession object used to track each SSH session
    final SshClient client;
    protected String testString; // used to unit test the button click event
    protected TextField hostField;
    protected TextField portField;
    protected TextField usernameField;
    protected PasswordField passwordField;
    private boolean showOptions = false;
    private final int FIELD_BUFFER = 20;

    /**
     * This constructor method spawns a window to authorize the
     * username and password input by the user. If the authroization
     * is sucessful, the user will be connected to the host at the 
     * given port through SSH, and the terminal emulator this window
     * will be replaced by a terminal emulator. 
     * 
     * @param app - The current application
     * @param h - The host name to connect to 
     * @param p - The port number to connect to
     */
    public AuthWindow(String host, int port) {
        m_host = host;
        m_port = port;
        if ("".equals(m_host) || m_port == 0) {
            showOptions = true;
        }
        setName("Auth Window");
        setModal(true);
        setCaption("Login");
        setWidth("260px");
        setHeight("190px");
        if (showOptions) setHeight("260px");
        setResizable(false);

        Label hostLabel = new Label("Host: ");
        hostField = new TextField();
        hostField.setMaxLength(FIELD_BUFFER);

        Label portLabel = new Label("Port: ");
        portField = new TextField();
        portField.setMaxLength(FIELD_BUFFER);

        Label usernameLabel = new Label("Username: ");
        usernameField = new TextField();
        usernameField.setMaxLength(FIELD_BUFFER);

        Label passwordLabel = new Label("Password: ");
        passwordField = new PasswordField();
        passwordField.setMaxLength(FIELD_BUFFER);

        final Button loginButton = new Button("Login");
        loginButton.setClickShortcut(KeyCode.ENTER);
        client = SshClient.setUpDefaultClient();
        client.start();
        loginButton.addListener(this); 
        GridLayout grid = new GridLayout(2,2);
        if (showOptions) {
            grid = new GridLayout(2,4);
            grid.addComponent(hostLabel);
            grid.addComponent(hostField);
            grid.addComponent(portLabel);
            grid.addComponent(portField);
        }
        grid.addComponent(usernameLabel);
        grid.addComponent(usernameField);
        grid.addComponent(passwordLabel);
        grid.addComponent(passwordField);
        grid.setSpacing(true);
        grid.setMargin(false, false, true, false);
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(grid);
        layout.addComponent(loginButton);
        layout.setComponentAlignment(loginButton, Alignment.BOTTOM_RIGHT);
        addComponent(layout);
    }

    /**
     * The validateInput method attempts to set the host and port variables. If the port
     * is not an integer, an exception is thrown. If the port is not between 1 and 65535,
     * the method returns false;
     * @return Validity of the users input
     * @throws NumberFormatException Port was not an integer
     */
    protected boolean validateInput() throws NumberFormatException {
        m_host = (String)hostField.getValue();
        m_port = Integer.parseInt((String)portField.getValue());
        if (m_port < 0 || m_port > 65535) return false;
        return true;
    }

    @Override
    public void attach() {
        super.attach();

        int posX = (int)(getApplication().getMainWindow().getWidth() - this.getWidth())/2;
        int posY = (int)(getApplication().getMainWindow().getHeight() - this.getHeight())/2;
        setPositionX(posX);
        setPositionY(posY);
        if (showOptions) hostField.focus();
        else usernameField.focus();
    }

    /**
     * This methods adds (shows) the SSH Window to the main application
     */
    protected void showSSHWindow() {
        sshWindow = new SSHWindow(session, TERM_WIDTH, TERM_HEIGHT);
        getApplication().getMainWindow().addWindow(sshWindow);
        this.close();
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        String login = (String)usernameField.getValue();
        String password = (String)passwordField.getValue();
        boolean validInput = false;
        try { 
            if (showOptions) {
                validInput = validateInput();
                if (!validInput) {
                    testString = "Port must be between 1 and 65535";
                    getApplication().getMainWindow().showNotification("Port must be between 1 and 65535", Notification.TYPE_WARNING_MESSAGE);
                }
            } else validInput = true;
        } catch (NumberFormatException e) {
            testString = "Port must be an integer";
            getApplication().getMainWindow().showNotification("Port must be an integer", Notification.TYPE_WARNING_MESSAGE);
        }
        if (validInput) {
            try {
                session = client.connect(m_host, m_port).await().getSession();

                int ret = ClientSession.WAIT_AUTH;
                if (session != null){
                    while ((ret & ClientSession.WAIT_AUTH) != 0) {
                        session.authPassword(login, password);
                        ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
                    }
                    if ((ret & ClientSession.CLOSED) != 0) {
                        testString = "Failed to log in";
                        getApplication().getMainWindow().showNotification("Failed to log in", Notification.TYPE_WARNING_MESSAGE);
                        return;
                    }
                    showSSHWindow();
                } 
            } catch (Exception e) {
                testString = "Failed to connect to host";
                getApplication().getMainWindow().showNotification("Failed to connect to host", Notification.TYPE_WARNING_MESSAGE);
            }
        }
    }

}

