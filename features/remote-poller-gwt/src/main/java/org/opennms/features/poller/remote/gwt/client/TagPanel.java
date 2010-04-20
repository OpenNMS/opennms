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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class TagPanel extends Composite implements Collection<String> {

    private final List<String> m_delegate = new ArrayList<String>();

    interface Binder extends UiBinder<Widget, TagPanel> { }
    private static final Binder BINDER = GWT.create(Binder.class);

    interface TagStyles extends CssResource {
        public final static double COUNT = 10.0;
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

    public interface TagSelectedEventHandler extends EventHandler {
        public void onTagSelected(String tagName);
        public void onTagCleared();
    }

    public static class TagSelectedEvent extends GwtEvent<TagSelectedEventHandler>
    {
        public static Type<TagSelectedEventHandler> TYPE = new Type<TagSelectedEventHandler>();

        private final String m_tagName;

        public TagSelectedEvent(String tagName) {
            m_tagName = tagName;
        }

        protected void dispatch(TagSelectedEventHandler handler) {
            handler.onTagSelected(m_tagName);
        }

        public GwtEvent.Type<TagSelectedEventHandler> getAssociatedType() {
            return TYPE;
        }
    }

    public TagPanel() {
        super();
        initWidget(BINDER.createAndBindUi(this));
        this.add("Hello");
        this.add("Hello");
        this.add("Hello");
        this.add("Hello");
        this.add("Hello");
        this.add("Hello");
        this.add("Hello");
        this.add("Hello");
        this.add("World");
        this.add("World");
        this.add("World");
        this.add("World");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
        this.add("!");
    }

    public void setEventBus(final HandlerManager eventBus) {
        m_eventBus = eventBus;
    }

    public boolean add(String e) {
        boolean retval = m_delegate.add(e);
        updatePanel();
        return retval;
    }

    public boolean addAll(Collection<? extends String> c) {
        boolean retval = m_delegate.addAll(c);
        updatePanel();
        return retval;
    }

    public void clear() {
        m_delegate.clear();
        updatePanel();
    }

    public boolean contains(Object o) {
        return m_delegate.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return m_delegate.containsAll(c);
    }

    public boolean isEmpty() {
        return m_delegate.isEmpty();
    }

    public Iterator<String> iterator() {
        return m_delegate.iterator();
    }

    public boolean remove(Object o) {
        boolean retval = m_delegate.remove(o);
        updatePanel();
        return retval;
    }

    public boolean removeAll(Collection<?> c) {
        boolean retval = m_delegate.removeAll(c);
        updatePanel();
        return retval;
    }

    public boolean retainAll(Collection<?> c) {
        boolean retval = m_delegate.retainAll(c);
        updatePanel();
        return retval;
    }

    public int size() {
        return m_delegate.size();
    }

    public Object[] toArray() {
        return m_delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return m_delegate.toArray(a);
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

        for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
            final Anchor tagLabel = new Anchor();
            double rawValue = (double)(entry.getValue() - minCount) * TagStyles.COUNT / (double)(maxCount - minCount);
            tagLabel.setText(entry.getKey());
            tagLabel.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    m_eventBus.fireEvent(new TagSelectedEvent(tagLabel.getText()));
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
        }
    }
}
