package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.OnmsForeignSource;
import org.springframework.beans.factory.annotation.Autowired;

public class ForeignSourceDaoFilesystem implements ForeignSourceDao {
    @Autowired
    private ForeignSourceRepository m_foreignSourceRepository = null;

    public ForeignSourceDaoFilesystem() {
    }

    public int countAll() {
        return findAll().size();
    }
    
    public List<OnmsForeignSource> findAll() {
        return new ArrayList<OnmsForeignSource>(m_foreignSourceRepository.getForeignSources());
    }

    public OnmsForeignSource get(String foreignSource) {
        return m_foreignSourceRepository.getForeignSource(foreignSource);
    }

    public void save(OnmsForeignSource foreignSource) {
        m_foreignSourceRepository.save(foreignSource);
    }

    public void delete(OnmsForeignSource foreignSource) {
        m_foreignSourceRepository.delete(foreignSource);
    }
    
    public void setForeignSourceRepository(ForeignSourceRepository fsr) {
        m_foreignSourceRepository = fsr;
    }

}
