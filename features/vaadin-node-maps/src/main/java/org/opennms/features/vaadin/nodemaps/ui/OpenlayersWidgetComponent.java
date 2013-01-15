package org.opennms.features.vaadin.nodemaps.ui;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.geocoder.CoordinateParseException;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.vaadin.nodemaps.gwt.client.VOpenlayersWidget;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.VerticalLayout;

@ClientWidget(value=VOpenlayersWidget.class)
public class OpenlayersWidgetComponent extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private GeocoderService m_geocoderService;

    public OpenlayersWidgetComponent() {}
    public OpenlayersWidgetComponent(final NodeDao nodeDao, final AssetRecordDao assetDao, final GeocoderService geocoder) {
        m_nodeDao = nodeDao;
        m_assetDao = assetDao;
        m_geocoderService = geocoder;
    }

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (m_nodeDao == null) {
            return;
        }

        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("assetRecord", "asset");
        cb.isNotNull("asset.geolocation");
        cb.ne("asset.geolocation", "");

        target.startTag("nodes");

        for (final OnmsNode node : m_nodeDao.findMatching(cb.toCriteria())) {
            paintNode(target, node);
        }
        target.endTag("nodes");
    }

    void paintNode(final PaintTarget target, final OnmsNode node) throws PaintException {
        final OnmsAssetRecord assets = node.getAssetRecord();
        if (assets != null && assets.getGeolocation() != null) {
            final OnmsGeolocation geolocation = assets.getGeolocation();

            final String addressString = geolocation.asAddressString();
            if (geolocation.getCoordinates() == null || geolocation.getCoordinates() == "" && addressString != "") {
                LogUtils.debugf(this, "No coordinates for node %s, getting geolocation for street address: %s", node.getId(), addressString);
                Coordinates coordinates = null;
                try {
                    coordinates = m_geocoderService.getCoordinates(addressString);
                    if (coordinates == null) {
                        LogUtils.debugf(this, "Failed to look up coordinates for street address: %s", addressString);
                    } else {
                        geolocation.setCoordinates(coordinates.getLatitude() + "," + coordinates.getLongitude());
                        m_assetDao.saveOrUpdate(assets);
                    }
                } catch (final CoordinateParseException e) {
                    LogUtils.infof(this, e, "Failed to retrieve coordinates.");
                }
            }
            
            if (geolocation.getCoordinates() != null && geolocation.getCoordinates() != "") {
                target.startTag(node.getId().toString());
                final String[] coordinates = geolocation.getCoordinates().split(",");
                target.addAttribute("latitude", coordinates[0]);
                target.addAttribute("longitude", coordinates[1]);
                target.endTag(node.getId().toString());
            }
        }
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
}
