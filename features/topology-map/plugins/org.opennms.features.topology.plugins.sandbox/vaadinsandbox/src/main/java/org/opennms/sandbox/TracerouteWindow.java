package org.opennms.sandbox;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

/**
 * The TracerouteWindow class creates a Vaadin Sub-window with a form and results section
 * for the Traceroute functionality of a Node.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class TracerouteWindow extends Window{

	private final double sizePercentage = 0.80; // Window size proportionate to main window
    private NativeSelect ipDropdown = null; //Dropdown component for IP Address
    private Label nodeLabel = null; //Label displaying the name of the Node at the top of the window
    private TextField forcedHopField = null; //Textfield for the "Forced Hop" variable
    private CheckBox numericalDataCheckBox = null; //Checkbox which toggles numeric output
    private Embedded resultsBrowser = null; //Browser component which displays the results of Traceroute
    private VerticalLayout topLayout = null; //Contains the form for Traceroute
    private VerticalLayout bottomLayout = null; //Contains the results Browser for a Traceroute
    private VerticalSplitPanel vSplit = null; //Splits up the top and bottom layout
    private int margin = 40; //Padding around the results browser
    private int splitHeight = 180;//Height from top of the window to the split location in pixels
    private int topHeight = 220;//Set height size for everything above the split
    
    /**
     * The TracerouteWindow method constructs a TracerouteWindow component with a size proportionate to the 
     * width and height of the main window.
     * @param node 
     * @param width Width of Main window
     * @param height Height of Main window
     */
    public TracerouteWindow (Node node, float width, float height){
        
    	/*Sets up window settings*/
    	setCaption("Traceroute - " + node.getName());
        setImmediate(true);
        setResizable(false);
        int windowWidth = (int)(sizePercentage * width), windowHeight = (int)(sizePercentage * height);
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
        setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
        

        /*Initialize the header of the Sub-window with the name of the selected Node*/
        String nodeName = "<div style=\"text-align: center; font-size: 18pt; font-weight:bold;\">" + node.getName() + "</div>";
        nodeLabel = new Label(nodeName);
        nodeLabel.setContentMode(Label.CONTENT_XHTML);
        
        /*Creating various layouts to encapsulate all of the components*/
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        vSplit = new VerticalSplitPanel();
        topLayout = new VerticalLayout();
        bottomLayout = new VerticalLayout();
        VerticalLayout form = new VerticalLayout();
        GridLayout grid = new GridLayout(2,2);
        grid.setWidth("420");
        grid.setHeight("62");
        
        /*Sets up IP Address dropdown with the Name as default*/
        ipDropdown = new NativeSelect();
        ipDropdown.addItem(node.getIPAddress());
        ipDropdown.select(node.getIPAddress());
        
        /*Creates the Numerical Output Check box and sets up the listener*/
        numericalDataCheckBox = new CheckBox("Use Numerical Node Names");
        numericalDataCheckBox.setImmediate(true);
        numericalDataCheckBox.setValue(false);
        
        /*Creates the form labels and text fields*/
        Label ipLabel = new Label("IP Address: ");
        Label forcedHopLabel = new Label("Forced Hop IP: ");
        forcedHopField = new TextField();
     
        /*Add all of the components to the GridLayout*/
        grid.addComponent(ipLabel);
        grid.setComponentAlignment(ipLabel, Alignment.MIDDLE_LEFT);
        grid.addComponent(ipDropdown);
        grid.setComponentAlignment(ipDropdown, Alignment.MIDDLE_LEFT);
        grid.addComponent(forcedHopLabel);
        grid.setComponentAlignment(forcedHopLabel, Alignment.MIDDLE_LEFT);
        grid.addComponent(forcedHopField);
        grid.setComponentAlignment(forcedHopField, Alignment.MIDDLE_LEFT);
        
        /*Creates the Ping button and sets up the listener*/
        final Button tracerouteButton = new Button("Traceroute"); 
        tracerouteButton.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
					changeBrowserURL(buildURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
            }
        }); 
        
        /*Adds components to the form and sets the width and spacing*/
        form.addComponent(grid);
        form.addComponent(numericalDataCheckBox);
        form.addComponent(tracerouteButton);
        form.setWidth("100%");
        form.setSpacing(true);
        
        /*Adds components to the Top Layout and sets the width and margins*/
        topLayout.addComponent(nodeLabel);
        topLayout.setComponentAlignment(nodeLabel, Alignment.MIDDLE_CENTER);
        topLayout.addComponent(form);
        topLayout.setSizeFull();
        topLayout.setMargin(true, true, false, true);
        
        /*Adds components to the Bottom Layout and sets the width and margins*/
        bottomLayout.setSizeFull();
        bottomLayout.setMargin(true);
        bottomLayout.setImmediate(true);
        
        buildEmbeddedBrowser();
        
        /*Setting first and second components for the split panel and setting the panel divider position*/
        vSplit.setFirstComponent(topLayout);
        vSplit.setSecondComponent(bottomLayout);
        vSplit.setSplitPosition(splitHeight, UNITS_PIXELS);
        vSplit.setLocked(true);
        
        /*Adds split panel to the main layout and expands the split panel to 100% of the layout space*/
        mainLayout.addComponent(vSplit);
        mainLayout.setExpandRatio(vSplit, 1);
        
        setContent(mainLayout);
    }
    
    /**
	 * The changeBrowserURL method changes the address of the results browser whenever a new
	 * traceroute request form is submitted and refreshes the browser.
	 * @param url New web address
	 */
    private void changeBrowserURL(URL url) {
		resultsBrowser.setVisible(false);   // This setVisible(false/true) toggle is used to refresh the browser.
                                                    // Due to to the fact that the updates to the client require a call to
                                                    // the server, this is currently one of the only ways to accomplish the 
                                                    // the needed update.
		resultsBrowser.setSource(new ExternalResource(url));
		resultsBrowser.setVisible(true);
	}

    /**
	 * The buildURL method takes the current values in the form and formats them into the
	 * URL string that is used to redirect the results browser to the Traceroute page.
	 * @return Web address for traceroute command with submitted parameters
	 * @throws MalformedURLException
	 */
	private URL buildURL() throws MalformedURLException {
		String base = "http://demo.opennms.org/opennms/ExecCommand.map?command=traceroute";
		String options = base;
		options += "&address=" + ipDropdown.getValue().toString();
		options += "&hopAddress=" + forcedHopField.getValue().toString();
		if (numericalDataCheckBox.getValue().equals(true))
			options += "&numericOutput=true";
		return new URL(options);
	}
	
	/**
	 * The buildEmbeddedBrowser method creates a new browser instance and adds it to the 
	 * bottom layout. The browser is set to invisible by default.
	 */
	private void buildEmbeddedBrowser() {
		resultsBrowser = new Embedded();
		resultsBrowser.setType(Embedded.TYPE_BROWSER);
		resultsBrowser.setWidth("" + (int)(this.getWidth()-margin) + "px"); //Cuts off "close" button from window
		resultsBrowser.setHeight("" + (int)(this.getHeight() - topHeight - margin) + "px");
		resultsBrowser.setImmediate(true);
		resultsBrowser.setVisible(false);
		bottomLayout.addComponent(resultsBrowser);
	}
 
}
