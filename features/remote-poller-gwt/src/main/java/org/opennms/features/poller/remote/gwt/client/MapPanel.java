package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.ui.Widget;

/**
 * <p>MapPanel interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface MapPanel {
    
    /**
     * <p>getWidget</p>
     *
     * @return a {@link com.google.gwt.user.client.ui.Widget} object.
     */
    public Widget getWidget();

    /**
     * <p>showLocationDetails</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param htmlTitle a {@link java.lang.String} object.
     * @param htmlContent a {@link java.lang.String} object.
     */
    public void showLocationDetails(String name, String htmlTitle, String htmlContent);

    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public GWTBounds getBounds();

    /**
     * <p>setBounds</p>
     *
     * @param locationBounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public void setBounds(GWTBounds locationBounds);

    /**
     * <p>placeMarker</p>
     *
     * @param marker a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
     */
    public void placeMarker(GWTMarkerState marker);
    
}
