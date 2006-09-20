package org.opennms.netmgt.rtc;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class DataSenderTest extends OpenNMSTestCase {
    /*
     * This doesn't work unless we have a receiver on the other end.... more of an integration test
     */
    public void XXXtestSendData() throws MarshalException, ValidationException, IOException, FilterParseException, SAXException, SQLException, RTCException {
        Reader reader = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/rtc-configuration.xml");
        RTCConfigFactory.setInstance(new RTCConfigFactory(reader));
        reader.close();
        
        reader = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/categories.xml");
        CategoryFactory.setInstance(new CategoryFactory(reader));
        reader.close();
        
        reader = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/test-database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(reader));
        reader.close();

        
        DataManager dataManager = new DataManager();
        RTCManager.setDataManager(dataManager);

        String categoryName = "Database Servers";
        String categoryNameUrl = "Database+Servers";
        Category category = new Category();
        category.setLabel(categoryName);
        category.setComment("Some database servers.  Exciting, eh?");
        category.setNormal(99.0);
        category.setWarning(97.0);
        RTCCategory rtcCategory = new RTCCategory(category, categoryName);
        Map<String, RTCCategory> rtcCategories = new HashMap<String, RTCCategory>();
        rtcCategories.put(categoryName, rtcCategory);
        
        DataSender sender = new DataSender(rtcCategories, 1);
        sender.subscribe("http://localhost:8080/opennms-webapp/rtc/post/" + categoryNameUrl, categoryName, "rtc", "rtc");
        sender.sendData();
    }
}
