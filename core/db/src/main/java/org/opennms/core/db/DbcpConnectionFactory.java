package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;

public class DbcpConnectionFactory extends BaseConnectionFactory {
	
    private final static String PROP_DEFAULTAUTOCOMMIT = "defaultAutoCommit";
    private final static String PROP_DEFAULTREADONLY = "defaultReadOnly";
    private final static String PROP_DEFAULTTRANSACTIONISOLATION = "defaultTransactionIsolation";
    private final static String PROP_DEFAULTCATALOG = "defaultCatalog";
    private final static String PROP_DRIVERCLASSNAME = "driverClassName";
    private final static String PROP_MAXACTIVE = "maxActive";
    private final static String PROP_MAXIDLE = "maxIdle";
    private final static String PROP_MINIDLE = "minIdle";
    private final static String PROP_INITIALSIZE = "initialSize";
    private final static String PROP_MAXWAIT = "maxWait";
    private final static String PROP_TESTONBORROW = "testOnBorrow";
    private final static String PROP_TESTONRETURN = "testOnReturn";
    private final static String PROP_TIMEBETWEENEVICTIONRUNSMILLIS = "timeBetweenEvictionRunsMillis";
    private final static String PROP_NUMTESTSPEREVICTIONRUN = "numTestsPerEvictionRun";
    private final static String PROP_MINEVICTABLEIDLETIMEMILLIS = "minEvictableIdleTimeMillis";
    private final static String PROP_TESTWHILEIDLE = "testWhileIdle";
    private final static String PROP_PASSWORD = "password";
    private final static String PROP_URL = "url";
    private final static String PROP_USERNAME = "username";
    private final static String PROP_VALIDATIONQUERY = "validationQuery";
    private final static String PROP_VALIDATIONQUERY_TIMEOUT = "validationQueryTimeout";
    /**
     * The property name for initConnectionSqls.
     * The associated value String must be of the form [query;]*
     * @since 1.3
     */
    private final static String PROP_INITCONNECTIONSQLS = "initConnectionSqls";
    private final static String PROP_ACCESSTOUNDERLYINGCONNECTIONALLOWED = "accessToUnderlyingConnectionAllowed";
    private final static String PROP_REMOVEABANDONED = "removeAbandoned";
    private final static String PROP_REMOVEABANDONEDTIMEOUT = "removeAbandonedTimeout";
    private final static String PROP_LOGABANDONED = "logAbandoned";
    private final static String PROP_POOLPREPAREDSTATEMENTS = "poolPreparedStatements";
    private final static String PROP_MAXOPENPREPAREDSTATEMENTS = "maxOpenPreparedStatements";
    private final static String PROP_CONNECTIONPROPERTIES = "connectionProperties";

    private final static String[] ALL_PROPERTIES = {
        PROP_DEFAULTAUTOCOMMIT,
        PROP_DEFAULTREADONLY,
        PROP_DEFAULTTRANSACTIONISOLATION,
        PROP_DEFAULTCATALOG,
        PROP_DRIVERCLASSNAME,
        PROP_MAXACTIVE,
        PROP_MAXIDLE,
        PROP_MINIDLE,
        PROP_INITIALSIZE,
        PROP_MAXWAIT,
        PROP_TESTONBORROW,
        PROP_TESTONRETURN,
        PROP_TIMEBETWEENEVICTIONRUNSMILLIS,
        PROP_NUMTESTSPEREVICTIONRUN,
        PROP_MINEVICTABLEIDLETIMEMILLIS,
        PROP_TESTWHILEIDLE,
        PROP_PASSWORD,
        PROP_URL,
        PROP_USERNAME,
        PROP_VALIDATIONQUERY,
        PROP_VALIDATIONQUERY_TIMEOUT,
        PROP_INITCONNECTIONSQLS,
        PROP_ACCESSTOUNDERLYINGCONNECTIONALLOWED,
        PROP_REMOVEABANDONED,
        PROP_REMOVEABANDONEDTIMEOUT,
        PROP_LOGABANDONED,
        PROP_POOLPREPAREDSTATEMENTS,
        PROP_MAXOPENPREPAREDSTATEMENTS,
        PROP_CONNECTIONPROPERTIES
    };


