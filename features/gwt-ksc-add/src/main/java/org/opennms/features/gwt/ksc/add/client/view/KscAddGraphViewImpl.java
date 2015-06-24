/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.ksc.add.client.view;

import java.util.List;

import org.opennms.features.gwt.ksc.add.client.KscReport;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class KscAddGraphViewImpl extends Composite implements KscAddGraphView<KscReport> {

    public class KscReportCell extends AbstractSafeHtmlCell<KscReport> {

        public KscReportCell() {
            super(new SafeHtmlRenderer<KscReport>() {
                @Override
                public SafeHtml render(final KscReport reportDetail) {
                    return new SafeHtml() {
                        private static final long serialVersionUID = -8194796300806582660L;
                        @Override
                        public String asString() {
                            return reportDetail.getLabel();
                        }
                    };
                }
                @Override
                public void render(final KscReport reportDetail, final SafeHtmlBuilder builder) {
                    builder.appendEscaped(reportDetail.getLabel());
                }
            });
        }

        @Override
        protected void render(final Context context, final SafeHtml value, final SafeHtmlBuilder sb) {
            sb.append(value);
        }

    }

    private static KscAddGraphViewImplUiBinder uiBinder = GWT.create(KscAddGraphViewImplUiBinder.class);
    
    @UiTemplate("KscAddGraphViewImpl.ui.xml")
    interface KscAddGraphViewImplUiBinder extends UiBinder<Widget, KscAddGraphViewImpl> {}
    
    @UiField
    LayoutPanel m_layoutPanel;

    @UiField
    TextBox m_titleBox;

    @UiField
    TextBox m_textBox;
    
    @UiField
    Button m_addButton;

    @UiField
    Label m_titleLabel;

    @UiField
    Label m_reportLabel;

    CellList<KscReport> m_reportList;
    SimplePager m_pager;
    PopupPanel m_popupPanel;
    
    private Presenter<KscReport> m_presenter;
    private ListDataProvider<KscReport> m_dataList;
    private SingleSelectionModel<KscReport> m_selectionModel;
    private PositionCallback m_repositionPopupPanel;
    

    public KscAddGraphViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        m_layoutPanel.setSize("100%", "75px");
        m_textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(final KeyDownEvent event) {
                if(m_presenter != null) {
                    m_presenter.onKeyCodeEvent(event, getSearchText());
                }
            }
        });
        m_textBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(final KeyUpEvent event) {
                if(m_presenter != null) {
                    m_presenter.onKeyCodeEvent(event, getSearchText());
                }
            }
        });
        
        m_selectionModel = new SingleSelectionModel<KscReport>();
        m_selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(final SelectionChangeEvent event) {
                if (getSelectedReport() != null) {
                    m_textBox.setValue(getSelectedReport().getLabel(), false);
                    m_popupPanel.hide();
                }
            }
        });

        m_titleLabel.getElement().getStyle().setFontSize(12, Unit.PX);
        m_reportLabel.getElement().getStyle().setFontSize(12, Unit.PX);
        
        m_reportList = new CellList<KscReport>(new KscReportCell());
        m_reportList.setPageSize(10);
        m_reportList.getElement().getStyle().setFontSize(12, Unit.PX);
        m_reportList.setSelectionModel(m_selectionModel);

        m_dataList = new ListDataProvider<KscReport>();
        m_dataList.addDataDisplay(m_reportList);
        
        m_pager = new SimplePager();
        m_pager.setStyleName("onms-table-no-borders-margin");
        m_pager.getElement().getStyle().setWidth(100, Unit.PCT);
        m_pager.setDisplay(m_reportList);
        
        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(m_reportList);
        flowPanel.add(m_pager);
        m_popupPanel = new PopupPanel();
        m_popupPanel.add(flowPanel);
        m_popupPanel.setAutoHideEnabled(true);
        m_popupPanel.setAnimationEnabled(false);
        m_popupPanel.setModal(false);
        m_popupPanel.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_popupPanel.getElement().getStyle().setBorderColor("#B5B8C8");
        m_popupPanel.getElement().getStyle().setPadding(1, Unit.PX);
        
        m_repositionPopupPanel = new PositionCallback() {
            @Override
            public void setPosition(final int offsetWidth, final int offsetHeight) {
                m_popupPanel.setWidth((getOffsetWidth() - 5) + "px");
                m_popupPanel.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + 74);
            }
        };
        
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(final ResizeEvent event) {
                if (m_popupPanel.isShowing()) {
                    m_popupPanel.setPopupPositionAndShow(m_repositionPopupPanel);
                }
            }
        });
    }

    @Override
    public String getSearchText() {
        return m_textBox.getText();
    }

    @Override
    public void setPresenter(final Presenter<KscReport> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void setDataList(final List<KscReport> dataList) {
        m_dataList.setList(dataList);
    }

    @Override
    public String getTitle() {
        return m_titleBox.getValue();
    }
    
    @Override
    public void setTitle(final String title) {
        m_titleBox.setValue(title == null? "" : title);
    }

    @Override
    public KscReport getSelectedReport() {
        return m_selectionModel.getSelectedObject();
    }
    
    @Override
    public void select(final KscReport report) {
        m_selectionModel.setSelected(report, true);
    }
    
    @Override
    public void clearSelection() {
        m_selectionModel.setSelected(m_selectionModel.getSelectedObject(), false);
    }
    
    @UiHandler("m_addButton")
    public void handleAddButton(final ClickEvent event) {
        m_presenter.onAddButtonClicked();
    }
    
    @Override
    public boolean isPopupShowing() {
        return m_popupPanel.isShowing();
    }

    @Override
    public void hidePopup() {
        m_popupPanel.hide();
    }

    @Override
    public void showPopup() {
        if (!m_popupPanel.isShowing()) {
            m_popupPanel.setPopupPositionAndShow(m_repositionPopupPanel);
        }
    }

}
