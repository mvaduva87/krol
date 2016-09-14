package ro.mv.krol.storage.path;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mihai.vaduva on 13/08/2016.
 */
public class DateHelper {

    public String format(Date timestamp, String pattern) {
        return new SimpleDateFormat(pattern).format(timestamp);
    }

}
