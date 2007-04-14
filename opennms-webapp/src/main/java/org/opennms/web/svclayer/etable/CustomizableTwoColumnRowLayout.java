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
 */
public abstract class CustomizableTwoColumnRowLayout extends TwoColumnRowLayout {
    public CustomizableTwoColumnRowLayout(HtmlBuilder html, TableModel model) {
        super(html, model);
    }

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

    protected HtmlBuilder startTable(HtmlBuilder html) {
        return html.table(2).border("0").cellPadding("0").cellSpacing("0").width("100%").close();
    }
}
