package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class ConnectionFactoryUtil {

    /**
     * <p>marshalDataSourceFromConfig</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param dsName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static JdbcDataSource marshalDataSourceFromConfig(final InputStream stream, final String dsName) throws MarshalException, ValidationException {
    	final DataSourceConfiguration dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, stream);
        return validateDataSourceConfiguration(dsName, dsc);
    }

    /**
     * <p>marshalDataSourceFromConfig</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @param dsName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.sql.SQLException if any.
     * @deprecated Use code for InputStream instead to avoid character set issues
     */
    public static JdbcDataSource marshalDataSourceFromConfig(final Reader rdr, final String dsName) throws MarshalException, ValidationException, PropertyVetoException, SQLException {
    	final DataSourceConfiguration dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, rdr);
        return validateDataSourceConfiguration(dsName, dsc);
    }

    private static JdbcDataSource validateDataSourceConfiguration(final String dsName, final DataSourceConfiguration dsc) {
        for (final JdbcDataSource jdbcDs : dsc.getJdbcDataSourceCollection()) {
            if (jdbcDs.getName().equals(dsName)) {
                return jdbcDs;
            }
        }
        
        throw new IllegalArgumentException("C3P0ConnectionFactory: DataSource: "+dsName+" is not defined.");
    }

}
