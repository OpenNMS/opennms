package org.opennms.features.vaadin.nodemaps.ui;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.vaadin.nodemaps.gwt.client.VOpenlayersWidget;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private Logger m_log = LoggerFactory.getLogger(getClass());

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
        cb.isNotNull("asset.geolocation.coordinates");
        cb.ne("asset.geolocation.coordinates", "");

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

            m_log.debug("geolocation = {}", geolocation);

            final String addressString = geolocation.asAddressString();
            if (geolocation.getCoordinates() == null || geolocation.getCoordinates() == "" && addressString != "") {
                m_log.debug("No coordinates for node {}, getting geolocation for street address: {}", node.getId(), addressString);
                Coordinates coordinates = null;
                try {
                    coordinates = m_geocoderService.getCoordinates(addressString);
                    if (coordinates == null) {
                        m_log.debug("Failed to look up coordinates for street address: {}", addressString);
                    } else {
                        geolocation.setCoordinates(coordinates.getLatitude() + "," + coordinates.getLongitude());
                        m_assetDao.saveOrUpdate(assets);
                    }
                } catch (final GeocoderException e) {
                    m_log.debug("Failed to retrieve coordinates", e);
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
    public void setAssetRecordDao(final AssetRecordDao assetDao) {
        m_assetDao = assetDao;
    }
    public void setGeocoderService(final GeocoderService geocoderService) {
        m_geocoderService = geocoderService;
    }
}
