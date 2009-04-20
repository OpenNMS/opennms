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
                    <div style="font-family:Verdana, Arial, Helvetica, sans-serif; color:#FF0000; font-size:11px; padding:5 0 5 0;">
                    <strong>wrong password or username</strong>
                    </div>
                </td>
            </tr>
           </table>
        </td>
    </tr>
</table>
</body>
</html>