	private DataSource m_ds;
	private JdbcDataSource m_jdbcDataSource;
	private boolean m_debug = false;

	public DbcpConnectionFactory(String configFile, String dsName) throws IOException, MarshalException, ValidationException, PropertyVetoException, SQLException {
		super(configFile, dsName);
		System.out.println("<init>("+configFile+", "+dsName+")"+m_ds.toString());
	}
	
	public DbcpConnectionFactory(InputStream stream, String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
		super(stream, dsName);
		System.out.println("<init>(stream, "+dsName+")"+m_ds.toString());
	}

	@Override
	//no-op
	public void setIdleTimeout(int idleTimeout) {
		if (m_debug ) if (m_debug ) System.out.println("Whoops: setIdleTimeout("+idleTimeout+")"+m_ds.toString());
	}

	@Override
	//BasicDataSource does not support this method
	public int getLoginTimeout() throws SQLException {
		if (m_debug ) System.out.println("Whoops: setLoginTimeout()"+m_ds.toString());
		//return ((BasicDataSource)m_ds).getLoginTimeout();
		return -1;
	}
	
	@Override
	//no-op BasicDataSource does not support this method
	public void setLoginTimeout(int loginTimeout) throws SQLException {
		if (m_debug ) System.out.println("Whoops: setLoginTimeout("+loginTimeout+")"+m_ds.toString());
		//((BasicDataSource)m_ds).setLoginTimeout(loginTimeout);
	}

	@Override
	public void setMinPool(int minPool) {
		if (m_debug ) System.out.println("Whoops: setMinPool("+minPool+")"+m_ds.toString());
		((BasicDataSource)m_ds).setMinIdle(minPool);
		((BasicDataSource)m_ds).setInitialSize(minPool);
	}

	@Override
	public void setMaxPool(int maxPool) {
		if (m_debug ) System.out.println("Whoops: setMaxPool("+maxPool+")"+m_ds.toString());
		((BasicDataSource)m_ds).setMaxIdle(maxPool);
	}

	@Override
	public void setMaxSize(int maxSize) {
		if (m_debug ) System.out.println("Whoops: setMaxSize("+maxSize+")"+m_ds.toString());
		((BasicDataSource)m_ds).setMaxActive(maxSize);
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (m_debug ) System.out.println("Whoops: getConnection()"+m_ds.toString());
		if (m_debug ) System.out.println("Whoops: getConnection()"+m_ds.toString());
		System.out.println(m_ds.toString());
		return ((BasicDataSource)m_ds).getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		if (m_debug ) System.out.println("Whoops: getConnection("+username+", "+password+")"+m_ds.toString());
		return ((BasicDataSource)m_ds).getConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		if (m_debug ) System.out.println("Whoops: getLogWriter()"+m_ds.toString());		
		return ((BasicDataSource)m_ds).getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		if (m_debug ) System.out.println("Whoops: setLogWriter(logWriter)"+m_ds.toString());		
		((BasicDataSource)m_ds).setLogWriter(logWriter);
	}

	@Override
	public String getUrl() {
		if (m_debug ) System.out.println("Whoops: getUrl()"+m_ds.toString());		
		return ((BasicDataSource)m_ds).getUrl();
	}

	@Override
	public void setUrl(String url) {
		if (m_debug ) System.out.println("Whoops: setUrl("+url+")"+m_ds.toString());		
		((BasicDataSource)m_ds).setUrl(url);
	}

	@Override
	public String getUser() {
		if (m_debug ) System.out.println("Whoops: getUser()"+m_ds.toString());		
		return ((BasicDataSource)m_ds).getUsername();
	}

	@Override
	public void setUser(String user) {
		if (m_debug ) System.out.println("Whoops: setUser("+user+")"+m_ds.toString());
		((BasicDataSource)m_ds).setUsername(user);
	}

