<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

--%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>RAM Debug Page</title>
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
