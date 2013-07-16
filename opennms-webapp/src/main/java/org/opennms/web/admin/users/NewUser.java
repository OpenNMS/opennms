/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.users;

/**
 * Add new user Bean, containing data from the user info page.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
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

    /**
     * <p>Constructor for NewUser.</p>
     *
     * @param fullName a {@link java.lang.String} object.
     * @param userId a {@link java.lang.String} object.
     * @param userPasswd a {@link java.lang.String} object.
     * @param confirm a {@link java.lang.String} object.
     * @param userComments a {@link java.lang.String} object.
     * @param userEmail a {@link java.lang.String} object.
     * @param numericSvc a {@link java.lang.String} object.
     * @param numericPin a {@link java.lang.String} object.
     * @param txtSvc a {@link java.lang.String} object.
     * @param txtPin a {@link java.lang.String} object.
     * @param duty a {@link java.lang.String} object.
     */
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

    /**
     * <p>Getter for the field <code>fullname</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFullname() {
        return (this.fullname);
    }

    /**
     * <p>Getter for the field <code>user</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return (this.user);
    }

    /**
     * <p>Getter for the field <code>passwd</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPasswd() {
        return (this.passwd);
    }

    /**
     * <p>Getter for the field <code>confirmpasswd</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConfirmpasswd() {
        return (this.confirmpasswd);
    }

    /**
     * <p>Getter for the field <code>comments</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComments() {
        return (this.comments);
    }

    /**
     * <p>Getter for the field <code>email</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEmail() {
        return (this.email);
    }

    /**
     * <p>Getter for the field <code>numsvc</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumsvc() {
        return (this.numsvc);
    }

    /**
     * <p>Getter for the field <code>numpin</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumpin() {
        return (this.numpin);
    }

    /**
     * <p>Getter for the field <code>txtsvc</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTxtsvc() {
        return (this.txtsvc);
    }

    /**
     * <p>Getter for the field <code>txtpin</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTxtpin() {
        return (this.txtpin);
    }

    /**
     * <p>Getter for the field <code>dutysch</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDutysch() {
        return (this.dutysch);
    }

    /**
     * <p>Setter for the field <code>fullname</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setFullname(String name) {
        this.fullname = name;
    }

    /**
     * <p>Setter for the field <code>user</code>.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * <p>Setter for the field <code>passwd</code>.</p>
     *
     * @param passwd a {@link java.lang.String} object.
     */
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    /**
     * <p>Setter for the field <code>confirmpasswd</code>.</p>
     *
     * @param confirmPass a {@link java.lang.String} object.
     */
    public void setConfirmpasswd(String confirmPass) {
        this.confirmpasswd = confirmPass;
    }

    /**
     * <p>Setter for the field <code>comments</code>.</p>
     *
     * @param comments a {@link java.lang.String} object.
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * <p>Setter for the field <code>email</code>.</p>
     *
     * @param email a {@link java.lang.String} object.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * <p>Setter for the field <code>numsvc</code>.</p>
     *
     * @param num a {@link java.lang.String} object.
     */
    public void setNumsvc(String num) {
        this.numsvc = num;
    }

    /**
     * <p>Setter for the field <code>numpin</code>.</p>
     *
     * @param pin a {@link java.lang.String} object.
     */
    public void setNumpin(String pin) {
        this.numpin = pin;
    }

    /**
     * <p>Setter for the field <code>txtsvc</code>.</p>
     *
     * @param svc a {@link java.lang.String} object.
     */
    public void setTxtsvc(String svc) {
        this.txtsvc = svc;
    }

    /**
     * <p>Setter for the field <code>txtpin</code>.</p>
     *
     * @param pin a {@link java.lang.String} object.
     */
    public void setTxtpin(String pin) {
        this.txtpin = pin;
    }

    /**
     * <p>Setter for the field <code>dutysch</code>.</p>
     *
     * @param duty a {@link java.lang.String} object.
     */
    public void setDutysch(String duty) {
        this.dutysch = duty;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
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
