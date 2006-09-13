package org.opennms.netmgt.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.opennms.netmgt.model.AvailabilityReportLocator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

public class AvailabilityReportLocatorJdbc extends SimpleJdbcDaoSupport implements
		AvailabilityReportLocatorDao {

	public void delete(int id) {
		
		getJdbcTemplate().update("DELETE from reportlocator WHERE reportId = ?", new Object[] { new Integer(id) });
	}

	public Collection<AvailabilityReportLocator> findAll() {
		
		return getSimpleJdbcTemplate().query("SELECT * FROM reportlocator", new ParameterizedRowMapper<AvailabilityReportLocator>() {
			

			public AvailabilityReportLocator mapRow(ResultSet rs, int arg1) throws SQLException {
				AvailabilityReportLocator locator = new AvailabilityReportLocator();
				locator.setId(rs.getInt("reportId"));
				locator.setCategory(rs.getString("reportCategory"));
				locator.setDate(rs.getDate("reportDate"));
				locator.setFormat(rs.getString("reportFormat"));
				locator.setType(rs.getString("reportType"));
				locator.setLocation(rs.getString("reportLocation"));
				locator.setAvailable(rs.getBoolean("reportAvailable"));
				return locator;
			}
		});

	}

	public Collection<AvailabilityReportLocator> findByCategory(String categoryName) {
		
		return getSimpleJdbcTemplate().query("SELECT * FROM reportlocator WHERE categoryName = "+categoryName, new ParameterizedRowMapper<AvailabilityReportLocator>() {
			

			public AvailabilityReportLocator mapRow(ResultSet rs, int arg1) throws SQLException {
				AvailabilityReportLocator locator = new AvailabilityReportLocator();
				locator.setId(rs.getInt("Id"));
				locator.setCategory(rs.getString("categoryName"));
				locator.setDate(rs.getDate("date"));
				locator.setFormat(rs.getString("format"));
				locator.setType(rs.getString("type"));
				locator.setLocation(rs.getString("location"));
				return locator;
			}
		});

	}

	public AvailabilityReportLocator get(Integer id) {
		
		return getSimpleJdbcTemplate().queryForObject("SELECT * FROM reportlocator WHERE reportid = "+id, new ParameterizedRowMapper<AvailabilityReportLocator>() {
		
			public AvailabilityReportLocator mapRow(ResultSet rs, int arg1) throws SQLException {
				AvailabilityReportLocator locator = new AvailabilityReportLocator();
				locator.setId(rs.getInt("reportid"));
				locator.setCategory(rs.getString("reportcategory"));
				locator.setDate(rs.getDate("reportdate"));
				locator.setFormat(rs.getString("reportformat"));
				locator.setType(rs.getString("reporttype"));
				locator.setLocation(rs.getString("reportlocation"));
				locator.setAvailable(rs.getBoolean("reportavailable"));
				return locator;
			}
		});
	}
	
	
	public void saveOrUpdate(AvailabilityReportLocator locator) {
        if (locator.getId() == null)
            save(locator);
        else
            update(locator);
    }

	   public void save(AvailabilityReportLocator locator) {
	        if (locator.getId() != null)
	            throw new IllegalArgumentException("Cannot save a report locator that already has a reportid");
	        
	        locator.setId(allocateId());
	        
	        getJdbcTemplate().execute("INSERT INTO reportLocator (reportId, reportCategory, reportDate, reportFormat, reportType, reportLocation, reportAvailable)" +
	        		" VALUES ('" 
	        			+ locator.getId() + "','" 
	        			+ locator.getCategory() + "','"
	        			+ locator.getDate() + "','" 
	        			+ locator.getFormat() + "','" 
	        			+ locator.getType() + "','" 
	        			+ locator.getLocation() + "','" 
	        			+ locator.getAvailable() + "');");
	        
	    }
	   
	   public void update(AvailabilityReportLocator locator) {
	        if (locator.getId() == null)
	            throw new IllegalArgumentException("Cannot update a report locator that has null reportid");
	        getJdbcTemplate().execute("UPDATE reportLocator SET"
	        		+ " reportCategory = '" + locator.getCategory() 
	        		+ "', reportDate = '" + locator.getDate()
	        		+ "', reportFormat = '" + locator.getFormat()
	        		+ "', reportType, '" + locator.getType()
	        		+ "', reportLocation, '"  + locator.getLocation()
	        		+ "', reportAvailable, '" + locator.getAvailable()
	        		+ "' WHERE reportId = '" + locator.getId() + "';");
	       	
	}


	public void clear() {
		// TODO Auto-generated method stub

	}

	public int countAll() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void flush() {
		// TODO Auto-generated method stub

	}
	
	private Integer allocateId() {
		return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('reportNxtId')"));
		}

	public void delete(AvailabilityReportLocator entity) {
		delete(entity.getId());
	}

	public AvailabilityReportLocator load(Integer id) {
		return get(id);
	}

	public void initialize(Object obj) {
		// TODO Auto-generated method stub
		
	}


}
