/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author Donald Desloge
 *
 */

@Component
@Scope("prototype")
public class CitrixDetector extends AsyncLineOrientedDetector {
//MultilineOrientedDetector
    public CitrixDetector() {
        super(1494, 500, 2);
        setServiceName("CITRIX");
    }

    @Override
    protected void onInit() {
        expectBanner(startsWith("ICA"));       
    }

    /**
     * @param string
     * @return
     */
//    private ResponseValidator readStreamUntilContains(final String string) {
//        
//        return new ResponseValidator() {
//
//            public boolean validate(Object message) {
//                
//                return string.contains("ICA");
//            }
//            
//        };
//    }

    /**
     * @param string
     * @return
     */
//    private ResponseValidator<MultilineOrientedResponse> readStreamUntilContains(final String pattern) {
//
//        return new ResponseValidator<MultilineOrientedResponse>() {
//
//            public boolean validate(MultilineOrientedResponse response) throws IOException {
//                return response.readStreamUntilContains(pattern);
//            }
//            
//        };
//    }

}
