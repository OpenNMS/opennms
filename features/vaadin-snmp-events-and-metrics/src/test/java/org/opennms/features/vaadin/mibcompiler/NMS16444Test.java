/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.mibcompiler;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Test;
import org.opennms.features.timeformat.api.TimeformatService;

import com.vaadin.v7.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class NMS16444Test {

    @Test
    public void testLogEncoding() {
        final MibConsolePanel mibConsolePanel = new MibConsolePanel(new TimeformatService() {
            @Override
            public String format(Instant instant, ZoneId zoneId) {
                return "TIME";
            }

            @Override
            public String format(Date date, ZoneId zoneId) {
                return "TIME";
            }

            @Override
            public String getFormatPattern() {
                return "TIME";
            }
        });

        mibConsolePanel.debug("debug<img src='foobar'/>Something");
        assertEquals("debug&lt;img src='foobar'/&gt;Something", getLastLabelMessage(mibConsolePanel));

        mibConsolePanel.info("info<img src='foobar'/>Something");
        assertEquals("info&lt;img src='foobar'/&gt;Something", getLastLabelMessage(mibConsolePanel));

        mibConsolePanel.warn("warn<img src='foobar'/>Something");
        assertEquals("warn&lt;img src='foobar'/&gt;Something", getLastLabelMessage(mibConsolePanel));

        mibConsolePanel.error("error<img src='foobar'/>Something");
        assertEquals("error&lt;img src='foobar'/&gt;Something", getLastLabelMessage(mibConsolePanel));
    }

    private String getLastLabelMessage(final MibConsolePanel mibConsolePanel) {
        final VerticalLayout verticalLayout = getLogContent(mibConsolePanel);
        final Label label = (Label) verticalLayout.getComponent(verticalLayout.getComponentCount() - 1);
        final String messageOnly = label.getValue().replaceAll("TIME<b>.*</b>", "");
        return messageOnly;
    }

    private VerticalLayout getLogContent(final MibConsolePanel mibConsolePanel) {
        final VerticalLayout content = ((VerticalLayout) mibConsolePanel.getContent());
        for (int i = 0; i < content.getComponentCount(); i++) {
            if (content.getComponent(i) instanceof VerticalLayout) {
                return (VerticalLayout) content.getComponent(i);
            }
        }
        return null;
    }
}
