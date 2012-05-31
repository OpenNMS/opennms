package org.opennms.sandbox;

import java.net.MalformedURLException;
import java.net.URL;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ResourceGraphsWindow extends Window {

	private final double sizePercentage = 0.80; // Window size proportionate to main window
	private final int widthCushion = 50; //Border cushion for width of window;
	private final int heightCushion = 110; //Border cushion for height of window
	private final URL rgURL = new URL("http://demo.opennms.org/opennms/graph/index.jsp");
	private Embedded rgBrowser = new Embedded("", new ExternalResource(rgURL));
	
	public ResourceGraphsWindow (float width, float height) throws MalformedURLException{
		int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
		int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
		
		setCaption("Resource Graphs");
		setImmediate(true);
		setResizable(false);
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
		setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
		
		VerticalLayout layout = new VerticalLayout();
		rgBrowser.setType(Embedded.TYPE_BROWSER);
		rgBrowser.setWidth("" + browserWidth + "px");
		rgBrowser.setHeight("" + browserHeight + "px");
		
		layout.addComponent(rgBrowser);
		addComponent(layout);
	}
}
