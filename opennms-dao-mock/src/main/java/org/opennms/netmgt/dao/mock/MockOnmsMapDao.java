package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;

public class MockOnmsMapDao extends AbstractMockDao<OnmsMap, Integer> implements OnmsMapDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsMap map) {
        map.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsMap map) {
        return map.getId();
    }

    @Override
    public Collection<OnmsMap> findAll(final Integer offset, final Integer limit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsLike(final String mapLabel) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByName(final String mapLabel) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMap findMapById(final int id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByNameAndType(final String mapName, final String mapType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByType(final String mapType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findAutoMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findUserMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findSaveMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findAutoAndSaveMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByOwner(final String owner) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByGroup(final String group) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findVisibleMapsByGroup(final String group) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int updateAllAutomatedMap(final Date time) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
