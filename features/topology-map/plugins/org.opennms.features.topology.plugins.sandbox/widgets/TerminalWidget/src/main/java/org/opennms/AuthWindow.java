package org.opennms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class AuthWindow extends Window {

	private String host;
	private int port;
	private int TERM_WIDTH = 650;
	private int TERM_HEIGHT = 454;
	private ClientSession session = null;
	private Window mainWindow;
	private TerminalApplication app;
	
	public AuthWindow(TerminalApplication app, Window mainWindow, String h, int p) {
		this.mainWindow = mainWindow;
		this.app = app;
		host = h;
		port = p;
		setModal(true);
		setCaption("Login");
		setWidth("260px");
		setHeight("180px");
		int posX = (int)(app.getMainWindow().getWidth() - this.getWidth())/2;
		int posY = (int)(app.getMainWindow().getHeight() - this.getHeight())/2;
		this.setPositionX(posX);
		this.setPositionY(posY);
		setResizable(false);
		final Label usernameLabel = new Label("Username: ");
		final TextField usernameField = new TextField();
		final Label passwordLabel = new Label("Password: ");
		final PasswordField passwordField = new PasswordField();
		final Button loginButton = new Button("Login");
		final SshClient client = SshClient.setUpDefaultClient();
		client.start();
		loginButton.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				String login = (String)usernameField.getValue();
				String password = (String)passwordField.getValue();
				try {
					session = client.connect(host, port).await().getSession();

					int ret = ClientSession.WAIT_AUTH;
					if (session != null){
						while ((ret & ClientSession.WAIT_AUTH) != 0) {
							session.authPassword(login, password);
							ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
						}
						if ((ret & ClientSession.CLOSED) != 0) {
							getApplication().getMainWindow().showNotification("Failed to Login");
							return;
						}
						showSSHWindow();
					} 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		GridLayout grid = new GridLayout(2,2);
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
	
	private void showSSHWindow() {
		mainWindow.addWindow(getSSHWindow());
		this.close();
	}

	private Window getSSHWindow() {
		SSHWindow sshWindow = new SSHWindow(app, session, TERM_WIDTH, TERM_HEIGHT);
		sshWindow.addListener(app);
		return sshWindow;
	}
	
}
