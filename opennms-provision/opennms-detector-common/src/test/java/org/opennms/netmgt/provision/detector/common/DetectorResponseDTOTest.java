package org.opennms.netmgt.provision.detector.common;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DetectorResponseDTOTest
        extends XmlTestNoCastor<DetectorResponseDTO> {

    public DetectorResponseDTOTest(DetectorResponseDTO sampleObject,
            String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getDetectorResponse(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<detector-response isDetected=\"true\" failureMessage=\"classCast exception\"/>"
                }
        });
    }


    public static DetectorResponseDTO getDetectorResponse() {
        
        DetectorResponseDTO response = new DetectorResponseDTO();
        response.setDetected(true);
        response.setFailureMesage("classCast exception");
        return response;
 
    }
}
