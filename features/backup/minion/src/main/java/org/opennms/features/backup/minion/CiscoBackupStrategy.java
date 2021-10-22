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
        params.put(Const.DEVICE_USER, "???");
        params.put(Const.DEVICE_KEY, "???");
        Config config = ciscoBackupStrategy.getConfig("???", 22, params);
        System.out.println(config);
    }
}
