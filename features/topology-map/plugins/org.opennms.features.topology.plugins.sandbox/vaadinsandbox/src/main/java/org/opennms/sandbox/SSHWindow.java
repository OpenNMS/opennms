package org.opennms.sandbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.vaadin.console.Console;
import org.vaadin.console.Console.Handler;
import org.vaadin.console.DefaultConsoleHandler;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
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
    private static PipedOutputStream m_sshOutputStream;
    private static PipedInputStream m_sshInputStream;
    private static Console m_console = new Console();
    private static ClientChannel channel = null;
    private static SshClient client = null;
    private static ClientSession session = null;
    private static boolean loginFlag = false;
    private static String host = "debian.opennms.org";
    private static String username;
    private int port = 22;
    private static PrintStream m_printStream;
    private static boolean doneWriting = false;
    private static String m_line = "";


    /**
     * The SSHWindow method constructs a sub-window instance that can be added to a main window
     * The sub-window contains a console component which uses an SSH client to access nodes
     * @param node Selected node
     * @param width Width of the main window
     * @param height Height of the main window
     * @throws IOException 
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

        try {
            m_sshOutputStream = new PipedOutputStream(){

                @Override
                public void write(byte[] b, int off, int len){
                    doneWriting = false;
                    try {
                        super.write(b, off, len);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    doneWriting = true;
                }
            };
            m_sshInputStream = new PipedInputStream(m_sshOutputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        HorizontalLayout loginForm = new HorizontalLayout();
        loginForm.setSpacing(true);
        loginForm.setMargin(false, false, true, false);
        Label usernameLabel = new Label("Username:   ");
        final TextField usernameTF = new TextField();
        Label passwordLabel = new Label("Password:   ");
        final PasswordField passwordField = new PasswordField();
        final Button loginButton = new Button("Login");
        loginButton.addListener(new ClickListener(){

            public void buttonClick(ClickEvent event) {
                if (event.getButton() == loginButton){
                    username = (String)usernameTF.getValue();
                    authorize((String)passwordField.getValue());
                    if (!loginFlag){
                        showNotification("Incorrect password, please try again.", Window.Notification.TYPE_WARNING_MESSAGE);
                    }else{
                        m_console.setPs(username + "@" + host + ":~$");
                        m_console.focus();
                    }
                }
            }

        });
        loginForm.addComponent(usernameLabel);
        loginForm.addComponent(usernameTF);
        loginForm.addComponent(passwordLabel);
        loginForm.addComponent(passwordField);
        loginForm.addComponent(loginButton);

        /*Creates a console component and sets its properties*/

        m_console.setImmediate(true);
        m_console.setPs("}> ");
        m_console.setHeight("" + (browserHeight-50) + "px");
        m_console.setWidth("" + browserWidth + "px");
        m_console.setGreeting("Welcome to SSH Terminal that does nothing");
        m_console.prompt();
        m_console.reset();
        m_console.focus();

        Handler scriptHandler = new DefaultConsoleHandler() {
            private static final long serialVersionUID = -5733237166568671987L;

            @Override
            public void inputReceived(Console console, String lastInput) {
                try {
                    m_line = lastInput;
                    lastInput += "\n";         
                    input.setBuffer(lastInput.getBytes());
                    synchronized(m_console) {
                        m_console.wait(); 
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
                console.prompt();
                return;
            }
        };
        m_console.setHandler(scriptHandler);

        /*Creates a layout and adds the console component to it*/
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(loginForm);
        layout.addComponent(m_console);
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
        Thread results = new Thread() {

            @Override
            public void run(){
                byte[] resultBuffer;
                while (true) {
                    try {
                        int len = m_sshInputStream.available();
                        if (len > 0 && doneWriting){
                            resultBuffer = new byte[len];
                            m_sshInputStream.read(resultBuffer, 0, len);
                            m_console.print(new String(resultBuffer));
                            doneWriting = false;
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        results.start();
        chThread.setInputStream(input);
        chThread.setOutputStream(getPrintStream());
        chThread.setClientChannel(channel);
        chThread.setClientSession(session);
        chThread.start();
    }

    private static void authorize(String pw){
        try {
            int ret = ClientSession.WAIT_AUTH;
            session.authPassword(username, pw);
            ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
            if ((ret & ClientSession.WAIT_AUTH) == 0) {
                loginFlag = true;
                openChannel();
                m_console.requestRepaint();
            } 
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static PrintStream getPrintStream() {
        if (m_printStream == null) {
            m_printStream = new PrintStream(new OutputStream() {

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                @Override
                public synchronized void write(byte[] b, int off, int len) throws IOException {

                    super.write(b, off, len);
                    synchronized(m_console) {
                        m_console.notifyAll();
                    }
                }

                public synchronized void write(final int b) throws IOException {
                    buffer.write(b);
                    // Line buffering
                    if (13 == b) {
                        flush();
                    }
                }

                @Override
                public synchronized void flush() throws IOException {
                    String line = buffer.toString();
                    super.flush();
                    buffer.flush();
                    if (line.contains(username +"@" )){
                        m_console.setPs(line);

                    }else{
                        if(!line.contains(m_line)){
                            m_console.print(line);
                        }
                    }
                    buffer = new ByteArrayOutputStream();
                }
            }, true);
        }
        return m_printStream;
    }

}
