package org.opennms.features.node.list.gwt.client;

import java.util.Date;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class ErrorDialogBox extends PopupPanel {
    
    private final Label m_errorLabel = new Label();
    private final HTML m_caption = new HTML();
    private DockLayoutPanel m_layoutPanel;
    
    public ErrorDialogBox() {
        
        setModal(false);
        
        //setText("Error");
        // Enable animation.
        setAnimationEnabled(true);
        setWidget(createWidget());
        
    }

    private Widget createWidget() {
        m_errorLabel.setText("hello");
        Button ok = new Button("OK");
        ok.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
              ErrorDialogBox.this.hide();
          }
        });
        
        final CheckBox check = new CheckBox();
        check.setText("Don't show again for 24hours");
        check.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(check.getValue()) {
                    Cookies.setCookie(PageableNodeList.COOKIE, "true", new Date(new Date().getTime() + 86400000));
                }
                
            }
        });
        
        
        m_layoutPanel = new DockLayoutPanel(Unit.EM);
        m_layoutPanel.setWidth("100%");
        m_layoutPanel.setHeight("100%");
        
        m_caption.getElement().getStyle().setBackgroundColor("#ebebeb");
        m_caption.setText("Error");
        m_layoutPanel.addNorth(m_caption, 2);
        m_layoutPanel.addSouth(ok, 2);
        m_layoutPanel.addSouth(check, 2);
        
        m_layoutPanel.add(m_errorLabel);
        return m_layoutPanel;
    }

    public void setErrorMessageAndShow(String errorMsg) {
        m_errorLabel.setText(errorMsg);
        m_errorLabel.setHeight("100%");
        setHeight((52 + m_caption.getOffsetHeight() + 52) + "px");
        show();
    }
    
}
