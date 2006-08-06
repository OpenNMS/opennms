package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.dao.ibatis.PathOutage;

public interface PathOutageDao {
	public List getAll();
	
	public void save(PathOutage pathOutage);
	
	public void delete(int pathOutageId);
	
//	public PathOutage getById(int pathOutageId);
}
