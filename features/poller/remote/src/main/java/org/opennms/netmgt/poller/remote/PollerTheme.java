/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import java.awt.Color;
import java.io.Serializable;
import java.net.URL;

public class PollerTheme implements Serializable {
    private static final long serialVersionUID = 1L;

    private String m_title;
    private URL m_image;
    private Color m_foregroundColor;
    private Color m_backgroundColor;
    private Color m_detailColor;

    public PollerTheme() {
    }

    public PollerTheme(final String title, final URL image, final Color foreground, final Color background, final Color detail) {
        m_title = title;
        m_image = image;
        m_foregroundColor = foreground;
        m_backgroundColor = background;
        m_detailColor = detail;
    }

    public String getTitle() {
        return m_title;
    }

    public URL getImage() {
        return m_image;
    }

    public Color getForegroundColor() {
        return m_foregroundColor;
    }

    public Color getBackgroundColor() {
        return m_backgroundColor;
    }

    public Color getDetailColor() {
        return m_detailColor;
    }

    @Override
    public String toString() {
        return "PollerTheme [title=" + m_title + ", image=" + m_image + ", foregroundColor=" + m_foregroundColor + ", backgroundColor=" + m_backgroundColor + ", detailColor=" + m_detailColor + "]";
    }
}
