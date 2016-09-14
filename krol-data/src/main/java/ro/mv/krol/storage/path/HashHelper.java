package ro.mv.krol.storage.path;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by mihai on 9/13/16.
 */
public class HashHelper {

    public String md5(String data) {
        return DigestUtils.md5Hex(data);
    }

    public String sha1(String data) {
        return DigestUtils.sha1Hex(data);
    }

}
