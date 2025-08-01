/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        final StringBuilder str = new StringBuilder("Full Name : " + fullname + " \n");
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
