package org.opennms.gwtterminal.client;

/*
@ITMillApache2LicenseForJavaFiles@
 */
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A regular GWT component without integration with IT Mill Toolkit.
 */
public class GwtTerm extends Composite implements HasAllKeyHandlers, ClickHandler {
    /** Currently selected color name to give client-side feedback to the user. */
    protected Label currentcolor = new Label();
    private Element pre;
    private Element termDiv;
    private Element promptSpan;
    private Element inputSpan;
    private Element sshSpan;
    
    public Element getInputSpan() {
		return inputSpan;
	}

	public void setInputSpan(Element inputSpan) {
		this.inputSpan = inputSpan;
	}

	private Element cursor;
    private FocusPanel mainPanel;
    
    public GwtTerm() {
        
        mainPanel = new FocusPanel();
        //DOM.setElementAttribute(mainPanel.getElement(), "onkeydown", "keydown(event)");
        //DOM.setElementAttribute(mainPanel.getElement(), "onkeypress", "keypress(event, false)");
        DOM.setElementAttribute(mainPanel.getElement(), "class", "focusPanel");
        pre = DOM.createElement("pre");
        DOM.setElementAttribute(pre, "class", "preContainer");
        termDiv = DOM.createDiv();
        DOM.setElementAttribute(termDiv, "class", "termDiv");
        promptSpan = DOM.createElement("span");
        DOM.setElementAttribute(promptSpan, "class", "prompt");
        promptSpan.setInnerHTML("lmbell@localhost~:");
        cursor = DOM.createElement("span");
        DOM.setElementAttribute(cursor, "class", "cursor");
        cursor.setInnerHTML(" ");
        inputSpan = DOM.createElement("span");
        DOM.setElementAttribute(inputSpan, "class", "prompt");
        DOM.setElementAttribute(inputSpan, "id", "inputSpan");
        DOM.appendChild(pre, termDiv);
        DOM.appendChild(termDiv, promptSpan);
        DOM.appendChild(termDiv, inputSpan);
        DOM.appendChild(termDiv, cursor);
        sshSpan = DOM.createElement("span");
        DOM.setElementAttribute(sshSpan, "id", "sshSpan");
        DOM.setElementAttribute(sshSpan, "class", "sshSpan");
        DOM.appendChild(mainPanel.getElement(), pre);
        DOM.appendChild(mainPanel.getElement(), sshSpan);

       
        // Set the class of the color selection feedback box to allow CSS
        // styling.
        // We need to obtain the DOM element for the current color label.
        // This assumes that the <td> element of the HorizontalPanel is
        // the parent of the label element. Notice that the element has no
        // parent
        // before the widget has been added to the horizontal panel.
        final Element panelcell = DOM.getParent(currentcolor.getElement());
        DOM.setElementProperty(panelcell, "className",
                               "colorpicker-currentcolorbox");
        // Set initial color. This will be overridden with the value read from
        // server.
        setColor("white");
        // Composite GWT widgets must call initWidget().
        initWidget(mainPanel);
    }

    /** Sets the currently selected color. */
    public void setColor(String newcolor) {
        // Give client-side feedback by changing the color name in the label
        currentcolor.setText(newcolor);
        // Obtain the DOM elements. This assumes that the <td> element
        // of the HorizontalPanel is the parent of the label element.
        final Element nameelement = currentcolor.getElement();
        final Element cell        = DOM.getParent(nameelement);
        // Give feedback by changing the background color
        DOM.setStyleAttribute(cell,        "background", newcolor);
        DOM.setStyleAttribute(nameelement, "background", newcolor);
        if ("black navy maroon blue purple".indexOf(newcolor) != -1)
            DOM.setStyleAttribute(nameelement, "color", "white");
        else
            DOM.setStyleAttribute(nameelement, "color", "black");
    }
	
    /** Handles click on a color button. */
    @Override
	public void onClick(ClickEvent event) {
    	// Use the button label as the color name to set
    	Button clickedButton = (Button) event.getSource();
    	Window.alert("BUTTON PRESSED");
    	setColor(clickedButton.getText());
	}

	@Override
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return mainPanel.addKeyUpHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return mainPanel.addKeyDownHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return mainPanel.addKeyPressHandler(handler);
	}
}