	@Override
	public DataSource getDataSource() {
		if (m_debug ) System.out.println("Whoops: getDataSource()"+m_ds.toString());
		return m_ds;
	}

	private JdbcDataSource getJdbcDataSource() {
		return m_jdbcDataSource;
	}
	
	@Override
	protected void initializePool(JdbcDataSource jdbcDataSource) throws SQLException {
	
		if (m_debug ) System.out.println("Whoops: initializePool(jdbcDataSource)");
		m_jdbcDataSource = jdbcDataSource;
		
		final Properties props = determineProps(jdbcDataSource);
        
        try {
    		if (m_debug ) System.out.println("Whoops: initializePool(jdbcDataSource); creating datasource...");
        	m_ds = BasicDataSourceFactory.createDataSource(props);
    		if (m_debug ) System.out.println("Whoops: initializePool(jdbcDataSource); datasource created.");
        } catch (Exception e) {
        	throw new SQLException("Unable to create BasicDataSource instance", e);
        }

        
        //properties.put("username", jdbcDataSource.getUserName());
        //properties.put("password", jdbcDataSource.getPassword());
        //properties.put("url", jdbcDataSource.getUrl());
        //properties.put("connectionProperties", ?);
        //properties.put("driverClassName", jdbcDataSource.getClassName());
        
//		DriverManagerConnectionFactory factory = new DriverManagerConnectionFactory(getUrl(), props);
//		ObjectPool<PoolableConnectionFactory> objPool = null;
//		
//		KeyedObjectPoolFactory stmtPoolFactory = null;
//		String validationQuery = null;
//		Collection validationQueryTimeout = null;
//		Boolean defaultReadOnly = null;
//		boolean defaultAutoCommit = true;
//		int defaultTransactionIsolation = -1;
//		String defaultCatalog = null;
//		AbandonedConfig config = null;
//		
//		PoolableObjectFactory<PoolableConnectionFactory> objPoolFactory = new PoolableConnectionFactory(
//				factory, objPool, stmtPoolFactory, validationQuery, validationQueryTimeout, 
//				defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config );
//		
//		objPool = new GenericObjectPool<PoolableConnectionFactory>(objPoolFactory);
//		
//		PoolingDataSource pds = new PoolingDataSource(objPool);
//		
//		m_ds = pds;
	}
	
