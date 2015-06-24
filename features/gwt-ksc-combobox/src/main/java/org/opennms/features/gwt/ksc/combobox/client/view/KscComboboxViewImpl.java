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

package org.opennms.features.gwt.ksc.combobox.client.view;

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

public class KscComboboxViewImpl extends Composite implements KscComboboxView<KscReportDetail> {

    public class KscReportDetailCell extends AbstractSafeHtmlCell<KscReportDetail> {

        public KscReportDetailCell() {
            super(new SafeHtmlRenderer<KscReportDetail>() {

                @Override
                public SafeHtml render(final KscReportDetail reportDetail) {
                    return new SafeHtml() {

                        private static final long serialVersionUID = -5363136937302598352L;

                        @Override
                        public String asString() {
                            // TODO Auto-generated method stub
                            return reportDetail.getLabel();
                        }
                        
                    };
                }

                @Override
                public void render(KscReportDetail reportDetail,SafeHtmlBuilder builder) {
                    builder.appendEscaped(reportDetail.getLabel());
                }
            });
        }

        @Override
        protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
            sb.append(value);
        }

    }

    private static KscComboboxViewImplUiBinder uiBinder = GWT.create(KscComboboxViewImplUiBinder.class);
    
    @UiTemplate("KscComboboxViewImpl.ui.xml")
    interface KscComboboxViewImplUiBinder extends UiBinder<Widget, KscComboboxViewImpl> {}
    
    @UiField
    TextBox m_textBox;
    
    @UiField
    Button m_searchButton;
    
    CellList<KscReportDetail> m_reportList;
    SimplePager m_pager;
    PopupPanel m_popupPanel;
    
    private Presenter<KscReportDetail> m_presenter;
    private ListDataProvider<KscReportDetail> m_dataList;

    private SingleSelectionModel<KscReportDetail> m_selectionModel;
    

    public KscComboboxViewImpl() {
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
        
        m_selectionModel = new SingleSelectionModel<KscReportDetail>();
        m_selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                m_presenter.onKscReportSelected();
            }
        });
        
        m_reportList = new CellList<KscReportDetail>(new KscReportDetailCell());
        m_reportList.setPageSize(10);
        m_reportList.getElement().getStyle().setFontSize(12, Unit.PX);
        m_reportList.setSelectionModel(m_selectionModel);

        m_dataList = new ListDataProvider<KscReportDetail>();
        m_dataList.addDataDisplay(m_reportList);
        
        m_pager = new SimplePager();
        m_pager.setStyleName("onms-table-no-borders-margin");
        m_pager.getElement().getStyle().setWidth(100, Unit.PCT);
        m_pager.setDisplay(m_reportList);
        
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(m_reportList);
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
    public String getSearchText() {
        return m_textBox.getText();
    }

    @Override
    public void setPresenter(Presenter<KscReportDetail> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void setDataList(List<KscReportDetail> dataList) {
        m_dataList.setList(dataList);
        
        m_popupPanel.setPopupPositionAndShow(new PositionCallback() {
            
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                m_popupPanel.setWidth((getOffsetWidth() - 5) + "px");
                m_popupPanel.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + 24);
            }
        });
    }

    @Override
    public KscReportDetail getSelectedReport() {
        return m_selectionModel.getSelectedObject();
    }
    
    @UiHandler("m_searchButton")
    public void handleSearchButton(ClickEvent event) {
        m_presenter.onSearchButtonClicked();
    }

}
