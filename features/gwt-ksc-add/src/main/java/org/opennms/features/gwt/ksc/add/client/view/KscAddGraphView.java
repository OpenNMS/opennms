package org.opennms.features.gwt.ksc.add.client.view;

import java.util.List;

import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface KscAddGraphView<T> extends IsWidget {
    
    public interface Presenter<T> {
        void onAddButtonClicked();
        void onKeyCodeEvent(KeyCodeEvent<?> event, String searchText);
        void onKscReportSelected();
    }
    
    String getSearchText();
    void setPresenter(Presenter<T> presenter);
    void setDataList(List<T> dataList);
    Widget asWidget();

    String getTitle();

    KscReport getSelectedReport();
    void select(KscReport report);
    void clearSelection();

    boolean isPopupShowing();
    void hidePopup();
    void showPopup();
}
