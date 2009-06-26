<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Oct 07: Corrected issue with selecting non-IP interface SNMP data.
// 2003 Sep 25: Fixed SNMP Performance link issue.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 01: Added response time link (Bug #684) and HTTP link (Bug #469).
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

--%>



<%@page language="java"
		contentType="text/html"
		session="true"
		import="org.opennms.netmgt.config.PollerConfigFactory,
				org.opennms.netmgt.config.PollerConfig,
				java.util.*,
                org.opennms.core.utils.SIUtils,
                org.opennms.netmgt.model.OnmsResource,
                org.opennms.web.Util,
                org.opennms.web.springframework.security.Authentication,
                org.opennms.web.element.*,
                org.opennms.web.event.*,
                org.opennms.web.svclayer.ResourceService,
                org.opennms.netmgt.utils.IfLabel,
                org.springframework.web.context.WebApplicationContext,
                org.springframework.web.context.support.WebApplicationContextUtils"
%>

<%!protected int telnetServiceId;
    protected int httpServiceId;
    private WebApplicationContext m_webAppContext;
    private ResourceService m_resourceService;
    
    public void init() throws ServletException {
        try {
            this.telnetServiceId = NetworkElementFactory.getServiceIdFromName("Telnet");            
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the Telnet service ID", e );
        }

        try {
            this.httpServiceId = NetworkElementFactory.getServiceIdFromName("HTTP");
        }
        catch( Exception e ) {
            throw new ServletException( "Could not determine the HTTP service ID", e );
        }

	    m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) m_webAppContext.getBean("resourceService", ResourceService.class);
    }%>

<%
    Interface intf_db = ElementUtil.getInterfaceByParams(request);
    int nodeId = intf_db.getNodeId();
    String ipAddr = intf_db.getIpAddress();
	int ifIndex = -1;    
	if (intf_db.getIfIndex() > 0) {
		ifIndex = intf_db.getIfIndex();
	}
    
    AtInterface atif_db = NetworkElementFactory.getAtInterface(nodeId, ipAddr);


    Service[] services = NetworkElementFactory.getServicesOnInterface( nodeId, ipAddr );
    if( services == null ) {
        services = new Service[0];
    }

    String eventUrl1 = "event/list.htm?filter=node%3D" + nodeId + "&filter=interface%3D" + ipAddr;
    String eventUrl2 = "event/list.htm?filter=node%3D" + nodeId + "&filter=ifindex%3D" + ifIndex;
    
    String telnetIp = null;
    Service telnetService = NetworkElementFactory.getService(nodeId, ipAddr, this.telnetServiceId);
    
    if( telnetService != null  ) {
        telnetIp = ipAddr;
    }    

    String httpIp = null;
    Service httpService = NetworkElementFactory.getService(nodeId, ipAddr, this.httpServiceId);

    if( httpService != null  ) {
        httpIp = ipAddr;
    }
    PollerConfigFactory.init();
    PollerConfig pollerCfgFactory = PollerConfigFactory.getInstance();
    pollerCfgFactory.rebuildPackageIpListMap();
%>

<%
String nodeBreadCrumb = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>";
%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Interface" />
  <jsp:param name="headTitle" value="<%= ipAddr %>" />
  <jsp:param name="headTitle" value="Interface" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
  <jsp:param name="breadcrumb" value="Interface" />
</jsp:include>

