package org.opennms.web.acegisecurity;

import org.acegisecurity.providers.encoding.Md5PasswordEncoder;

public class UpperCaseMd5PasswordEncoder extends Md5PasswordEncoder {
    public String encodePassword(String rawPass, Object salt) {
    	// This is almost too easy -- I'm not complaining!!
        return super.encodePassword(rawPass, salt).toUpperCase();
    }
}
