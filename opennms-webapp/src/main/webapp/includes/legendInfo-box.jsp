<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">



<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en' xmlns:opennms='xsds/coreweb.xsd'>
<head>
  <title>
    
    OpenNMS Web Console
  </title>
  <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css"/>
  <meta http-equiv="Content-Script-Type" content="text/javascript"/>

  
  
    
        <base href="http://localhost:8980/opennms/" />
    
  
  <!--   -->
  
    
        <link rel="stylesheet" type="text/css" href="http://localhost:8980/opennms/css/styles.css" media="screen" />
        <link rel="stylesheet" type="text/css" href="http://localhost:8980/opennms/css/print.css" media="print" />
        
        	
  <script type="text/javascript" src="http://localhost:8980/opennms/js/global.js"></script>

    
        <script type="text/javascript" src="http://localhost:8980/opennms/coreweb/coreweb.nocache.js"></script>


</head>



<TITLE>
Additional Legend Information
</TITLE>

<div id="header">
<h1><a href="http://localhost:8980/opennms/index.jsp"><img alt="OpenNMS Web Console Home" src="http://localhost:8980/opennms/images/logo.png"></a></h1>
</div>

<div id="content">
<BODY>
<h2>
<P>
Each status cell is an intersection of a Location and Application
</P>
<br/>
<P>
An Application is defined by a subset of the set of IP based services created in OpenNMS
</P>
<p>
</p>
<P>
A Location is an arbitrary entity defined through configuration by the OpenNMS user
</P>
<br/>
<P>
Each Location presents Availability as the best percentage possible based on the history of status<br/>
of services monitored from *all* remote pollers in that Location since midnight of the current day.<br/>
If there were 2 services being monitored by 2 remote pollers and each 1 service down, uniquely, then<br/>
 the availability would still be 100%.
</P>
<br/>
<P>
Each Location presents Status as the worst known status of all remote pollers in a Started state.
<P>
</h2>
</BODY>
</div>

</HTML>
