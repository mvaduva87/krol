package ro.mv.krol.model;

import groovy.lang.Script;
import ro.mv.krol.exception.ScriptCompileException;
import ro.mv.krol.script.ScriptManager;

import java.util.Map;

/**
 * Created by mihai.vaduva on 31/08/2016.
 */
public class CompiledSelector extends Selector {

    private final Script whenScript;

    private CompiledSelector(Selector selector, Script whenScript) {
        super(selector.name, selector.query, selector.attribute, selector.when, selector.metadata);
        this.whenScript = whenScript;
    }

    public boolean pass(Selector selector, Map<String, String> metadata) {
        if (whenScript == null) {
            return true;
        }
        try {
            if (metadata != null) {
                metadata.forEach(whenScript::setProperty);
            }
            return (boolean) whenScript.run();
        } catch (Exception ignored) {
            return false;
        }
    }

    public static CompiledSelector compile(Selector selector, ScriptManager scriptManager) throws ScriptCompileException {
        Script whenScript = null;
        if (selector.getWhen() != null && !selector.getWhen().isEmpty()) {
            whenScript = scriptManager.parse(selector.getWhen());
        }
        return new CompiledSelector(selector, whenScript);
    }
}
