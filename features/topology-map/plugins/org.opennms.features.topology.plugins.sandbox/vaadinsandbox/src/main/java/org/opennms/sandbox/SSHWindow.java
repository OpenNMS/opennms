package org.opennms.sandbox;


import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.vaadin.console.Console;
import org.vaadin.console.Console.Handler;
import org.vaadin.console.DefaultConsoleHandler;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * The SSHWindow class constructs a custom Window component which contains a terminal
 * that can SSH into the currently selected node
 * @author Leonardo Bell
 * @author Philip Grenon
 */
@SuppressWarnings("serial")
public class SSHWindow extends Window {

	private double sizePercentage = 0.8; // Window size ratio to the main window
	private final int widthCushion = 50; //Border cushion for width of window;
	private final int heightCushion = 110; //Border cushion for height of window
	private static ChannelThread chThread = new ChannelThread();
	private static ConsoleInputStream input = new ConsoleInputStream();
	private static Console console = new Console();
	private static org.apache.sshd.ClientChannel channel = null;
	private static org.apache.sshd.SshClient client = null;
	private static org.apache.sshd.ClientSession session = null;
	private static boolean loginFlag = false;
	private static String host = "debian.opennms.org";
	private static String login = System.getProperty("user.name");
	private int port = 22;


	/**
	 * The SSHWindow method constructs a sub-window instance that can be added to a main window
	 * The sub-window contains a console component which uses an SSH client to access nodes
	 * @param node Selected node
	 * @param width Width of the main window
	 * @param height Height of the main window
	 */
	public SSHWindow(Node node, float width, float height) {

		/*Sets the browser and window size based on the main window size*/
		int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
		int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
		System.setIn(input);
		
		/*Sets the properties of the sub-window*/
		setCaption("SSH - " + node.getName());
		setImmediate(true);
		setResizable(false);
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
		setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));

		/*Creates a console component and sets its properties*/

		console.setImmediate(true);
		console.setPs("}> ");
		console.setHeight("" + browserHeight + "px");
		console.setWidth("" + browserWidth + "px");
		console.setGreeting("Welcome to SSH Terminal that does nothing");
		console.print("Password: ");
		console.prompt();
		
		//console.reset();
		console.focus();

		Handler scriptHandler = new DefaultConsoleHandler() {
			private static final long serialVersionUID = -5733237166568671987L;

			@Override
			public void inputReceived(Console console, String lastInput) {

				try {
					if (!loginFlag){
						authorize(lastInput);
					} else {
						console.println("Input was changed");
						input.setBuffer(lastInput.getBytes());
						console.requestRepaint();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				console.prompt();
				return;
			}
		};

		console.setHandler(scriptHandler);
		/*Creates a layout and adds the console component to it*/
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(console);
		layout.setSizeFull();
		layout.setImmediate(true);

		addComponent(layout);
		initializeSession();
	}

	public void initializeSession() {
		client = SshClient.setUpDefaultClient();
		client.start();
		try {
			session = client.connect(host, port).await().getSession();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void openChannel(){
		chThread.setInputStream(input);
		chThread.setOutputStream(console.getPrintStream());
		chThread.setClientChannel(channel);
		chThread.setClientSession(session);
		chThread.start();
	}
	
	private static void authorize(String pw){
		try {
			int ret = ClientSession.WAIT_AUTH;
			session.authPassword(login, pw);
			ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
			if ((ret & ClientSession.WAIT_AUTH) == 0) {
				loginFlag = true;
				openChannel();
			} else {
				console.println("Invalid password, try again.");
				console.print("Password: ");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
