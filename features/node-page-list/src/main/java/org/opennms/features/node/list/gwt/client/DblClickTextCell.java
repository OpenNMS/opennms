package org.opennms.features.node.list.gwt.client;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

public class DblClickTextCell extends AbstractSafeHtmlCell<String> {

    public DblClickTextCell() {
        super(SimpleSafeHtmlRenderer.getInstance(), "dblclick");
    }

    @Override
    protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.append(value);
        }
    }

    

}
