<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

--%>

<%@ include file="/WEB-INF/jsp/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Acl Login</title>
	<style type="text/css">input{border: 1px solid #808080}</style>
</head>
<body>
<table align="center" width="100%" height="100%">
    <tr>
	   <td>
	       <table align="center" border="0" cellpadding="0" cellspacing="0" width="395" style="border: 1px solid rgb(128, 128, 128);">
		      <tr>
			     <td colspan="2" align="center">
				    <form id="loginForm" method="post" action="j_security_check">
					   <table style="height: 106px" border="0" cellpadding="0" cellspacing="0" width="300">
					       <tr>
					  		   <td align="right" valign="middle" width="63">username</td>
					  		   <td align="right"><input name="j_username" id="j_username" style="width: 97%;" type="text"/></td>
					  	   </tr>
					  	   <tr>
					  		   <td align="right" valign="middle">password</td>
					  		   <td align="right"><input style="width: 97%;" type="password" name="j_password" id="j_password"/></td>
					  	   </tr>
					  	   <tr align="right" valign="middle">
					  	       <td><div align="right"></div></td>
					  		   <td width="329"><input type="submit" value="Login"></td>
					  	   </tr>
					   </table>
					</form>
				</td>
			</tr>
		   </table>
		</td>
	</tr>
</table>
</body>
</html>