	private Properties determineProps(JdbcDataSource jdbcDataSource) {
		//Go through properties specified in the configuration as parameters
		final Properties props = new Properties();
        for (final Param parameter : jdbcDataSource.getParamCollection()) {
            props.put(parameter.getName(), parameter.getValue());
        }
        
        //Now set the defaults for the BasicDataSoruce which implements a connection pool
        if (props.get(PROP_ACCESSTOUNDERLYINGCONNECTIONALLOWED) == null) {
        	props.setProperty(PROP_ACCESSTOUNDERLYINGCONNECTIONALLOWED, "false");
        }
        
        if (props.get(PROP_DEFAULTAUTOCOMMIT)==null) {
        	props.setProperty(PROP_DEFAULTAUTOCOMMIT, "true");
        }
        
        if (props.get(PROP_DEFAULTCATALOG)==null) {
        	//the default is ultimately null in the BasicDataSource.class
        }
        
        if (props.get(PROP_DEFAULTREADONLY)==null) {
        	//the default is ultimately null in the BasicDataSource.class
        }
		
        if (props.get(PROP_DEFAULTTRANSACTIONISOLATION)==null) {
        	props.setProperty(PROP_DEFAULTTRANSACTIONISOLATION, "-1");
        }
        
        if (props.get(PROP_DRIVERCLASSNAME)==null) {
        	props.setProperty(PROP_DRIVERCLASSNAME, m_jdbcDataSource.getClassName());
        }
        
        if (props.get(PROP_INITCONNECTIONSQLS)==null) {
        	//the default is ultimately null in the BasicDataSource.class
        }
        
        if (props.get(PROP_INITIALSIZE)==null) {
        	props.setProperty(PROP_INITIALSIZE, "0");
        }
        
        //Had to research AbandonedConfig class to get default setting for this
        if (props.get(PROP_LOGABANDONED)==null) {
        	props.setProperty(PROP_LOGABANDONED, "false");
        }
        
        //FIXME:Having this set to the default of 8is inconsistent with corrent OpenNMS expectations
        if (props.get(PROP_MAXACTIVE)==null) {
        	//props.setProperty(PROP_MAXACTIVE, Integer.toString(GenericObjectPool.DEFAULT_MAX_ACTIVE));
        	props.setProperty(PROP_MAXACTIVE, "50");
        }
        
        if (props.get(PROP_MAXIDLE)==null) {
        	props.setProperty(PROP_MAXIDLE, Integer.toString(GenericObjectPool.DEFAULT_MAX_IDLE));
        }
        
        if (props.get(PROP_MAXOPENPREPAREDSTATEMENTS)==null) {
        	props.setProperty(PROP_MAXOPENPREPAREDSTATEMENTS, Integer.toString(GenericKeyedObjectPool.DEFAULT_MAX_TOTAL));
        }
        
        //this one is specified as a long
        if (props.get(PROP_MAXWAIT)==null) {
        	props.setProperty(PROP_MAXWAIT, "-1");
        }
        
        if (props.get(PROP_MINEVICTABLEIDLETIMEMILLIS)==null) {
        	props.setProperty(PROP_MINEVICTABLEIDLETIMEMILLIS, "1800000");
        }
        
        if (props.get(PROP_MINIDLE)==null) {
        	props.setProperty(PROP_MINIDLE, Integer.toString(GenericObjectPool.DEFAULT_MIN_IDLE));
        }
        
        if (props.get(PROP_NUMTESTSPEREVICTIONRUN)== null) {
        	props.setProperty(PROP_NUMTESTSPEREVICTIONRUN, Integer.toBinaryString(GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN));
        }
        
        if (props.get(PROP_PASSWORD)==null) {
        	//the default is ultimately null in the BasicDataSource.class
        }
        
        if (props.get(PROP_POOLPREPAREDSTATEMENTS)==null) {
        	props.setProperty(PROP_POOLPREPAREDSTATEMENTS, "false");
        }
        
        //see AbandondedConfig class for default
        if (props.get(PROP_REMOVEABANDONED)==null) {
        	props.setProperty(PROP_REMOVEABANDONED, "false");
        }
        
        //see AbandondedConfig class for default in seconds
        if (props.get(PROP_REMOVEABANDONEDTIMEOUT)==null) {
        	props.setProperty(PROP_REMOVEABANDONEDTIMEOUT, "300");
        }
        
        if (props.get(PROP_TESTONBORROW)==null) {
        	props.setProperty(PROP_TESTONBORROW, "true");
        }
        
        if (props.get(PROP_TESTONRETURN)==null) {
        	props.setProperty(PROP_TESTONRETURN, "false");
        }
        
        if (props.get(PROP_TESTWHILEIDLE)==null) {
        	props.setProperty(PROP_TESTWHILEIDLE, "false");
        }
        
        if (props.get(PROP_TIMEBETWEENEVICTIONRUNSMILLIS)==null) {
        	//Can't use GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS for it is
        	//defined as a long
        	props.setProperty(PROP_TIMEBETWEENEVICTIONRUNSMILLIS, "-1");
        }
        
        if (props.get(PROP_URL)==null) {
			props.setProperty(PROP_URL, m_jdbcDataSource.getUrl());
        }
        
        if (props.get(PROP_USERNAME)==null) {
        	//the default is ultimately null in the BasicDataSource.class and hesitant
        	//to set this to the likely default of "opennms"
        	//props.setProperty(PROP_USERNAME, "opennms");
        }
        
        if (props.get(PROP_VALIDATIONQUERY)==null) {
        	//the default is ultimately null in the BasicDataSource.class
        }
        
        if (props.get(PROP_VALIDATIONQUERY_TIMEOUT)==null) {
        	props.setProperty(PROP_VALIDATIONQUERY_TIMEOUT, "-1");
        }
		return props;
	}

}
