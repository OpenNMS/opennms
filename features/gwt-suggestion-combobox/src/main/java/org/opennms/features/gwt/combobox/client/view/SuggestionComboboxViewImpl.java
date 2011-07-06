package org.opennms.features.gwt.combobox.client.view;

import java.util.List;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class SuggestionComboboxViewImpl extends Composite implements SuggestionComboboxView<NodeDetail> {
    
    public class NodeDetailCell extends AbstractSafeHtmlCell<NodeDetail> {

        public NodeDetailCell() {
            super(new SafeHtmlRenderer<NodeDetail>() {

                @Override
                public SafeHtml render(final NodeDetail nodeDetail) {
                    
                    return new SafeHtml() {

                        @Override
                        public String asString() {
                            return nodeDetail.getLabel();
                        }
                    };
                }

                @Override
                public void render(NodeDetail nodeDetail, SafeHtmlBuilder builder) {
                    builder.appendEscaped(nodeDetail.getLabel());
                }
            });
        }

        @Override
        protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
            sb.append(value);
        }
    }

    private static SuggestionComboboxViewUiBinder uiBinder = GWT.create(SuggestionComboboxViewUiBinder.class);

    @UiTemplate("SuggestionComboboxView.ui.xml")
    interface SuggestionComboboxViewUiBinder extends UiBinder<Widget, SuggestionComboboxViewImpl> {}
    
    @UiField
    LayoutPanel m_layoutPanel;
    
    @UiField
    TextBox m_textBox;
    
    @UiField
    Button m_goButton;
    
    
    CellList<NodeDetail> m_nodeList;
    SimplePager m_pager;
    ListDataProvider<NodeDetail> m_dataList;
    PopupPanel m_popupPanel;

    private Presenter<NodeDetail> m_presenter;

    private SingleSelectionModel<NodeDetail> m_selectionModel;
    
    public SuggestionComboboxViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        m_layoutPanel.setSize("100%", "21px");
        m_textBox.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    if(m_presenter != null) {
                        m_presenter.onEnterKeyEvent();
                    }
                }
            }
        });
        
        m_selectionModel = new SingleSelectionModel<NodeDetail>();
        m_selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                m_presenter.onNodeSelected();
            }
        });
        
        m_nodeList = new CellList<NodeDetail>(new NodeDetailCell());
        m_nodeList.setPageSize(10);
        m_nodeList.setSelectionModel(m_selectionModel);

        m_dataList = new ListDataProvider<NodeDetail>();
        m_dataList.addDataDisplay(m_nodeList);
        
        m_pager = new SimplePager();
        m_pager.setDisplay(m_nodeList);
        
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(m_nodeList);
        flowPanel.add(m_pager);
        m_popupPanel = new PopupPanel();
        m_popupPanel.add(flowPanel);
        m_popupPanel.setAutoHideEnabled(true);
        m_popupPanel.setAnimationEnabled(true);
        m_popupPanel.setModal(true);
    }
    
    @Override
    public void setPresenter(Presenter<NodeDetail> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void setData(List<NodeDetail> dataList) {
        m_dataList.setList(dataList);
        
        m_popupPanel.setPopupPositionAndShow(new PositionCallback() {
            
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                m_popupPanel.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + 21);
            }
        });
    }
    
    @UiHandler("m_goButton")
    void onGoButtonClickHandler(ClickEvent event) {
        if(m_presenter != null) {
            m_presenter.onGoButtonClicked();
        }
    }

    @Override
    public String getSelectedText() {
        return m_textBox.getText();
    }

    @Override
    public NodeDetail getSelectedNode() {
        return m_selectionModel.getSelectedObject();
    }
    
}