<%
if (request.isUserInRole( Authentication.ADMIN_ROLE )) {
%>

<script type="text/javascript" >
function doDelete() {
     if (confirm("Are you sure you want to proceed? This action will permanently delete this interface and cannot be undone."))
     {
         document.forms["delete"].submit();
     }
     return false;
}
</script>
<%
}
%>


      <h2>Interface: <%=intf_db.getIpAddress()%>
        <% if (intf_db.getHostname() != null && !intf_db.getIpAddress().equals(intf_db.getHostname())) { %>
          <%= intf_db.getHostname() %>
        <% } %>
      </h2>

        <%
        if (request.isUserInRole( Authentication.ADMIN_ROLE )) {
        %>
      <form method="post" name="delete" action="admin/deleteInterface">
      <input type="hidden" name="node" value="<%=nodeId%>"/>
      <input type="hidden" name="ifindex" value="<%=(ifIndex != -1 ? "" : ifIndex)%>"/>
      <input type="hidden" name="intf" value="<%=ipAddr%>"/>
      <%
      }
      %>

      <div id="linkbar">
      <ul>
        <%
        if (! ipAddr.equals("0.0.0.0")) {
        %>
	<li>
        <a href="<%=eventUrl1%>">View Events by Ip address</a>
	</li>
		<% } %>
		
        <%
        if (ifIndex > 0 ) {
        %>
	<li>
        <a href="<%=eventUrl2%>">View Events by ifIndex</a>
	</li>
		<% } %>
		
        <%
        if( telnetIp != null ) {
        %>
	  <li>
          <a href="telnet://<%=telnetIp%>">Telnet</a>
	  </li>
        <%
        }
        %>
        
        <%
                if( httpIp != null ) {
                %>
	  <li>
          <a href="http://<%=httpIp%>">HTTP</a>
	  </li>
        <%
        }
        %>

      <%
                          String ifLabel;
                          if (ifIndex != -1) {
                              ifLabel = IfLabel.getIfLabelfromIfIndex(nodeId, ipAddr, ifIndex);
                          } else {
                              ifLabel = IfLabel.getIfLabel(nodeId, ipAddr);
                          }

                          List<OnmsResource> resources = m_resourceService.findNodeChildResources(nodeId);
                          for (OnmsResource resource : resources) {
                              if (resource.getName().equals(ipAddr) || resource.getName().equals(ifLabel)) {
                                  out.println("<li>");
                                  out.println("<a href=\"graph/results.htm?reports=all"
                                              + "&amp;resourceId="
                                              + Util.encode(resource.getId())
                                              + "\">" + Util.htmlify(resource.getResourceType().getLabel())
                                              + " Graphs</a>");
                                  out.println("</li>");
                              }
                          }
      %>
        
        <% if (request.isUserInRole( Authentication.ADMIN_ROLE )) { %>
	 <li>
         <a href="admin/deleteInterface" onClick="return doDelete()">Delete</a>
	 </li>
         <% } %>
         
        <% if (request.isUserInRole( Authentication.ADMIN_ROLE )) { %>
	  <li>
            <a href="element/rescan.jsp?node=<%=nodeId%>&ipaddr=<%=ipAddr%>">Rescan</a>      
          </li>
         <% } %>

        <% if (request.isUserInRole( Authentication.ADMIN_ROLE ) && hasSNMPData(intf_db) && "P".equals(intf_db.getIsSnmpPrimary())) { %>
	 <li>
         <a href="admin/updateSnmp.jsp?node=<%=nodeId%>&ipaddr=<%=ipAddr%>">Update SNMP</a>
	 </li>
         <% } %>
      </ul>
      </div>

      <% if (request.isUserInRole( Authentication.ADMIN_ROLE )) { %>
      </form>
      <% } %>

	<div id="contentleft">

        <h3>General</h3>
            <!-- general info box -->
	    <table>
              <tr>
                <th>Node</th>
                <td><a href="element/node.jsp?node=<%=intf_db.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(intf_db.getNodeId())%></a></td>
              </tr>
              <tr> 
                <th>Polling Status</th>
                <td><%=ElementUtil.getInterfaceStatusString(intf_db)%></td>
              </tr>
              <% if(ElementUtil.getInterfaceStatusString(intf_db).equals("Managed") && request.isUserInRole( Authentication.ADMIN_ROLE )) {
                  List inPkgs = pollerCfgFactory.getAllPackageMatches(ipAddr);
                  Iterator pkgiter = inPkgs.iterator();
                  while (pkgiter.hasNext()) { %>
                      <tr>
                          <th>Polling Package</th>
                          <td><%= (String) pkgiter.next()%></td>
                      </tr>
                  <% } %>
              <% } %>
              <tr>
                <th>Interface Index</th>
                <td>
                  <% if( ifIndex != -1 ) {  %>
                    <%=ifIndex%>
                  <% } else { %>
                    &nbsp;
                  <% } %>
                </td>
              </tr>
              <tr> 
                <th>Last Service Scan</th>
                <td><%=intf_db.getLastCapsdPoll()%></td>
              </tr>
              <tr>
                <th>Physical Address</th>
                <td>
                  <% String macAddr = intf_db.getPhysicalAddress(); %>
                  <% if( macAddr != null && macAddr.trim().length() > 0 && !macAddr.equals("000000000000")) { %>
                    <%=macAddr%>
                  <% } else if ( atif_db != null && atif_db.get_physaddr().trim().length() > 0 ) { %>
                  <%=atif_db.get_physaddr()%>
                  <% } else { %>
                    &nbsp;
                  <% } %>
                </td>
              </tr>
            </table>
            
            <!-- Node Link box -->
            <jsp:include page="/includes/interfaceLink-box.jsp" flush="false" />
                        

            <!-- SNMP box, if info available -->
            <% if( hasSNMPData(intf_db) ) { %>
              <h3>SNMP Attributes</h3>
		  <table>
                    <tr> 
                      <th>Subnet Mask</th>
                      <td>
                        <%=(intf_db.getSnmpIpAdEntNetMask() == null) ? "&nbsp;" : intf_db.getSnmpIpAdEntNetMask()%>                                    
                      </td>
                    </tr>
                    <tr>
                      <th>Interface Type</th>
                      <td><%=getIfTypeString(intf_db.getSnmpIfType())%></td>
                    </tr>
                    <tr> 
                      <th>Status (Adm/Op)</th>
                      <td>
                        <% if( intf_db.getSnmpIfAdminStatus() < 1 || intf_db.getSnmpIfOperStatus() < 1 ) { %>
                          &nbsp;
                        <% } else { %>
                          <%=getIfStatusString(intf_db.getSnmpIfAdminStatus())%>/<%=getIfStatusString(intf_db.getSnmpIfOperStatus())%>
                        <% } %>
                      </td>
                    </tr>
                    <tr>
                      <th>Speed</th>
                      <td><%=(intf_db.getSnmpIfSpeed() > 0) ? SIUtils.getHumanReadableIfSpeed(intf_db.getSnmpIfSpeed()) : "&nbsp;"%></td>
                    </tr>
                    <tr> 
                      <th>Description</th>
                      <td><%=(intf_db.getSnmpIfDescription() == null) ? "&nbsp;" : intf_db.getSnmpIfDescription()%></td>
                    </tr>
                    <tr>
                      <th>Alias</th>
                      <td><%=(intf_db.getSnmpIfAlias() == null) ? "&nbsp;" : intf_db.getSnmpIfAlias()%></td>
                    </tr>

                  </table>
            <% } %>

            <!-- services box -->
            
            <h3>Services</h3>
            
		    <table>
              <% for( int i=0; i < services.length; i++ ) { %>
                <tr>
                  <td><a href="element/service.jsp?node=<%=services[i].getNodeId()%>&intf=<%=services[i].getIpAddress()%>&service=<%=services[i].getServiceId()%>"><%=services[i].getServiceName()%></a></td>
                </tr>
              <% } %>
            </table>

            <!-- Availability box -->
            <jsp:include page="/includes/interfaceAvailability-box.jsp" flush="false" />

