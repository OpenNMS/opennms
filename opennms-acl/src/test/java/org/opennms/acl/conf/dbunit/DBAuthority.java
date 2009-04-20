package org.opennms.acl.conf.dbunit;

import java.io.FileInputStream;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

public class DBAuthority extends DbUnit {

    public IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSet(new FileInputStream("src/test/java/org/opennms/acl/conf/dbunit/authority-test.xml"));
    }
}
