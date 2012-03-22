package org.opennms.features.gwt.ksc.add.client.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphView;
import org.opennms.features.gwt.ksc.add.client.view.KscReport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

public class KscAddGraphPresenter implements Presenter, KscAddGraphView.Presenter<KscReport> {

    private static final List<KscReport> EMPTY_KSCREPORT_LIST = Collections.unmodifiableList(new ArrayList<KscReport>());
    private KscAddGraphView<KscReport> m_view;
    private List<KscReport> m_KscReports;
    private String m_reportName;
    private String m_resourceId;
    private PopupPanel m_mainPopup;
    private final Image m_addImage;
    
    public KscAddGraphPresenter(final PopupPanel mainPopup, final KscAddGraphView<KscReport> addGraphView, final List<KscReport> kscReports, final String reportName, final String resourceId) {
        m_mainPopup = mainPopup;
        m_view = addGraphView;
        m_view.setPresenter(this);
        m_KscReports = kscReports;
        m_reportName = reportName;
        m_resourceId = resourceId;
        
        m_addImage = new Image("plus.gif");
        m_addImage.setAltText("Add this graph to a KSC report.");
        m_addImage.setTitle("Add this graph to a KSC report.");
    }

    private List<KscReport> filterResultsByName(final String searchText) {
        final List<KscReport> list = new ArrayList<KscReport>();
        for (final KscReport detail : m_KscReports) {
            if (detail.getLabel().toLowerCase().contains(searchText.toLowerCase())) {
                list.add(detail);
            }
        }
        
        return list;
    }

    @Override
    public void onKeyCodeEvent(final KeyCodeEvent<?> event, final String searchText) {
        final int keyCode = event.getNativeEvent().getKeyCode();
        GWT.log("associated type = " + event.getAssociatedType());

        if (keyCode == KeyCodes.KEY_ESCAPE) {
            GWT.log("escape, hiding results");
            m_view.hidePopup();
        } else if (keyCode == KeyCodes.KEY_BACKSPACE && searchText.length() == 0) {
            m_view.hidePopup();
            m_view.setDataList(EMPTY_KSCREPORT_LIST);
            m_view.clearSelection();
        } else if (keyCode == KeyCodes.KEY_ENTER && m_view.getSelectedReport() != null && m_view.getTitle() != null && event instanceof KeyDownEvent && !m_view.isPopupShowing()) {
            onAddButtonClicked();
        } else {
            if (searchText.length() == 0) {
                GWT.log("search text is empty");
                m_view.setDataList(EMPTY_KSCREPORT_LIST);
            } else {
                GWT.log("search text is not empty");
                final List<KscReport> results = filterResultsByName(searchText);
                if (keyCode == KeyCodes.KEY_ENTER && results.size() == 1) {
                    m_view.hidePopup();
                    m_view.select(results.get(0));
                } else {
                    m_view.setDataList(results);
                    m_view.showPopup();
                    m_view.clearSelection();
                }
            }
        }
    }

    @Override
    public void onKscReportSelected() {
        GWT.log("selected report " + m_view.getSelectedReport().getId());
        /*
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseHref() + "KSC/customView.htm");
        urlBuilder.append("?type=custom");
        urlBuilder.append("&report=" + m_view.getSelectedReport().getId());
        Location.assign(urlBuilder.toString());
        */
    }

    @Override
    public void onAddButtonClicked() {
        GWT.log("adding resource '" + m_resourceId + "' from graph report '" + m_reportName + "' to KSC report '" + m_view.getSelectedReport().getLabel() + "' with title '" + m_view.getTitle() + "'");
    }

    @Override
    public void go(final HasWidgets container) {

        m_addImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (m_mainPopup.isShowing()) {
                    m_mainPopup.hide();
                } else {
                    m_mainPopup.setPopupPositionAndShow(new PositionCallback() {
                        @Override
                        public void setPosition(final int offsetWidth, final int offsetHeight) {
                            final int[] positions = calculateMainPopupPosition();
                            m_mainPopup.setPopupPosition(positions[0], positions[1]);
                        }
                    });
                }
            }
        });

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(final ResizeEvent event) {
                final int[] positions = calculateMainPopupPosition();
                m_mainPopup.setPopupPosition(positions[0], positions[1]);
            }
        });

        container.clear();
        container.add(m_addImage);
        container.add(m_mainPopup.asWidget());
    }
    
    public native final String getBaseHref() /*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
    }-*/;

    private int[] calculateMainPopupPosition() {
        final int[] positions = {0, 0};

        final int windowWidth = Window.getClientWidth();
        final int imageRightEdge = m_addImage.getAbsoluteLeft() + m_addImage.getWidth();

        if (imageRightEdge + 300 > windowWidth) {
            positions[0] = windowWidth - 320;
        } else {
            positions[0] = imageRightEdge - 3;
        }
        if (positions[0] < 0) positions[0] = 0;

        positions[1] = m_addImage.getAbsoluteTop() + m_addImage.getHeight() - 1;

        return positions;
    }

}
