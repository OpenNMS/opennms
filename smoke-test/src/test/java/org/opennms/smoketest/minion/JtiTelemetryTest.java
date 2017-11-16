package org.opennms.smoketest.minion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;

public class JtiTelemetryTest {
    
    @Test
    public void verifyProto() throws IOException {
        
        byte[] jtiOutBytes = Files.readAllBytes(Paths.get("/home/chandra/dev/opennms/smoke-test/src/test/resources/telemetry/jti-proto.raw"));
        
        //System.out.println(Resources.getResource("/telemetry/jti-proto.raw").toString());
        byte[] jtiMsgBytes = Resources.toByteArray(Resources.getResource("telemetry/jti-proto.raw"));
        
        Assert.assertArrayEquals(jtiOutBytes, jtiMsgBytes);
    }

}
