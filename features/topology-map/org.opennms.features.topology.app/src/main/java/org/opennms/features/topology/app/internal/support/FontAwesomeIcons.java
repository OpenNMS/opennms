/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.support;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;

public abstract class FontAwesomeIcons {

    public static void load(Resource css){
        Page.getCurrent().getStyles().add(css);
    }

    public enum IconVariant {

        SIZE_LARGE("icon-large"),
        SIZE_2X("icon-2x"),
        SIZE_3X("icon-3x"),
        SIZE_4X("icon-4x"),
        SPIN("icon-spin"),
        BORDER("icon-border"),
        PULL_LEFT("pull-left"),
        PULL_RIGHT("pull-right");

        private final String stylename;

        IconVariant(String stylename) {
            this.stylename = stylename;
        }

        @Override
        public String toString() {
            return stylename;
        }
    }

    public enum Icon {
        glass("&#xf000;"),
        music("&#xf001;"),
        search("&#xf002;"),
        envelope("&#xf003;"),
        heart("&#xf004;"),
        star("&#xf005;"),
        star_empty("&#xf006;"),
        user("&#xf007;"), 
        film("&#xf008;"), 
        th_large("&#xf009;"), 
        th("&#xf00a;"), 
        th_list("&#xf00b;"), 
        ok("&#xf00c;"), 
        remove("&#xf00d;"), 
        zoom_in("&#xf00e;"), 

        zoom_out("&#xf010;"), 
        off("&#xf011;"), 
        signal("&#xf012;"), 
        cog("&#xf013;"), 
        trash("&#xf014;"), 
        home("&#xf015;"), 
        file("&#xf016;"), 
        time("&#xf017;"), 
        road("&#xf018;"), 
        download_alt("&#xf019;"), 
        download("&#xf01a;"), 
        upload("&#xf01b;"), 
        inbox("&#xf01c;"), 
        play_circle("&#xf01d;"), 
        repeat("&#xf01e;"), 

        /* &#xf020 doesn't work in Safari. all shifted one down */
        refresh("&#xf021;"), 
        list_alt("&#xf022;"), 
        lock("&#xf023;"), 
        flag("&#xf024;"), 
        headphones("&#xf025;"), 
        volume_off("&#xf026;"), 
        volume_down("&#xf027;"), 
        volume_up("&#xf028;"), 
        qrcode("&#xf029;"), 
        barcode("&#xf02a;"), 
        tag("&#xf02b;"), 
        tags("&#xf02c;"), 
        book("&#xf02d;"), 
        bookmark("&#xf02e;"), 
        print("&#xf02f;"), 

        camera("&#xf030;"), 
        font("&#xf031;"), 
        bold("&#xf032;"), 
        italic("&#xf033;"), 
        text_height("&#xf034;"), 
        text_width("&#xf035;"), 
        align_left("&#xf036;"), 
        align_center("&#xf037;"), 
        align_right("&#xf038;"), 
        align_justify("&#xf039;"), 
        list("&#xf03a;"), 
        indent_left("&#xf03b;"), 
        indent_right("&#xf03c;"), 
        facetime_video("&#xf03d;"), 
        picture("&#xf03e;"), 

        pencil("&#xf040;"), 
        map_marker("&#xf041;"), 
        adjust("&#xf042;"), 
        tint("&#xf043;"), 
        edit("&#xf044;"), 
        share("&#xf045;"), 
        check("&#xf046;"), 
        move("&#xf047;"), 
        step_backward("&#xf048;"), 
        fast_backward("&#xf049;"), 
        backward("&#xf04a;"), 
        play("&#xf04b;"), 
        pause("&#xf04c;"), 
        stop("&#xf04d;"), 
        forward("&#xf04e;"), 

