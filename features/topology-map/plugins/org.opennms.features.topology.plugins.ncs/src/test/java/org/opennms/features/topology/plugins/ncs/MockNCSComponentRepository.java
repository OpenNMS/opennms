package org.opennms.features.topology.plugins.ncs;

import java.util.List;

import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

public class MockNCSComponentRepository implements NCSComponentRepository {
    @Override
    public void lock() {}
    @Override
    public void initialize(Object obj) {}
    @Override
    public void flush() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int countAll() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void delete(NCSComponent component) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<NCSComponent> findAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NCSComponent> findMatching(OnmsCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int countMatching(OnmsCriteria onmsCrit) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public NCSComponent get(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NCSComponent load(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void save(NCSComponent component) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void saveOrUpdate(NCSComponent component) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void update(NCSComponent component) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<NCSComponent> findByType(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NCSComponent findByTypeAndForeignIdentity(String type,
            String foreignSource, String foreignId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NCSComponent> findComponentsThatDependOn(
            NCSComponent component) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NCSComponent> findComponentsWithAttribute(String attrKey,
            String attrValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NCSComponent> findComponentsByNodeId(int nodeid) {
        // TODO Auto-generated method stub
        return null;
    }
    
}