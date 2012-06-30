package org.opennms.features.dashboard.client.dnd;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.AbstractInsertPanelDropController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.allen_sauer.gwt.dnd.client.util.LocationWidgetComparator;

/**
 * A {@link DropController} for instances of {@link VerticalPanel}.
 * 
 * VerticalPanel performance is slow because of positioner DOM manipulation
 */
public class VerticalPanelDropController extends AbstractInsertPanelDropController {

  /**
   * Label for IE quirks mode workaround.
   */
  private static final Label DUMMY_LABEL_IE_QUIRKS_MODE_OFFSET_HEIGHT = new Label("x");

  /**
   * Construct an {@link VerticalPanelDropController}.
   * 
   * @param dropTarget the {@link VerticalPanel} drop target
   */
  public VerticalPanelDropController(VerticalPanel dropTarget) {
    super(dropTarget);
  }

  @Override
  protected LocationWidgetComparator getLocationWidgetComparator() {
    return LocationWidgetComparator.BOTTOM_HALF_COMPARATOR;
  }

  @Override
  protected Widget newPositioner(DragContext context) {
    // Use two widgets so that setPixelSize() consistently affects dimensions
    // excluding positioner border in quirks and strict modes
    SimplePanel outer = new SimplePanel();
    outer.addStyleName(DragClientBundle.INSTANCE.css().positioner());

    // place off screen for border calculation
    RootPanel.get().add(outer, -500, -500);

    // Ensure IE quirks mode returns valid outer.offsetHeight, and thus valid
    // DOMUtil.getVerticalBorders(outer)
    outer.setWidget(DUMMY_LABEL_IE_QUIRKS_MODE_OFFSET_HEIGHT);

    int width = 0;
    int height = 0;
    for (Widget widget : context.selectedWidgets) {
      width = Math.max(width, widget.getOffsetWidth());
      height += widget.getOffsetHeight();
    }

    SimplePanel inner = new SimplePanel();
    inner.setPixelSize(width - DOMUtil.getHorizontalBorders(outer), height
        - DOMUtil.getVerticalBorders(outer));

    outer.setWidget(inner);

    return outer;
  }
}