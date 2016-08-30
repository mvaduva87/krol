package ro.mv.krol.util;

/**
 * Created by mihai.vaduva on 07/08/2016.
 */
public class Environment {

    public static int getCpuCount() {
        return Runtime.getRuntime().availableProcessors();
    }

}
