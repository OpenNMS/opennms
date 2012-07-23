package org.opennms.features.topology.netutils.internal;

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
public class AuthWindow extends Window {

	private String host;  // The hostname to connect to
	private int port;  // The port to connect to
	private int TERM_WIDTH = 650;  // The width of the terminal
	private int TERM_HEIGHT = 460;   // The height of the terminal
	private ClientSession session = null; // The ClientSession object used to track each SSH session
	
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
	public AuthWindow(String h, int p) {
		host = h;
		port = p;
		setModal(true);
		setCaption("Login");
		setWidth("260px");
		setHeight("190px");
		setResizable(false);
		final Label usernameLabel = new Label("Username: ");
		final TextField usernameField = new TextField();
		final Label passwordLabel = new Label("Password: ");
		final PasswordField passwordField = new PasswordField();
		final Button loginButton = new Button("Login");
		loginButton.setClickShortcut(KeyCode.ENTER);
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
	
	@Override
	public void attach() {
		super.attach();
		
		int posX = (int)(getApplication().getMainWindow().getWidth() - this.getWidth())/2;
		int posY = (int)(getApplication().getMainWindow().getHeight() - this.getHeight())/2;
		setPositionX(posX);
		setPositionY(posY);
	}
	
	 /**
	  * This methods adds (shows) the SSH Window to the main application
	  */
	private void showSSHWindow() {
		getApplication().getMainWindow().addWindow(getSSHWindow());
		this.close();
	}

	/**
	 * This method creates a new SSH window 
	 * @return The newly created SSH window
	 */
	private Window getSSHWindow() {
		SSHWindow sshWindow = new SSHWindow(session, TERM_WIDTH, TERM_HEIGHT);
		return sshWindow;
	}
	
}
