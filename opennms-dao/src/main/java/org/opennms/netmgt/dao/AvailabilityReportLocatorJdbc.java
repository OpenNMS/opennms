package org.opennms.netmgt.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.opennms.netmgt.model.AvailabilityReportLocator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

public class AvailabilityReportLocatorJdbc extends SimpleJdbcDaoSupport implements
		AvailabilityReportLocatorDao {

	public void delete(AvailabilityReportLocator locator) {
		
	}

	public Collection findAll() {
		// TODO Auto-generated method stub
		return getSimpleJdbcTemplate().query("SELECT * FROM reportlocator", new ParameterizedRowMapper<AvailabilityReportLocator>() {
			

			public AvailabilityReportLocator mapRow(ResultSet rs, int arg1) throws SQLException {
				AvailabilityReportLocator locator = new AvailabilityReportLocator();
				locator.setId(rs.getInt("Id"));
				locator.setCategoryName(rs.getString("categoryName"));
				locator.setDate(rs.getDate("date"));
				locator.setFormat(rs.getString("format"));
				locator.setType(rs.getString("type"));
				locator.setLocation(rs.getString("location"));
				return locator;
			}
		});

		
		
	}

	public Collection findByCategoryName(String categoryName) {
		
		return getSimpleJdbcTemplate().query("SELECT * FROM reportlocator WHERE categoryName = "+categoryName, new ParameterizedRowMapper<AvailabilityReportLocator>() {
			

			public AvailabilityReportLocator mapRow(ResultSet rs, int arg1) throws SQLException {
				AvailabilityReportLocator locator = new AvailabilityReportLocator();
				locator.setId(rs.getInt("Id"));
				locator.setCategoryName(rs.getString("categoryName"));
				locator.setDate(rs.getDate("date"));
				locator.setFormat(rs.getString("format"));
				locator.setType(rs.getString("type"));
				locator.setLocation(rs.getString("location"));
				return locator;
			}
		});

	}

	public AvailabilityReportLocator get(Integer id) {
		
		return getSimpleJdbcTemplate().queryForObject("SELECT * FROM reportlocator WHERE id = "+id, new ParameterizedRowMapper<AvailabilityReportLocator>() {
		
			public AvailabilityReportLocator mapRow(ResultSet rs, int arg1) throws SQLException {
				AvailabilityReportLocator locator = new AvailabilityReportLocator();
				locator.setId(rs.getInt("Id"));
				locator.setCategoryName(rs.getString("categoryName"));
				locator.setDate(rs.getDate("date"));
				locator.setFormat(rs.getString("format"));
				locator.setType(rs.getString("type"));
				locator.setLocation(rs.getString("location"));
				return locator;
			}
		});
	}

	public void save(AvailabilityReportLocator locator) {
		// TODO Auto-generated method stub

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

}