        fast_forward("&#xf050;"), 
        step_forward("&#xf051;"), 
        eject("&#xf052;"), 
        chevron_left("&#xf053;"), 
        chevron_right("&#xf054;"), 
        plus_sign("&#xf055;"), 
        minus_sign("&#xf056;"), 
        remove_sign("&#xf057;"), 
        ok_sign("&#xf058;"), 
        question_sign("&#xf059;"), 
        info_sign("&#xf05a;"), 
        screenshot("&#xf05b;"), 
        remove_circle("&#xf05c;"), 
        ok_circle("&#xf05d;"), 
        ban_circle("&#xf05e;"), 

        arrow_left("&#xf060;"), 
        arrow_right("&#xf061;"), 
        arrow_up("&#xf062;"), 
        arrow_down("&#xf063;"), 
        share_alt("&#xf064;"), 
        resize_full("&#xf065;"), 
        resize_small("&#xf066;"), 
        plus("&#xf067;"), 
        minus("&#xf068;"), 
        asterisk("&#xf069;"), 
        exclamation_sign("&#xf06a;"), 
        gift("&#xf06b;"), 
        leaf("&#xf06c;"), 
        fire("&#xf06d;"), 
        eye_open("&#xf06e;"), 

        eye_close("&#xf070;"), 
        warning_sign("&#xf071;"), 
        plane("&#xf072;"), 
        calendar("&#xf073;"), 
        random("&#xf074;"), 
        comment("&#xf075;"), 
        magnet("&#xf076;"), 
        chevron_up("&#xf077;"), 
        chevron_down("&#xf078;"), 
        retweet("&#xf079;"), 
        shopping_cart("&#xf07a;"), 
        folder_close("&#xf07b;"), 
        folder_open("&#xf07c;"), 
        resize_vertical("&#xf07d;"), 
        resize_horizontal("&#xf07e;"), 

        bar_chart("&#xf080;"), 
        twitter_sign("&#xf081;"), 
        facebook_sign("&#xf082;"), 
        camera_retro("&#xf083;"), 
        key("&#xf084;"), 
        cogs("&#xf085;"), 
        comments("&#xf086;"), 
        thumbs_up("&#xf087;"), 
        thumbs_down("&#xf088;"), 
        star_half("&#xf089;"), 
        heart_empty("&#xf08a;"), 
        signout("&#xf08b;"), 
        linkedin_sign("&#xf08c;"), 
        pushpin("&#xf08d;"), 
        external_link("&#xf08e;"), 

        signin("&#xf090;"), 
        trophy("&#xf091;"), 
        github_sign("&#xf092;"), 
        upload_alt("&#xf093;"), 
        lemon("&#xf094;"), 
        phone("&#xf095;"), 
        check_empty("&#xf096;"), 
        bookmark_empty("&#xf097;"), 
        phone_sign("&#xf098;"), 
        twitter("&#xf099;"), 
        facebook("&#xf09a;"), 
        github("&#xf09b;"), 
        unlock("&#xf09c;"), 
        credit_card("&#xf09d;"), 
        rss("&#xf09e;"), 

        hdd("&#xf0a0;"), 
        bullhorn("&#xf0a1;"), 
        bell("&#xf0a2;"), 
        certificate("&#xf0a3;"), 
        hand_right("&#xf0a4;"), 
        hand_left("&#xf0a5;"), 
        hand_up("&#xf0a6;"), 
        hand_down("&#xf0a7;"), 
        circle_arrow_left("&#xf0a8;"), 
        circle_arrow_right("&#xf0a9;"), 
        circle_arrow_up("&#xf0aa;"), 
        circle_arrow_down("&#xf0ab;"), 
        globe("&#xf0ac;"), 
        wrench("&#xf0ad;"), 
        tasks("&#xf0ae;"), 

        filter("&#xf0b0;"), 
        briefcase("&#xf0b1;"), 
        fullscreen("&#xf0b2;"), 