</div>
       

<div id="contentright">

            <!-- interface desktop information box -->
          
            <!-- events list box 1 using ipaddress-->
            <% if (!ipAddr.equals("0.0.0.0")) {
            	String eventHeader1 = "<a href='" + eventUrl1 + "'>Recent Events (Using Filter Ip address:" + ipAddr + ")</a>"; %>
            <% String moreEventsUrl1 = eventUrl1; %>
            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="ipAddr" value="<%=ipAddr%>" />
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<%=eventHeader1%>" />
              <jsp:param name="moreUrl" value="<%=moreEventsUrl1%>" />
            </jsp:include>
            <% } %>
            <!-- events list box 2 using ifindex -->
			<% if (ifIndex > 0 ) { %>
            <% String eventHeader2 = "<a href='" + eventUrl1 + "'>Recent Events (Using Filter ifIndex = " + ifIndex + ")</a>"; %>
            <% String moreEventsUrl2 = eventUrl2; %>
            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<%=eventHeader2%>" />
              <jsp:param name="moreUrl" value="<%=moreEventsUrl2%>" />
              <jsp:param name="ifIndex" value="<%=ifIndex%>" />
            </jsp:include>
            <% } %>
            <!-- Recent outages box -->
            <jsp:include page="/outage/interfaceOutages-box.htm" flush="false" />
            
            <!-- STP Info box -->
            <jsp:include page="/includes/interfaceSTP-box.jsp" flush="false" />
         


</div> <!-- id="contentright" -->

<jsp:include page="/includes/footer.jsp" flush="false" />


