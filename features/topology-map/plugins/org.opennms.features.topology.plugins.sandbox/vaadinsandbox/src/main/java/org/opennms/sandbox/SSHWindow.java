package org.opennms.sandbox;

import org.vaadin.console.Console;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SSHWindow extends Window {

	private int cols = 170;
	private int rows = 52;
	private double sizePercentage = 0.8;
	private VerticalLayout layout = new VerticalLayout();
	private final int widthCushion = 50; //Border cushion for width of window;
    private final int heightCushion = 110; //Border cushion for height of window
	
	public SSHWindow(Node testNode, float width, float height) {
		
		setCaption("Node Info");
        setImmediate(true);
        setResizable(false);
        
        int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
        int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
        setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
        
        final Console console = new Console();
        
        console.setPs("}> ");
        console.setCols(cols);
        console.setRows(rows);
        console.setMaxBufferSize(20);
        console.setGreeting("Welcome to SSH Terminal that does nothing");
        console.reset();
        console.focus();
        
        layout.addComponent(console);
        layout.setSizeFull();
        layout.setImmediate(true);
        addComponent(layout);
	}
}
