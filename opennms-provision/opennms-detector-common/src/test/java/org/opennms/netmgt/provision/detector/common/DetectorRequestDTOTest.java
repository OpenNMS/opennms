package org.opennms.netmgt.provision.detector.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;


public class DetectorRequestDTOTest  extends XmlTestNoCastor<DetectorRequestDTO>{

    public DetectorRequestDTOTest(DetectorRequestDTO sampleObject,
            String sampleXml) {
        super(sampleObject, sampleXml, null);
    } 
    
    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getDetectorRequest(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<detector-request location=\"MINION\" serviceName=\"ICMP\" address=\"localhost\">\n"+
                    "<attributes>port=8980</attributes> \n"+
                    "<attributes>timeout=5000</attributes> \n" +
                    "<attributes>retries=10</attributes> \n" +
                 "</detector-request>"
                }
        });
    }

    public static DetectorRequestDTO getDetectorRequest() {
        
        DetectorRequestDTO dto = new DetectorRequestDTO();
        dto.setAddress("localhost");
        dto.setLocation("MINION");
        List<String> properties = Arrays.asList("port=8980", "timeout=5000", "retries=10");
        dto.setProperties(properties);
        dto.setServiceName("ICMP");
        return dto;
    }
}