<%! // from http://www.iana.org/assignments/ianaiftype-mib
  public static final String[] IFTYPES = new String[] {
    "&nbsp;",                     //0 (not supported)
    "other",                    //1
    "regular1822",              //2
    "hdh1822",                  //3
    "ddn-x25",                  //4
    "rfc877-x25",               //5
    "ethernetCsmacd",           //6
    "iso88023Csmacd",           //7
    "iso88024TokenBus",         //8
    "iso88025TokenRing",        //9
    "iso88026Man",              //10
    "starLan",                  //11
    "proteon-10Mbit",           //12
    "proteon-80Mbit",           //13
    "hyperchannel",             //14
    "fddi",                     //15
    "lapb",                     //16
    "sdlc",                     //17
    "ds1",                      //18
    "e1",                       //19
    "basicISDN",                //20
    "primaryISDN",              //21
    "propPointToPointSerial",   //22
    "ppp",                      //23
    "softwareLoopback",         //24
    "eon",                      //25
    "ethernet-3Mbit",           //26
    "nsip",                     //27
    "slip",                     //28
    "ultra",                    //29
    "ds3",                      //30
    "sip",                      //31
    "frame-relay",              //32
    "rs232",                    //33
    "para",                     //34
    "arcnet",                   //35
    "arcnetPlus",               //36
    "atm",                      //37
    "miox25",                   //38
    "sonet",                    //39
    "x25ple",                   //40
    "is0880211c",               //41
    "localTalk",                //42
    "smdsDxi",                  //43
    "frameRelayService",        //44
    "v35",                      //45
    "hssi",                     //46
    "hippi",                    //47
    "modem",                    //48
    "aa15",                     //49
    "sonetPath",                //50
    "sonetVT",                  //51
    "smdsIcip",                 //52
    "propVirtual",              //53
    "propMultiplexor",          //54
    "ieee80212",                //55
    "fibreChannel",             //56
    "hippiInterface",           //57
    "frameRelayInterconnect",   //58
    "aflane8023",               //59
    "aflane8025",               //60
    "cctEmul",                  //61
    "fastEther",                //62
    "isdn",                     //63
    "v11",                      //64
    "v36",                      //65
    "g703at64k",                //66
    "g703at2mb",                //67
    "qllc",                     //68
    "fastEtherFX",              //69
    "channel",                  //70
    "ieee80211",                //71
    "ibm370parChan",            //72
    "escon",                    //73
    "dlsw",                     //74
    "isdns",                    //75
    "isdnu",                    //76
    "lapd",                     //77
    "ipSwitch",                 //78
    "rsrb",                     //79
    "atmLogical",               //80
    "ds0",                      //81
    "ds0Bundle",                //82
    "bsc",                      //83
    "async",                    //84
    "cnr",                      //85
    "iso88025Dtr",              //86
    "eplrs",                    //87
    "arap",                     //88
    "propCnls",                 //89
    "hostPad",                  //90
    "termPad",                  //91
    "frameRelayMPI",            //92
    "x213",                     //93
    "adsl",                     //94
    "radsl",                    //95
    "sdsl",                     //96
    "vdsl",                     //97
    "iso88025CRFPInt",          //98
    "myrinet",                  //99
    "voiceEM",                  //100
    "voiceFXO",                 //101
    "voiceFXS",                 //102
    "voiceEncap",               //103
    "voiceOverIp",              //104
    "atmDxi",                   //105
    "atmFuni",                  //106
    "atmIma",                   //107
    "pppMultilinkBundle",       //108
    "ipOverCdlc",               //109
    "ipOverClaw",               //110
    "stackToStack",             //111
    "virtualIpAddress",         //112
    "mpc",                      //113
    "ipOverAtm",                //114
    "iso88025Fiber",            //115
    "tdlc",                     //116
    "gigabitEthernet",          //117
    "hdlc",                     //118
    "lapf",                     //119
    "v37",                      //120
    "x25mlp",                   //121
    "x25huntGroup",             //122
    "trasnpHdlc",               //123
    "interleave",               //124
    "fast",                     //125
    "ip",                       //126
    "docsCableMaclayer",        //127
    "docsCableDownstream",      //128
    "docsCableUpstream",        //129
    "a12MppSwitch",             //130
    "tunnel",                   //131
    "coffee",                   //132
    "ces",                      //133
    "atmSubInterface",          //134
    "l2vlan",                   //135
    "l3ipvlan",                 //136
    "l3ipxvlan",                //137
    "digitalPowerline",         //138
    "mediaMailOverIp",          //139
    "dtm",                      //140
    "dcn",                      //141
    "ipForward",                //142
    "msdsl",                    //143
    "ieee1394",                 //144
    "if-gsn",                   //145
    "dvbRccMacLayer",           //146
    "dvbRccDownstream",         //147
    "dvbRccUpstream",           //148
    "atmVirtual",               //149
    "mplsTunnel",               //150
    "srp",                      //151
    "voiceOverAtm",             //152
    "voiceOverFrameRelay",      //153
    "idsl",                     //154
    "compositeLink",            //155
    "ss7SigLink",               //156
    "propWirelessP2P",          //157
    "frForward",                //158
    "rfc1483",                  //159
    "usb",                      //160
    "ieee8023adLag",            //161
    "bgppolicyaccounting",      //162
    "frf16MfrBundle",           //163
    "h323Gatekeeper",           //164
    "h323Proxy",                //165
    "mpls",                     //166
    "mfSigLink",                //167
    "hdsl2",                    //168
    "shdsl",                    //169
    "ds1FDL",                   //170
    "pos",                      //171
    "dvbAsiIn",                 //172
    "dvbAsiOut",                //173
    "plc",                      //174
    "nfas",                     //175
    "tr008",                    //176
    "gr303RDT",                 //177
    "gr303IDT",                 //178
    "isup",                     //179
    "propDocsWirelessMaclayer",      //180
    "propDocsWirelessDownstream",    //181
    "propDocsWirelessUpstream",      //182
    "hiperlan2",                //183
    "propBWAp2Mp",              //184
    "sonetOverheadChannel",     //185
    "digitalWrapperOverheadChannel", //186
    "aal2",                     //187
    "radioMAC",                 //188
    "atmRadio",                 //189
    "imt",                      //190
    "mvl",                      //191
    "reachDSL",                 //192
    "frDlciEndPt",              //193
    "atmVciEndPt",              //194
    "opticalChannel",           //195
    "opticalTransport",         //196
    "propAtm",                  //197
    "voiceOverCable",           //198
    "infiniband",               //199
    "teLink",                   //200
    "q2931",                    //201
    "virtualTg",                //202
    "sipTg",                    //203
    "sipSig",                   //204
    "docsCableUpstreamChannel", //205
    "econet",                   //206
    "pon155",                   //207
    "pon622",                   //208
    "bridge",                   //209
    "linegroup",                //210
    "voiceEMFGD",               //211
    "voiceFGDEANA",             //212
    "voiceDID",                 //213
    "mpegTransport",            //214
    "sixToFour",                //215
    "gtp",                      //216
    "pdnEtherLoop1",            //217
    "pdnEtherLoop2",            //218
    "opticalChannelGroup",      //219
    "homepna",                  //220
    "gfp",                      //221
    "ciscoISLvlan",             //222
    "actelisMetaLOOP",          //223
    "fcipLink",                 //224
    "rpr",                      //225
    "qam",                      //226
    "lmp",                      //227
    "cblVectaStar",             //228
    "docsCableMCmtsDownstream", //229
    "adsl2",                    //230
    "macSecControlledIF",       //231
    "macSecUncontrolledIF",     //232
    "aviciOpticalEther",        //233
    "atmbond",                  //234
    "voiceFGDOS",               //235
    "mocaVersion1",             //236
    "ieee80216WMAN",            //237
    "adsl2plus",                //238
    "dvbRcsMacLayer",           //239
    "dvbTdm",                   //240
    "dvbRcsTdma",               //241
    "x86Laps",                  //242
    "wwanPP",                   //243
    "wwanPP2",                  //244
    "voiceEBS",                 //245
    "ifPwType",                 //246
    "ilan",                     //247
    "pip",                      //248
    "aluELP",                   //249
    "gpon",                     //250
  };


  public static final String[] OPER_ADMIN_STATUS = new String[] {
    "&nbsp;",          //0 (not supported)
    "Up",              //1
    "Down",            //2
    "Testing",         //3
    "Unknown",         //4
    "Dormant",         //5
    "NotPresent",      //6
    "LowerLayerDown"   //7
  };

  public String getIfTypeString(int ifTypeNum) {
      if (ifTypeNum < IFTYPES.length) {
          return IFTYPES[ifTypeNum];
      } else {
          return "Unknown (" + ifTypeNum + ")";
      }
  }
  
  public String getIfStatusString(int ifStatusNum) {
      if (ifStatusNum < OPER_ADMIN_STATUS.length) {
          return OPER_ADMIN_STATUS[ifStatusNum];
      } else {
          return "Unknown (" + ifStatusNum + ")";
      }
  }
  
  private boolean hasSNMPData(Interface intf_db)
  {
      if (intf_db.getSnmpIpAdEntNetMask() != null)
          return true;
      
      if (intf_db.getSnmpIfType() > 0)
          return true;
      
      if (intf_db.getSnmpIfSpeed() > 0)
          return true;
          
      if (intf_db.getSnmpIfDescription() != null)
          return true;
          
      if (intf_db.getSnmpIfAdminStatus() > 0)
          return true;
      
      if (intf_db.getSnmpIfOperStatus() > 0)
          return true;

      if (intf_db.getSnmpIfAlias() != null)
          return true;
      
      return false;
  }%>
