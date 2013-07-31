/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler;

import java.util.Date;

import org.slf4j.LoggerFactory;
import org.opennms.features.vaadin.api.Logger;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Runo;

/**
 * The Class MIB Console Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibConsolePanel extends Panel implements Logger {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MibConsolePanel.class);

    /** The Constant ERROR. */
    private static final String ERROR = "<b><font color='red'>&nbsp;[ERROR]&nbsp;</font></b>";

    /** The Constant WARN. */
    private static final String WARN  = "<b><font color='orange'>&nbsp;[WARN]&nbsp;</font></b>";

    /** The Constant INFO. */
    private static final String INFO  = "<b><font color='green'>&nbsp;[INFO]&nbsp;</font></b>";

    /** The Constant DEBUG. */
    private static final String DEBUG  = "<b><font color='gray'>&nbsp;[DEBUG]&nbsp;</font></b>";

    /** The log content. */
    private final VerticalLayout logContent;

    /** The clear button. */
    private final Button clearButton;

    /**
     * Instantiates a new MIB Console Panel.
     */
    public MibConsolePanel() {
        super("MIB Console");
        addStyleName(Runo.PANEL_LIGHT);

        clearButton = new Button("Clear Log");
        clearButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                logContent.removeAllComponents();
            }
        });

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(clearButton);
        layout.setComponentAlignment(clearButton, Alignment.TOP_RIGHT);

        logContent = new VerticalLayout();
        layout.addComponent(logContent);

        setSizeFull();

        setContent(layout);
    }

    /**
     * Log Message.
     *
     * @param level the level
     * @param message the message
     */
    private void logMsg(String level, String message) {
        String msg = new Date().toString() + level + message;
        Label error = new Label(msg, ContentMode.HTML);
        logContent.addComponent(error);
        scrollIntoView();
        LOG.info(message);
    }

    /**
     * Scroll into view.
     */
    private void scrollIntoView() {
        final VerticalLayout layout = (VerticalLayout) getContent();
        if (getUI() != null && layout.getComponentCount() > 0)
            getUI().scrollIntoView(layout.getComponent(layout.getComponentCount() - 1));
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#error(java.lang.String)
     */
    @Override
    public void error(String message) {
        logMsg(ERROR, message);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#warn(java.lang.String)
     */
    @Override
    public void warn(String message) {
        logMsg(WARN, message);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#info(java.lang.String)
     */
    @Override
    public void info(String message) {
        logMsg(INFO, message);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
        logMsg(DEBUG, message);
    }

}
