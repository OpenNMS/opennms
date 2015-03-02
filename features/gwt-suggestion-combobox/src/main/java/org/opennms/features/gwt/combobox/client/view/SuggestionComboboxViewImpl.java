/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.combobox.client.view;

import java.util.List;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;

public class SuggestionComboboxViewImpl extends Composite implements SuggestionComboboxView<NodeDetail> {
    
    public class NodeDetailCell extends AbstractSafeHtmlCell<NodeDetail> {

        public NodeDetailCell() {
            super(new SafeHtmlRenderer<NodeDetail>() {

                @Override
                public SafeHtml render(final NodeDetail nodeDetail) {
                    
                    return new SafeHtml() {

                        private static final long serialVersionUID = 4989123499072122653L;

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
        m_nodeList.getElement().getStyle().setFontSize(12, Unit.PX);
        m_nodeList.setSelectionModel(m_selectionModel);

        m_dataList = new ListDataProvider<NodeDetail>();
        m_dataList.addDataDisplay(m_nodeList);
        
        m_pager = new SimplePager();
        m_pager.setStyleName("onms-table-no-borders-margin");
        m_pager.getElement().getStyle().setWidth(100, Unit.PCT);
        m_pager.setDisplay(m_nodeList);
        
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(m_nodeList);
        flowPanel.add(m_pager);
        m_popupPanel = new PopupPanel();
        m_popupPanel.add(flowPanel);
        m_popupPanel.setAutoHideEnabled(true);
        m_popupPanel.setAnimationEnabled(true);
        m_popupPanel.setModal(false);
        m_popupPanel.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_popupPanel.getElement().getStyle().setBorderColor("#B5B8C8");
        m_popupPanel.getElement().getStyle().setPadding(1, Unit.PX);
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
                m_popupPanel.setWidth((getOffsetWidth() - 5) + "px");
                m_popupPanel.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + 24);
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
