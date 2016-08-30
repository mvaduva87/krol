package ro.mv.krol.extract;

import java.util.List;

/**
 * Created by mihai on 8/19/16.
 */
public interface Document {

    List<String> select(String query, String attr);

}
