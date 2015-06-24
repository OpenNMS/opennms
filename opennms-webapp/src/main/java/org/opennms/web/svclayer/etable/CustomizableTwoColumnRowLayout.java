/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
