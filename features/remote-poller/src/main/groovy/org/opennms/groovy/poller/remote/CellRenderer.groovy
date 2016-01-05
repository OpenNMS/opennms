/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.groovy.poller.remote;

import java.awt.Color
import java.awt.Component

import javax.swing.DefaultListCellRenderer
import javax.swing.JList

public class CellRenderer extends DefaultListCellRenderer {
    def m_detailColor
    def m_foregroundColor
    def m_backgroundColor

    public CellRenderer(final Color detailColor, final Color foregroundColor, final Color backgroundColor) {
        m_detailColor = detailColor
        m_foregroundColor = foregroundColor
        m_backgroundColor = backgroundColor
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        setText(value.toString());

        if (isSelected) {
            setBackground(m_detailColor)
            setForeground(m_backgroundColor)
        } else {
            setBackground(m_backgroundColor)
            setForeground(m_foregroundColor)
        }

        //return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        return this
    }

}
