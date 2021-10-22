/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.backup.minion;

import com.jcraft.jsch.*;
import org.opennms.features.backup.api.BackupStrategy;
import org.opennms.features.backup.api.Config;
import org.opennms.features.backup.api.ConfigType;
import org.opennms.features.backup.api.Const;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CiscoBackupStrategy implements BackupStrategy {

    @Override
    public Config getConfig(String ipAddress, int port, Map<String, String> params) {
        Config config = new Config();
        try {
            Session session = loginToDevice(ipAddress, port, params.get(Const.DEVICE_USER), params.get(Const.DEVICE_KEY));
            byte[] result = executeCommand(session, "show run");
            session.disconnect();
            config.setData(result);
            config.setType(ConfigType.TEXT);
            config.setRetrievedAt(new Date());
        } catch (JSchException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            config.setMessage(e.getMessage());
        }
        return config;
    }

    private byte[] executeCommand(Session session, String command) throws JSchException, IOException {
        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec)channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        channel.connect();
        System.out.println("executing exec command .....");

        byte[] tmp = new byte[1024];
        while(true){
            while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;
            }
            if(channel.isClosed()){
                if(in.available()>0) continue;
                System.out.println("exit-status: "+channel.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        channel.disconnect();
        return tmp;
    }

    private Session loginToDevice(String ipAddress, int port, String user, String key) throws JSchException {
        JSch jsch = new JSch();
        String keyFileName = createKeyFile(key);
        jsch.addIdentity(keyFileName);
        Session session = jsch.getSession(user, ipAddress, port);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        System.out.println("session created.");
        session.connect();
        System.out.println("session connected.....");
        new File(keyFileName).delete();
        return session;
    }

    private String createKeyFile(String keyStr){
        String keyFileName = "key";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(keyFileName));
            writer.write(keyStr);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyFileName;
    }

    public static void main(String[] args) {
        CiscoBackupStrategy ciscoBackupStrategy = new CiscoBackupStrategy();
        Map params = new HashMap();
        params.put(Const.DEVICE_USER, "azureuser");
        params.put(Const.DEVICE_KEY, "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIG5QIBAAKCAYEAyWb6YdbfCnLUSGN0LGF95rB6fuxG0umUt8d75UWkzVf9/ujx\n" +
                "Vr9RH3PPyDzPToohoFI+RurNC2xx5IdVTKcILSvY7cg6EHP1zFFDLseLObSky3Zm\n" +
                "PstTmlwYDzp1VZKfp2Pne+plSA3a/MLRytviBQp2Xd549pb+CKZgicqn1PLRBZY3\n" +
                "TQPOt7Xxr6+i1dy3/RPnywHjd2oqTwt/ii1DIR+v+qHYUBF/LObNv+FmzVaG1O/8\n" +
                "zTFaITNGGpDAhT1cbGC9BrXkdacKKt3555o8ZYD7B6+tKXLMdnzw66EkI5r2gr+o\n" +
                "4LX6RwFY9PCBqYb4WPBUkioPgpC1gLLHX2Fiw3je9EVmHggBUFv7fpulSQpIJYXR\n" +
                "ybOIQfn72aXccNfwnu8xXBUNfEH69EpN0ls0mNNc+duws5V32qMNJV0ZTXfrXgdN\n" +
                "adPQrK3/iPBLLPbMqcxUAaXhx+M8sKS73SrePNNo5hvBpio1GFVlhRHS43u0olBE\n" +
                "HxG8E6P2DRHrcxJVAgMBAAECggGBALmu7Y0i04QIllsfBk6cm70CXA2EvU9a9zfw\n" +
                "/PNGoEiJpa1NCqSVwq8i0SCln+OjSplK+gvcEqtDkkXF2AjRvqIW2OtxPMdKggWb\n" +
                "rjsE0gGj4IONZ3rhbvTTj4MKvH7mrbdgTl1an4Is9AcxfsZurxVCrhqhMBj44MEM\n" +
                "09h2NQIJ1Fg4REgHCKErZ+Y4IgLWefMkVQI/fX3tLZCu4yl7zXZOhqqmaI55cyzs\n" +
                "pikQIP2Tlr/fes2bgKM3aJy5dCXjiRxxo2HGgN9AcBu7g3BnoD4294eSjwEBAMM3\n" +
                "tRqt8tvI2SSC+toSnZKI9TN85OEvZ5IMFrMrPGODoqr/hQCICz5cgOjETGkIJECS\n" +
                "ilOtEz8/iF3YgAn/ADSiSPnJ9Gkxp/kxkejhk9GoEELZEGh2PiNVymU8bDGvPcKN\n" +
                "CppS//yebm+lKNM60+aY0wjIFNcPY0e1ThXBv9ahd6vIvV4BdLdtxJVGPyEYpj1M\n" +
                "mu5dpQW/HhX4Ud3iWkoOSid+oRM8yQKBwQD2bleJBfz/xkb63SoMwX+SPnSyoYJe\n" +
                "Zg2zYIWV9yRq29kGGG8qHeBtojmQKIlXKTMBhdkdnehE8hT7b6deoV+RcJy+OVf5\n" +
                "WH6Dw/Jg62+KeVQX//4mXM2z3o4XS/AljZKK0q+WXaGjHFlk2wepNKmpNaiOigao\n" +
                "FPhsvPGHkk6z/slq3ZUiSMmyYWiNcW5qywJJjUkHPCrUkjmPrNlz5Fc9gxyDdjsS\n" +
                "8SqOebvliHsgvQ3Jo0+p9WIOyzEaCHL8KLMCgcEA0TkGmJmmPTm7HH+MclgR1ZxF\n" +
                "aaVpk7DbYq+3gwlUlCqrGkoivIuCsJOnBbsYyvJC6oNQywwxe3X6Tjl5Mn6yqMrv\n" +
                "84GW5TCGzeMTP+hlNoAKmHitC+rOb6ACrSEL5XHD8joRLg5GKzUHOWuvMdeDndHC\n" +
                "kxDZ3seryB2FbjsTVvPcmblzB8vwk1tv9DzAeg8A+d9iPqK8csOtcg9pPfz45cNn\n" +
                "0aHN0zV8Kbyl0qXKryCgMsiY0Csnjl1StRc2aYzXAoHBAJICAp9PCv3IztloHLf+\n" +
                "FcXid2Y3R3UC5m83ay7VhaBCri1sYMEFfqm2ioCdY/JemLf4RBHGM45WGJR5LSNm\n" +
                "Xadgn54df8Be7GbJvFwsYqQbCvZFWKIGLmK6JFotRhYZ8Y+LETn4NALekyCsnbdl\n" +
                "WnjFMB90LEl+U0qqEDkROMxw04oby3bKxaaJDFdxs8hWi/5kVbaRsZbNJIm6EgUW\n" +
                "b5IkkspZTNsGRP6xpeBzovBc00R7HIJIv1agkXEpPGem/QKBwFF/HQqkDxUaP8BC\n" +
                "Drsbgxbp4D/eptlYspwmRh1MrR1p4WxEJHO9BBMz8INS9b1X0Kj4rafp//xPYEjI\n" +
                "KmV45K7LHdyERfrQhDH+majzJTmtdnMdzoot1Gu2sFQv4ypkvibEZ5zBAqQh68Z6\n" +
                "I6wOfr4izt/qWHNm44r3tNnTOR1NsEhQ7HXFAtjjbSB96Zuh7IdodCRhz3YEsrMT\n" +
                "PbNfrrQRAYeutzXmR48aZJ1A50qAMuaVLLEpnibeQzE28YL70QKBwQDnA0QGEtDO\n" +
                "OHbtdIWU+pj/+gT4Y7y+4yvEeshbVzl4SsE827kwcYIFz/Mq6SxvUu6oBiyKuWNN\n" +
                "N9JjQ5htNQym3n+/pIdh28IvW6oLRn58JXuiAspeYkP6F0B2G7W+bZO64EeLVOB9\n" +
                "Q/2R2N8bGfxGOufo/VwwfjGUlcSMaXfTw5ZikNPvbgipRsd9w5akRDDLAxPRicXF\n" +
                "oLSGKix8ToGR5/k9oEmyBe0qhCq4xHcY3mSG0PdReqd0dIuihWKoYm0=\n" +
                "-----END RSA PRIVATE KEY-----\n");
        Config config = ciscoBackupStrategy.getConfig("20.115.57.63", 22, params);
        System.out.println(config);
    }
}
