/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.mibcompiler;

import java.time.Instant;

import org.apache.commons.lang.StringEscapeUtils;
import org.opennms.features.timeformat.api.TimeformatService;
import org.opennms.vaadin.user.UserTimeZoneExtractor;
import org.slf4j.LoggerFactory;
import org.opennms.features.vaadin.api.Logger;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * The Class MIB Console Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibConsolePanel extends Panel implements Logger {

    /** The Constant LOG. */
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

    private final TimeformatService timeformatService;

    /**
     * Instantiates a new MIB Console Panel.
     */
    public MibConsolePanel(TimeformatService timeformatService) {
        super("MIB Console");
        this.timeformatService = timeformatService;
        addStyleName("light");

        Button clearButton = new Button("Clear Log");
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
        String msg = timeformatService.format(Instant.now(), UserTimeZoneExtractor.extractUserTimeZoneIdOrNull(getUI())) + level + StringEscapeUtils.escapeHtml(message);
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
