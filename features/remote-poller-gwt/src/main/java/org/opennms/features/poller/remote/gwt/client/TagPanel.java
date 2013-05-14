/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>TagPanel class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class TagPanel extends Composite implements Collection<String> {

    private final List<String> m_delegate = new ArrayList<String>();

    interface Binder extends UiBinder<Widget, TagPanel> { }
    private static final Binder BINDER = GWT.create(Binder.class);

    interface TagStyles extends CssResource {
        public final static double COUNT = 10.0;
        String selectedTag();
        String tag0();
        String tag1();
        String tag2();
        String tag3();
        String tag4();
        String tag5();
        String tag6();
        String tag7();
        String tag8();
        String tag9();
    }

    private transient HandlerManager m_eventBus;

    @UiField
    FlowPanel tagPanel;

    @UiField
    TagStyles tagStyles;

    @UiField
    Hyperlink clearTags;

    public interface TagSelectedEventHandler extends EventHandler {
        public void onTagSelected(String tagName);
    }

    public interface TagClearedEventHandler extends EventHandler {
        public void onTagCleared();
    }
    
    public interface TagResizeEventHandler extends EventHandler{
        public void onTagPanelResize();
    }

    public static class TagSelectedEvent extends GwtEvent<TagSelectedEventHandler>
    {
        public static Type<TagSelectedEventHandler> TYPE = new Type<TagSelectedEventHandler>();

        private final String m_tagName;

        public TagSelectedEvent(String tagName) {
            m_tagName = tagName;
        }

        @Override
        protected void dispatch(TagSelectedEventHandler handler) {
            handler.onTagSelected(m_tagName);
        }

        @Override
        public GwtEvent.Type<TagSelectedEventHandler> getAssociatedType() {
            return TYPE;
        }
    }

    public static class TagClearedEvent extends GwtEvent<TagClearedEventHandler>
    {
        public static Type<TagClearedEventHandler> TYPE = new Type<TagClearedEventHandler>();

        public TagClearedEvent() {}

        @Override
        protected void dispatch(TagClearedEventHandler handler) {
            handler.onTagCleared();
        }

        @Override
        public GwtEvent.Type<TagClearedEventHandler> getAssociatedType() {
            return TYPE;
        }
    }
    
    public static class TagResizeEvent extends GwtEvent<TagResizeEventHandler>{

        public static Type<TagResizeEventHandler> TYPE = new Type<TagResizeEventHandler>();
        @Override
        protected void dispatch(TagResizeEventHandler handler) {
            handler.onTagPanelResize();
        }

        @Override
        public com.google.gwt.event.shared.GwtEvent.Type<TagResizeEventHandler> getAssociatedType() {
            return TYPE;
        }
        
    }

    /**
     * <p>Constructor for TagPanel.</p>
     */
    public TagPanel() {
        super();
        initWidget(BINDER.createAndBindUi(this));
    }

    /**
     * <p>setEventBus</p>
     *
     * @param eventBus a {@link com.google.gwt.event.shared.HandlerManager} object.
     */
    public void setEventBus(final HandlerManager eventBus) {
        m_eventBus = eventBus;
    }

    /**
     * <p>onClearTagsClick</p>
     *
     * @param event a {@link com.google.gwt.event.dom.client.ClickEvent} object.
     */
    @UiHandler("clearTags")
    public void onClearTagsClick(ClickEvent event) {
        // Remove the "selected" style from all tags
        for (int i = 0; i < tagPanel.getWidgetCount(); i++) {
            tagPanel.getWidget(i).removeStyleName(tagStyles.selectedTag());
        }
        m_eventBus.fireEvent(new TagClearedEvent());
    }

    /**
     * <p>add</p>
     *
     * @param e a {@link java.lang.String} object.
     * @return a boolean.
     */
    @Override
    public boolean add(String e) {
        boolean retval = m_delegate.add(e);
        updatePanel();
        return retval;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean retval = m_delegate.addAll(c);
        updatePanel();
        m_eventBus.fireEvent(new TagResizeEvent());
        return retval;
    }

    /**
     * <p>clear</p>
     */
    @Override
    public void clear() {
        m_delegate.clear();
        updatePanel();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o) {
        return m_delegate.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> c) {
        return m_delegate.containsAll(c);
    }

    /**
     * <p>isEmpty</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isEmpty() {
        return m_delegate.isEmpty();
    }

    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<String> iterator() {
        return m_delegate.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object o) {
        boolean retval = m_delegate.remove(o);
        updatePanel();
        return retval;
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean retval = m_delegate.removeAll(c);
        updatePanel();
        return retval;
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean retval = m_delegate.retainAll(c);
        updatePanel();
        return retval;
    }

    /**
     * <p>size</p>
     *
     * @return a int.
     */
    @Override
    public int size() {
        return m_delegate.size();
    }

    /**
     * <p>toArray</p>
     *
     * @return an array of {@link java.lang.Object} objects.
     */
    @Override
    public Object[] toArray() {
        return m_delegate.toArray();
    }

    /**
     * <p>toArray</p>
     *
     * @param a an array of T objects.
     * @param <T> a T object.
     * @return an array of T objects.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return m_delegate.toArray(a);
    }

    /**
     * <p>selectTag</p>
     *
     * @param tag a {@link java.lang.String} object.
     */
    public void selectTag(String tag) {
        for (Widget widget : tagPanel) {
            if (widget instanceof Anchor) {
                Anchor anchor = (Anchor)widget;
                if (tag != null && tag.replaceAll(" ", "&nbsp;").equals(anchor.getHTML())) {
                    anchor.addStyleName(tagStyles.selectedTag());
                } else {
                    anchor.removeStyleName(tagStyles.selectedTag());
                }
            }
        }
    }

    private void updatePanel() {
        int minCount = Integer.MAX_VALUE, maxCount = Integer.MIN_VALUE;
        Map<String,Integer> tagCounts = new TreeMap<String,Integer>();
        for (String tag : m_delegate) {
            if (tagCounts.containsKey(tag)) {
                tagCounts.put(tag, tagCounts.get(tag).intValue() + 1);
            } else {
                tagCounts.put(tag, 1);
            }
        }

        for (int entry : tagCounts.values()) {
            if (entry < minCount) minCount = entry;
            if (entry > maxCount) maxCount = entry;
        }

        tagPanel.clear();

        for (final Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
            final String tagText = entry.getKey();
            final Anchor tagLabel = new Anchor();
            double rawValue = (double)(entry.getValue() - minCount) * TagStyles.COUNT / (double)(maxCount - minCount);
            tagLabel.setHTML(tagText.replace(" ", "&nbsp;"));
            tagLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectTag(tagText);
                    m_eventBus.fireEvent(new TagSelectedEvent(tagText));
                }
            });
            switch ((int)Math.round(rawValue)) {
            case(0):
                tagLabel.addStyleName(tagStyles.tag0());
            break;
            case(1):
                tagLabel.addStyleName(tagStyles.tag1());
            break;
            case(2):
                tagLabel.addStyleName(tagStyles.tag2());
            break;
            case(3):
                tagLabel.addStyleName(tagStyles.tag3());
            break;
            case(4):
                tagLabel.addStyleName(tagStyles.tag4());
            break;
            case(5):
                tagLabel.addStyleName(tagStyles.tag5());
            break;
            case(6):
                tagLabel.addStyleName(tagStyles.tag6());
            break;
            case(7):
                tagLabel.addStyleName(tagStyles.tag7());
            break;
            case(8):
                tagLabel.addStyleName(tagStyles.tag8());
            break;
            case(9):
                tagLabel.addStyleName(tagStyles.tag9());
            break;
            default:
                tagLabel.addStyleName(tagStyles.tag9());
            }
            tagPanel.add(tagLabel);
            // without this, the tag cloud doesn't wrap properly
            tagPanel.add(new InlineHTML(" "));
            
        }
    }
}
