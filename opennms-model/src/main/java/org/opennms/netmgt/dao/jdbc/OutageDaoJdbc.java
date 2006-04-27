/**
 * 
 */
package org.opennms.netmgt.dao.jdbc;

import java.util.Collection;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.jdbc.outage.FindAllOutages;
import org.opennms.netmgt.dao.jdbc.outage.FindByOutageId;
import org.opennms.netmgt.dao.jdbc.outage.LazyOutage;
import org.opennms.netmgt.dao.jdbc.outage.OutageSave;
import org.opennms.netmgt.dao.jdbc.outage.OutageUpdate;
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
    
    public OnmsOutage load(int id) {
        return load(new Integer(id));
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
        if (outage.getId() == null)
            throw new IllegalArgumentException("Cannot update a outage without a outageid");
        
        if (isDirty(outage))
        		getOutageUpdater().doUpdate(outage);
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#saveOrUpdate(org.opennms.netmgt.model.OnmsOutage)
     */
    public void saveOrUpdate(OnmsOutage outage) {
        if (outage.getId() == null)
            save(outage);
        else
            update(outage);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#flush()
     */
    public void flush() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#clear()
     */
    public void clear() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#countAll()
     */
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from outages");
    }
    
    private boolean isDirty(OnmsOutage outage) {
		if (outage instanceof LazyOutage) {
			LazyOutage lazyOutage = (LazyOutage) outage;
			return lazyOutage.isDirty();
		}
		return true;
    }
    
    private OutageUpdate getOutageUpdater() {
        return new OutageUpdate(getDataSource());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#findAll()
     */
    public Collection findAll() {
        return new FindAllOutages(getDataSource()).findSet();
	}

    public OnmsOutage get(int id) {
        return get(new Integer(id));
    }

    public OnmsOutage get(Integer id) {
        if (Cache.retrieve(OnmsOutage.class, id) == null)
            return new FindByOutageId(getDataSource()).findUnique(id);
        else
            return (OnmsOutage)Cache.retrieve(OnmsOutage.class, id);
    }


}
