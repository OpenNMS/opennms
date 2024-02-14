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
