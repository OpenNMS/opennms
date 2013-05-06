package org.opennms.features.topology.plugins.ncs;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ServicePathTestServlet extends HttpServlet {
    
    private String m_responsePath1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
    		"<Data xmlns=\"services.schema.networkapi.jmp.juniper.net\">" +
    		"<ServiceResource>" +
    		"<ServicePath>" +
    		"<To>10.1.0.2</To>" +
    		"<Via>ge-1/0/2.0</Via>" +
    		"<MPLSLabel>Push 300528, Push 301888(top)</MPLSLabel>" +
    		"<LSPPath>" +
    		"<LSPNode>" +
    		"<Depth>1</Depth>" +
    		"<Parent>(null)</Parent>" +
    		"<Address>10.1.0.2</Address>" +
    		"<LabelValue>301888</LabelValue>" +
    		"<Status>Success</Status>" +
    		"<Device>" +
    		"<DeviceName>siegfried</DeviceName>" +
    		"<DeviceID>131103</DeviceID>" +
    		"</Device>" +
    		"</LSPNode>" +
    		"<LSPNode>" +
    		"<Depth>2</Depth>" +
    		"<Parent>10.1.0.2</Parent>" +
    		"<Address>10.1.3.2</Address>" +
    		"<LabelValue>3</LabelValue>" +
    		"<Interface>ge-1/0/2.0</Interface>" +
    		"<Status>Egress</Status>" +
    		"<Device>" +
    		"<DeviceName>froh</DeviceName>" +
    		"<DeviceID>688141</DeviceID>" +
    		"</Device>" +
    		"</LSPNode>" +
    		"</LSPPath>" +
    		"</ServicePath>" +
    		"</ServiceResource>" +
    		"<Status>" +
    		"<Code>200</Code>" +
    		"<Message>Operation Successful</Message>" +
    		"</Status>" +
    		"</Data>";
    
    private String m_responsePath2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Data xmlns=\"services.schema.networkapi.jmp.juniper.net\">" +
            "<ServiceResource>" +
            "<ServicePath>" +
            "<To>10.1.3.1</To>" +
            "<Via>ae2.0</Via>" +
            "<LSPPath>" +
            "<RSVPLSPName>froh-penelope</RSVPLSPName>" +
            "<LSPNode>" +
            "<Depth>1</Depth>" +
            "<Parent>(null)</Parent>" +
            "<Address>10.1.3.1</Address>" +
            "<LabelValue>303648</LabelValue>" +
            "<Status>Success</Status>" +
            "<Device>" +
            "<DeviceName>siegfried</DeviceName>" +
            "<DeviceID>688141</DeviceID>" +
            "</Device>" +
            "</LSPNode>" +
            "<LSPNode>" +
            "<Depth>2</Depth>" +
            "<Parent>10.1.3.1</Parent>" +
            "<Address>10.1.0.1</Address>" +
            "<LabelValue>3</LabelValue>" +
            "<Interface>ae2.0</Interface>" +
            "<Status>Egress</Status>" +
            "<Device>" +
            "<DeviceName>penelope</DeviceName>" +
            "<DeviceID>131088</DeviceID>" +
            "</Device>" +
            "</LSPNode>" +
            "<LSPNode>" +
            "<Depth>1</Depth>" +
            "<Parent>(null)</Parent>" +
            "<Address>10.1.3.12</Address>" +
            "<LabelValue>934750</LabelValue>" +
            "<Status>Success</Status>" +
            "<Device>" +
            "<DeviceName>siegfried</DeviceName>" +
            "<DeviceID>131103</DeviceID>" +
            "</Device>" +
            "</LSPNode>" +
            "</LSPPath>" +
            "</ServicePath>" +
            
            "</ServiceResource>" +
            "<Status>" +
            "<Code>200</Code>" +
            "<Message>Operation Successful</Message>" +
            "</Status>" +
            "</Data>";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/xml");
        
        if(req.getQueryString().equals("deviceA=131103&deviceZ=688141")) {
            out.write(m_responsePath1);
        } else if(req.getQueryString().equals("deviceA=688141&deviceZ=131103")) {
            out.write(m_responsePath2);
        }
        
    }

}
