package org.opennms.features.topology.app.internal.support;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.IconRepository;

public class IconRepositoryManager {
    
    private List<IconRepository> m_iconRepos = new ArrayList<IconRepository>();
    
    public void addRepository(IconRepository iconRepo) {
        m_iconRepos.add(iconRepo);
    }
    
    public void onBind(IconRepository iconRepo) {
        addRepository(iconRepo);
    }
    
    public void onUnbind(IconRepository iconRepo) {
        m_iconRepos.remove(iconRepo);
    }
    
    public String lookupIconUrlByType(String type) {
        for(IconRepository iconRepo : m_iconRepos) {
            if(iconRepo.contains(type)) {
                return iconRepo.getIconUrl(type);
            }
        }
        
        return "VAADIN/widgetsets/org.opennms.features.topology.widgetset.gwt.TopologyWidgetset/topologywidget/images/group.png";
    }
}
