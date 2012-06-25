package org.opennms.term.client;

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
    private Element cursor;
    private FocusPanel mainPanel;
    
    public GwtTerm() {
        // Create a 4x4 grid of buttons with names for 16 colors
        final Grid grid = new Grid(4, 4);
        final String[] colors = new String[] { "aqua", "black", "blue",
                "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive",
                "purple", "red", "silver", "teal", "white", "yellow" };
        int colornum = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++, colornum++) {
                // Create a button for each color
                final Button button = new Button(colors[colornum]);
                button.addClickHandler(this);
                // Put the button in the Grid layout
                grid.setWidget(i, j, button);
                // Set the button background colors.
                DOM.setStyleAttribute(button.getElement(), "background",
                                      colors[colornum]);
                // For dark colors, the button label must be in white.
                if ("black navy maroon blue purple".indexOf(colors[colornum]) != -1)
                    DOM.setStyleAttribute(button.getElement(), "color", "white");
            }
        }
        // Create a panel with the color grid and currently selected color
        // indicator
        
        mainPanel = new FocusPanel();
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
        DOM.appendChild(mainPanel.getElement(), pre);

       
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