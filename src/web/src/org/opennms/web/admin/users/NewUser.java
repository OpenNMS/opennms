//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

package org.opennms.web.admin.users;

/**
 * Add new user Bean, containing data from the user info page.
 */
public class NewUser {
    // Data from the Add New User page
    private String fullname;

    private String user;

    private String passwd;

    private String confirmpasswd;

    private String comments;

    private String email;

    private String numsvc;

    private String numpin;

    private String txtsvc;

    private String txtpin;

    private String dutysch;

    /**
     * Default Constructor
     */
    public NewUser() {
        fullname = "";
        user = "";
        passwd = "";
        confirmpasswd = "";
        comments = "";
        email = "";
        numsvc = "";
        numpin = "";
        txtsvc = "";
        txtpin = "";
        dutysch = "";
    }

    public NewUser(String fullName, String userId, String userPasswd, String confirm, String userComments, String userEmail, String numericSvc, String numericPin, String txtSvc, String txtPin, String duty) {
        fullname = fullName;
        user = userId;
        passwd = userPasswd;
        confirmpasswd = confirm;
        comments = userComments;
        email = userEmail;
        numsvc = numericSvc;
        numpin = numericPin;
        txtsvc = txtSvc;
        txtpin = txtPin;
        dutysch = duty;
    }

    public String getFullname() {
        return (this.fullname);
    }

    public String getUser() {
        return (this.user);
    }

    public String getPasswd() {
        return (this.passwd);
    }

    public String getConfirmpasswd() {
        return (this.confirmpasswd);
    }

    public String getComments() {
        return (this.comments);
    }

    public String getEmail() {
        return (this.email);
    }

    public String getNumsvc() {
        return (this.numsvc);
    }

    public String getNumpin() {
        return (this.numpin);
    }

    public String getTxtsvc() {
        return (this.txtsvc);
    }

    public String getTxtpin() {
        return (this.txtpin);
    }

    public String getDutysch() {
        return (this.dutysch);
    }

    public void setFullname(String name) {
        this.fullname = name;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public void setConfirmpasswd(String confirmPass) {
        this.confirmpasswd = confirmPass;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNumsvc(String num) {
        this.numsvc = num;
    }

    public void setNumpin(String pin) {
        this.numpin = pin;
    }

    public void setTxtsvc(String svc) {
        this.txtsvc = svc;
    }

    public void setTxtpin(String pin) {
        this.txtpin = pin;
    }

    public void setDutysch(String duty) {
        this.dutysch = duty;
    }

    public String toString() {
        StringBuffer str = new StringBuffer("Full Name : " + fullname + " \n");
        str.append("User Id " + user + " \n");
        str.append("Password " + passwd + " \n");
        str.append("Confirm " + confirmpasswd + " \n");
        str.append("Comments " + comments + " \n");
        str.append("Email " + email + " \n");
        str.append("Numeric svc" + numsvc + " \n");
        str.append("Numeric Pin " + numpin + " \n");
        str.append("Text Service : " + txtsvc + " \n");
        str.append("Text Pin " + txtpin + "\n");
        str.append("Duty Schedule " + dutysch + "\n");
        return str.toString();
    }

}
