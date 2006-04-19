/**
 * 
 */
package org.opennms.netmgt.dao.jdbc;

import java.util.Collection;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;

/**
 * @author mhuot
 *
 */
public class OutageDaoJdbcTest extends AbstractDaoJdbc implements OutageDao {

    public OutageDaoJdbcTest() {
            super();
    }

    public OutageDaoJdbcTest(DataSource ds) {
            super(ds);
    }
    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#load(java.lang.Integer)
     */
    public OnmsOutage load(Integer id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#save(org.opennms.netmgt.model.OnmsOutage)
     */
    public void save(OnmsOutage outage) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#update(org.opennms.netmgt.model.OnmsOutage)
     */
    public void update(OnmsOutage outage) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#saveOrUpdate(org.opennms.netmgt.model.OnmsOutage)
     */
    public void saveOrUpdate(OnmsOutage outage) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#findAll()
     */
    public Collection findAll() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#flush()
     */
    public void flush() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#clear()
     */
    public void clear() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#countAll()
     */
    public int countAll() {
        // TODO Auto-generated method stub
        return 0;
    }

}
