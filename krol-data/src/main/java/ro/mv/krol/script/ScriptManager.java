package ro.mv.krol.script;

import ro.mv.krol.exception.ScriptCompileException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
public class ScriptManager {

    private final GroovyShell shell = new GroovyShell();

    public Script load(String urlString) throws ScriptCompileException {
        try {
            URL url = createScriptURLFrom(urlString);
            return shell.parse(url.toURI());
        } catch (CompilationFailedException | IOException | URISyntaxException e) {
            throw new ScriptCompileException(urlString, e);
        }
    }

    private URL createScriptURLFrom(String urlString) throws MalformedURLException {
        if (urlString.startsWith("classpath:")) {
            String classpathLocation = urlString.substring(10);
            return getClass().getResource(classpathLocation);
        }
        return new URL(urlString);
    }

    public Script load(URL url) throws ScriptCompileException {
        try {
            return shell.parse(url.toURI());
        } catch (CompilationFailedException | IOException | URISyntaxException e) {
            throw new ScriptCompileException(url, e);
        }
    }

    public Script parse(String text) throws ScriptCompileException {
        try {
            return shell.parse(text);
        } catch (CompilationFailedException e) {
            throw new ScriptCompileException(text, e);
        }
    }

}
