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
