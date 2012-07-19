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
public class AuthWindow extends Window {

	private String m_host;  // The hostname to connect to
	private int m_port;  // The port to connect to
	private int TERM_WIDTH = 800;  // The width of the terminal
	private int TERM_HEIGHT = 520;   // The height of the terminal
	private ClientSession session = null; // The ClientSession object used to track each SSH session
	protected TextField hostField;
	protected TextField portField;
	protected TextField usernameField;
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
		setModal(true);
		setCaption("Login");
		setWidth("260px");
		setHeight("190px");
		if (showOptions) setHeight("260px");
		setResizable(false);

		final Label hostLabel = new Label("Host: ");
		hostField = new TextField();
		hostField.setMaxLength(FIELD_BUFFER);

		final Label portLabel = new Label("Port: ");
		portField = new TextField();
		portField.setMaxLength(FIELD_BUFFER);

		final Label usernameLabel = new Label("Username: ");
		usernameField = new TextField();
		usernameField.setMaxLength(FIELD_BUFFER);

		final Label passwordLabel = new Label("Password: ");
		final PasswordField passwordField = new PasswordField();
		passwordField.setMaxLength(FIELD_BUFFER);

		final Button loginButton = new Button("Login");
		loginButton.setClickShortcut(KeyCode.ENTER);
		final SshClient client = SshClient.setUpDefaultClient();
		client.start();
		loginButton.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				String login = (String)usernameField.getValue();
				String password = (String)passwordField.getValue();
				boolean validInput = false;
				try { 
					if (showOptions) {
						validInput = validateInput();
						if (!validInput) {
							getApplication().getMainWindow().showNotification("Port must be between 1 and 65535", Notification.TYPE_WARNING_MESSAGE);
						}
					} else validInput = true;
				} catch (NumberFormatException e) {
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
								getApplication().getMainWindow().showNotification("Failed to Login", Notification.TYPE_WARNING_MESSAGE);
								return;
							}
							showSSHWindow();
						} 
					} catch (Exception e) {
						getApplication().getMainWindow().showNotification("Failed to connect to host", Notification.TYPE_WARNING_MESSAGE);
					}
				}
			}
		});
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
