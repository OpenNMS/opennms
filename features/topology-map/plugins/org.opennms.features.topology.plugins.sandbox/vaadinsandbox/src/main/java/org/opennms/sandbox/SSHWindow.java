package org.opennms.sandbox;

import org.vaadin.console.Console;
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
        
        /*Creates a layout and adds the console component to it*/
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(console);
        layout.setSizeFull();
        layout.setImmediate(true);
        
        addComponent(layout);
	}
	
}
