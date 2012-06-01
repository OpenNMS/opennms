package org.opennms.sandbox;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
    private Node testNode = null; //Node object containing all of its relative information.
    private Label nodeLabel = null; //Label displaying the name of the Node at the top of the window
    private TextField forcedHopField = null;
    private CheckBox numericalDataCheckBox = null;
    private Embedded resultsBrowser = null;
    private VerticalLayout topLayout = null;
    private VerticalLayout bottomLayout = null;
    private VerticalSplitPanel vSplit = null;
    private int margin = 40;
    private int splitPercentage = 25;
    
    public TracerouteWindow (Node testNode, float width, float height){
        
    	/*Sets up window settings*/
        this.testNode = testNode;
    	setCaption("Traceroute");
        setImmediate(true);
        setResizable(false);
        int windowWidth = (int)(sizePercentage * width), windowHeight = (int)(sizePercentage * height);
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
        setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
        

        /*Initialize the header of the Sub-window with the name of the selected Node*/
        String nodeName = "<div style=\"text-align: center; font-size: 18pt; font-weight:bold;\">" + testNode.getName() + "</div>";
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
        ipDropdown.addItem(this.testNode.getIPAddress());
        ipDropdown.select(this.testNode.getIPAddress());
        
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
        vSplit.setSplitPosition(splitPercentage, UNITS_PERCENTAGE);
        vSplit.setLocked(true);
        
        /*Adds split panel to the main layout and expands the split panel to 100% of the layout space*/
        mainLayout.addComponent(vSplit);
        mainLayout.setExpandRatio(vSplit, 1);
        
        setContent(mainLayout);
    }
    
    private void changeBrowserURL(URL url) {
		resultsBrowser.setVisible(false);
		resultsBrowser.setSource(new ExternalResource(url));
		resultsBrowser.setVisible(true);
	}

	private URL buildURL() throws MalformedURLException {
		String base = "http://demo.opennms.org/opennms/ExecCommand.map?command=traceroute";
		String options = base;
		options += "&address=" + ipDropdown.getValue().toString();
		options += "&hopAddress=" + forcedHopField.getValue().toString();
		if (numericalDataCheckBox.getValue().equals(true))
			options += "&numericOutput=true";
		return new URL(options);
	}
	
	private void buildEmbeddedBrowser() {
		resultsBrowser = new Embedded();
		resultsBrowser.setType(Embedded.TYPE_BROWSER);
		resultsBrowser.setWidth("" + (int)(this.getWidth()+margin) + "px"); //Cuts off "close" button from window
		resultsBrowser.setHeight("" + (int)((this.getHeight())*((100-splitPercentage)/100)-margin) + "px");
		resultsBrowser.setImmediate(true);
		resultsBrowser.setVisible(false);
		bottomLayout.addComponent(resultsBrowser);
	}
 
}
