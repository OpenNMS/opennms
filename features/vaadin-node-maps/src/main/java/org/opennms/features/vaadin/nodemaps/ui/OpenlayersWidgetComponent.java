package org.opennms.features.vaadin.nodemaps.ui;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.vaadin.nodemaps.gwt.client.VOpenlayersWidget;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.VerticalLayout;

@ClientWidget(value=VOpenlayersWidget.class)
public class OpenlayersWidgetComponent extends VerticalLayout {
    private static final long serialVersionUID = 1L;
    private NodeDao m_nodeDao;

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
            final OnmsAssetRecord assets = node.getAssetRecord();
            if (assets != null && assets.getGeolocation() != null && !assets.getGeolocation().equals("")) {
                target.startTag(node.getId().toString());
                final String[] geolocation = assets.getGeolocation().split(",");
                target.addAttribute("latitude", geolocation[0]);
                target.addAttribute("longitude", geolocation[1]);
                target.endTag(node.getId().toString());
            }
        }
        target.endTag("nodes");
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
}
