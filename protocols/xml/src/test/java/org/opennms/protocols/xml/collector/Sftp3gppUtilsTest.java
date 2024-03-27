/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.xml.collector;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.blob.inmemory.InMemoryMapBlobStore;
import org.opennms.netmgt.model.ResourcePath;

/**
 * The Test Class for Sftp3gppUtils.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppUtilsTest {

    /**
     * Test parser.
     *
     * @throws Exception the exception
     */
    @Test
    public void testParser() throws Exception {
        String format = Sftp3gppUtils.get3gppFormat("cdmaSc");
        Assert.assertEquals("system|/=/v=1/sg-name=<mmeScSgName>|", format);
        Map<String,String> properties = Sftp3gppUtils.get3gppProperties(format, "system|/=/v=1/sg-name=GA|");
        Assert.assertEquals(3, properties.size());
        Assert.assertEquals("system|/=/v=1/sg-name=GA|", properties.get("instance"));
        Assert.assertEquals("GA", properties.get("sg-name"));
        Assert.assertEquals("sg-name=GA", properties.get("label"));

        format = Sftp3gppUtils.get3gppFormat("gbBssgp");
        Assert.assertEquals("nse|/=/v=1/nse-id=<nseNumber>|/=/v=1/sg-name=<sgsnGtlSgName>/su-number=<n>", format);
        properties = Sftp3gppUtils.get3gppProperties(format, "nse|/=/v=1/nse-id=1201|/=/v=1/sg-name=GB71/su-number=1");
        Assert.assertEquals(5, properties.size());
        Assert.assertEquals("nse|/=/v=1/nse-id=1201|/=/v=1/sg-name=GB71/su-number=1", properties.get("instance"));
        Assert.assertEquals("1201", properties.get("nse-id"));
        Assert.assertEquals("GB71", properties.get("sg-name"));
        Assert.assertEquals("1", properties.get("su-number"));
        Assert.assertEquals("nse-id=1201, sg-name=GB71, su-number=1", properties.get("label"));

        format = Sftp3gppUtils.get3gppFormat("platformSystemFilesystem");
        Assert.assertEquals("disk|/=/v=1/frame=<frame>/shelf=<shelf>/slot=<slot>/sub-slot=<sub-slot>/name=<directory path>|", format);
        properties = Sftp3gppUtils.get3gppProperties(format, "disk|/=/v=1/frame=0/shelf=0/slot=2/sub-slot=0/name=\\/opt\\/hitachi\\/agw\\/data\\/trace|");
        Assert.assertEquals(7, properties.size());
        Assert.assertEquals("0", properties.get("frame"));
        Assert.assertEquals("0", properties.get("shelf"));
        Assert.assertEquals("2", properties.get("slot"));
        Assert.assertEquals("0", properties.get("sub-slot"));
        Assert.assertEquals("/opt/hitachi/agw/data/trace", properties.get("name"));
        Assert.assertEquals("frame=0, shelf=0, slot=2, sub-slot=0, name=/opt/hitachi/agw/data/trace", properties.get("label"));
    }

    /**
     * Test NMS-6365 (measObjLdn without PM Group information)
     *
     * @throws Exception the exception
     */
    @Test
    public void testNMS6365() throws Exception {
        String format = Sftp3gppUtils.get3gppFormat("dnsDns");
        Map<String,String> properties = Sftp3gppUtils.get3gppProperties(format, "system|/service=callp1|");
        assertEquals("system|/service=callp1|", properties.get("label"));

        System.setProperty("org.opennms.collectd.xml.3gpp.useSimpleParserForMeasObjLdn", "true");
        properties = Sftp3gppUtils.get3gppProperties(format, "system|/service=callp1|");
        assertEquals("/service=callp1", properties.get("label"));
    }

    @Test
    public void shouldSaveLastFileName() throws Exception {
        String filename = "myFile";
        String serviceName= "myServiceName";
        ResourcePath path = ResourcePath.fromString("aa/bb/cc");
        String targetPath = "dd/ee/ff";
        BlobStore blobStore = InMemoryMapBlobStore.withDefaultTicks();
        Sftp3gppUtils.setLastFilename(blobStore, serviceName, path, targetPath, filename);
        assertEquals(filename, Sftp3gppUtils.getLastFilename(blobStore, serviceName, path, targetPath));
    }
}
