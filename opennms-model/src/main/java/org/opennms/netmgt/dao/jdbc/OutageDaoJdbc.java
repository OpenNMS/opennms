/**
 * 
 */
package org.opennms.netmgt.dao.jdbc;

import java.util.Collection;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.jdbc.outage.FindByOutageId;
import org.opennms.netmgt.dao.jdbc.outage.OutageSave;
import org.opennms.netmgt.model.OnmsOutage;

/**
 * @author mhuot
 *
 */
public class OutageDaoJdbc extends AbstractDaoJdbc implements OutageDao {

    public OutageDaoJdbc() {
            super();
    }

    public OutageDaoJdbc(DataSource ds) {
            super(ds);
    }
    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#load(java.lang.Integer)
     */
    public OnmsOutage load(Integer id) {
        return new FindByOutageId(getDataSource()).findUnique(id);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#save(org.opennms.netmgt.model.OnmsOutage)
     */
    public void save(OnmsOutage outage) {
        if (outage.getId() != null)
            throw new IllegalArgumentException("Cannot save an outage that already has a outageid");
        
        outage.setId(allocateOutageId());
        getOutageSaver().doInsert(outage);

    }

    private OutageSave getOutageSaver() {
        return new OutageSave(getDataSource());
    }

    private Integer allocateOutageId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('outageNxtId')"));
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