        group("&#xf0c0;"), 
        link("&#xf0c1;"), 
        cloud("&#xf0c2;"), 
        beaker("&#xf0c3;"), 
        cut("&#xf0c4;"), 
        copy("&#xf0c5;"), 
        paper_clip("&#xf0c6;"), 
        save("&#xf0c7;"), 
        sign_blank("&#xf0c8;"), 
        reorder("&#xf0c9;"), 
        list_ul("&#xf0ca;"), 
        list_ol("&#xf0cb;"), 
        strikethrough("&#xf0cc;"), 
        underline("&#xf0cd;"), 
        table("&#xf0ce;"), 

        magic("&#xf0d0;"), 
        truck("&#xf0d1;"), 
        pinterest("&#xf0d2;"), 
        pinterest_sign("&#xf0d3;"), 
        google_plus_sign("&#xf0d4;"), 
        google_plus("&#xf0d5;"), 
        money("&#xf0d6;"), 
        caret_down("&#xf0d7;"), 
        caret_up("&#xf0d8;"), 
        caret_left("&#xf0d9;"), 
        caret_right("&#xf0da;"), 
        columns("&#xf0db;"), 
        sort("&#xf0dc;"), 
        sort_down("&#xf0dd;"), 
        sort_up("&#xf0de;"), 

        envelope_alt("&#xf0e0;"), 
        linkedin("&#xf0e1;"), 
        undo("&#xf0e2;"), 
        legal("&#xf0e3;"), 
        dashboard("&#xf0e4;"), 
        comment_alt("&#xf0e5;"), 
        comments_alt("&#xf0e6;"), 
        bolt("&#xf0e7;"), 
        sitemap("&#xf0e8;"), 
        umbrella("&#xf0e9;"), 
        paste("&#xf0ea;"), 
        lightbulb("&#xf0eb;"), 
        exchange("&#xf0ec;"), 
        cloud_download("&#xf0ed;"), 
        cloud_upload("&#xf0ee;"), 

        user_md("&#xf0f0;"), 
        stethoscope("&#xf0f1;"), 
        suitcase("&#xf0f2;"), 
        bell_alt("&#xf0f3;"), 
        coffee("&#xf0f4;"), 
        food("&#xf0f5;"), 
        file_alt("&#xf0f6;"), 
        building("&#xf0f7;"), 
        hospital("&#xf0f8;"), 
        ambulance("&#xf0f9;"), 
        medkit("&#xf0fa;"), 
        fighter_jet("&#xf0fb;"), 
        beer("&#xf0fc;"), 
        h_sign("&#xf0fd;"), 
        plus_sign_alt("&#xf0fe;"), 

        double_angle_left("&#xf100;"), 
        double_angle_right("&#xf101;"), 
        double_angle_up("&#xf102;"), 
        double_angle_down("&#xf103;"), 
        angle_left("&#xf104;"), 
        angle_right("&#xf105;"), 
        angle_up("&#xf106;"), 
        angle_down("&#xf107;"), 
        desktop("&#xf108;"), 
        laptop("&#xf109;"), 
        tablet("&#xf10a;"), 
        mobile_phone("&#xf10b;"), 
        circle_blank("&#xf10c;"), 
        quote_left("&#xf10d;"), 
        quote_right("&#xf10e;"), 

        spinner("&#xf110;"), 
        circle("&#xf111;"), 
        reply("&#xf112;"), 
        github_alt("&#xf113;"), 
        folder_close_alt("&#xf114;"), 
        folder_open_alt("&#xf115;"),
        location_arrow("&#xf124")
        ;

        private final String id;

        private Icon(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return variant();
        }

        public String id() {
            return id;
        }

        public String stylename() {
            return "icon-" + name().replaceAll("_", "-");
        }


        public String variant(IconVariant... variants) {
            String stylenames = stylename();
            for (IconVariant v : variants) {
                stylenames += " " + v;
            }
            return "<i class=\"" + stylenames + "\"></i>";
        }
    }
}
