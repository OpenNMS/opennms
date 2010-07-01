/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: April 14, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.etable;

import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.view.html.TwoColumnRowLayout;
import org.extremecomponents.util.HtmlBuilder;

/**
 * A stupidly simple class that extends TwoColumnRowLayout but allows us to
 * override the bit of code that creates the table.  We need to do this so we
 * can customize its CSS class.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see TwoColumnRowLayout
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class CustomizableTwoColumnRowLayout extends TwoColumnRowLayout {
    /**
     * <p>Constructor for CustomizableTwoColumnRowLayout.</p>
     *
     * @param html a {@link org.extremecomponents.util.HtmlBuilder} object.
     * @param model a {@link org.extremecomponents.table.core.TableModel} object.
     */
    public CustomizableTwoColumnRowLayout(HtmlBuilder html, TableModel model) {
        super(html, model);
    }

    /** {@inheritDoc} */
    @Override
    public void layout() {
        TableModel model = getTableModel();
        HtmlBuilder html = getHtmlBuilder();
        
        if (!showLayout(model)) {
            return;
        }

        html.tr(1).style("padding: 0px;").close();

        html.td(2).colSpan(String.valueOf(model.getColumnHandler().columnCount())).close();

        startTable(html);
        html.tr(3).close();

        // layout area left
        columnLeft(html, model);

        // layout area right
        columnRight(html, model);

        html.trEnd(3);
        html.tableEnd(2);
        html.newline();
        html.tabs(2);

        html.tdEnd();
        html.trEnd(1);
        html.tabs(2);
        html.newline();
    }

    /**
     * <p>startTable</p>
     *
     * @param html a {@link org.extremecomponents.util.HtmlBuilder} object.
     * @return a {@link org.extremecomponents.util.HtmlBuilder} object.
     */
    protected HtmlBuilder startTable(HtmlBuilder html) {
        return html.table(2).border("0").cellPadding("0").cellSpacing("0").width("100%").close();
    }
}
