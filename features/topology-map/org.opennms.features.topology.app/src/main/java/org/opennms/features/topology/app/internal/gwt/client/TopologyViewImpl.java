package org.opennms.features.topology.app.internal.gwt.client;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TopologyViewImpl extends Composite implements TopologyView<TopologyViewRenderer> {

    private static TopologyViewImplUiBinder uiBinder = GWT.create(TopologyViewImplUiBinder.class);

    interface TopologyViewImplUiBinder extends
            UiBinder<Widget, TopologyViewImpl> {
    }
    
    private Presenter<TopologyViewRenderer> m_presenter;
    private TopologyViewRenderer m_viewRenderer;
    
    @UiField
    Element m_svg;

    @UiField
    Element m_svgViewPort;

    @UiField
    Element m_edgeGroup;

    @UiField
    Element m_vertexGroup;

    @UiField
    Element m_referenceMap;

    @UiField
    Element m_referenceMapViewport;

    @UiField
    Element m_referenceMapBorder;
    
    @UiField
    Element m_marquee;

    public TopologyViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
    }


    @Override
    public void setPresenter(Presenter<TopologyViewRenderer> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void setViewRenderer(TopologyViewRenderer viewRenderer) {
        m_viewRenderer = viewRenderer;
    }
    

}
