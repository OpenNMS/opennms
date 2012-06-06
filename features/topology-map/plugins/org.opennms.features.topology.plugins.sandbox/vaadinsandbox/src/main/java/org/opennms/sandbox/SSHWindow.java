package org.opennms.sandbox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.vaadin.console.Console;
import org.vaadin.console.Console.Handler;
import org.vaadin.console.DefaultConsoleHandler;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.agent.SshAgent;
import org.apache.sshd.client.future.ConnectFuture;     


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
        
        /*Sets the properties of the sub-window*/
        setCaption("SSH - " + node.getName());
        setImmediate(true);
        setResizable(false);
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
        setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
        
        /*Creates a console component and sets its properties*/
        Console console = new Console();
        console.setPs("}> ");
        console.setHeight("" + browserHeight + "px");
        console.setWidth("" + browserWidth + "px");
        console.setMaxBufferSize(20);
        console.setGreeting("Welcome to SSH Terminal that does nothing");
        console.reset();
        console.focus();
        
        Handler scriptHandler = new DefaultConsoleHandler() {
            private static final long serialVersionUID = -5733237166568671987L;

            @Override
            public void inputReceived(Console console, String lastInput) {

                // Check if there is registered command and use it by default
                int i = lastInput.indexOf(' ');
                String cmdName = lastInput.substring(0, i >= 0 ? i : lastInput.length());
                if (console.getCommand(cmdName) != null) {
                    super.inputReceived(console, lastInput);
                    return;
                }

                // Run the input in the engine
                try {
                    SSHWindow.doThatSSHThing(console, lastInput);
//                    Object r = engine.eval(lastInput);
                    { /*if (r != null) {*/
//                        console.print("" + r);
                    }
                } catch (Exception e) {
//                    console.print(e.getMessage());
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
	}
	
	public static void doThatSSHThing(Console console, String lastInput) {
	        String host = "localhost";
	        int port = 8101;
	        String user = "karaf";
	        String password = "karaf";

	        SshClient client = null;
	        try {
	            client = SshClient.setUpDefaultClient();
	            client.start();
	            
	            
	            ConnectFuture future = client.connect(host, port);
	            future.await();
	            ClientSession session = future.getSession();
	            session.authPassword(user, password);
	            ClientChannel channel = session.createChannel("shell");
	            channel.setIn(new ByteArrayInputStream(lastInput.getBytes()));
	            channel.setOut(console.getPrintStream());
	            channel.setErr(console.getPrintStream());
	            channel.open();
	            channel.waitFor(ClientChannel.CLOSED, 0);
	        } catch (Throwable t) {
	            t.printStackTrace();
	            System.exit(1);
	        } finally {
	            try {
	                client.stop();
	            } catch (Throwable t) { }
	        }
	        System.exit(0);
	}
	
}
