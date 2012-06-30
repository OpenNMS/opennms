package org.opennms.features.dashboard.client.dnd;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.VetoDragException;

/**
 * Shared drag handler which display events as they are received by the various
 * drag controllers.
 */
public final class DemoDragHandler implements DragHandler {

	/**
	 * CSS blue.
	 */
	private static final String BLUE = "#4444BB";

	/**
	 * CSS green.
	 */
	private static final String GREEN = "#44BB44";

	/**
	 * CSS red.
	 */
	private static final String RED = "#BB4444";

	/**
	 * Text area where event messages are shown.
	 */

	public DemoDragHandler() {
	}

	public void onDragEnd(DragEndEvent event) {
		// TODO Auto-generated method stub

	}

	public void onDragStart(DragStartEvent event) {
		// TODO Auto-generated method stub

	}

	public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
		// TODO Auto-generated method stub

	}

	public void onPreviewDragStart(DragStartEvent event)
			throws VetoDragException {
		// TODO Auto-generated method stub

	}
}
