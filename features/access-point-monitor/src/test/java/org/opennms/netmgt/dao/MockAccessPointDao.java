package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;

public class MockAccessPointDao extends AbstractMockDao<OnmsAccessPoint,String> implements AccessPointDao {

    @Override
    protected void generateId(final OnmsAccessPoint ap) {
    }

    @Override
    protected String getId(final OnmsAccessPoint ap) {
        return ap.getPhysAddr();
    }

    @Override
    public OnmsAccessPointCollection findByPackage(final String pkg) {
        final OnmsAccessPointCollection collection = new OnmsAccessPointCollection();
        for (final OnmsAccessPoint ap : findAll()) {
            if (pkg.equals(ap.getPollingPackage())) {
                collection.add(ap);
            }
        }
        return collection;
    }

    @Override
    public List<String> findDistinctPackagesLike(final String pkg) {
        final Set<String> packages = new HashSet<String>();
        for (final OnmsAccessPoint ap : findAll()) {
            packages.add(ap.getPollingPackage());
        }
        return new ArrayList<String>(packages);
    }

}
