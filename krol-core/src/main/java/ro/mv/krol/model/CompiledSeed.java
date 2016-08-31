package ro.mv.krol.model;

import groovy.lang.Script;
import ro.mv.krol.exception.ScriptCompileException;
import ro.mv.krol.script.ScriptManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mihai.vaduva on 31/08/2016.
 */
public class CompiledSeed extends Seed {

    private final Script crawlScript;
    private final Map<Selector.Target, List<CompiledSelector>> compiledSelectors;

    private CompiledSeed(Seed seed, Script crawlScript, Map<Selector.Target, List<CompiledSelector>> compiledSelectors) {
        super(seed.url, seed.timestamp, seed.selectors, seed.metadata);
        this.crawlScript = crawlScript;
        this.compiledSelectors = compiledSelectors;
    }

    public Script getCrawlScript() {
        return crawlScript;
    }

    public Map<Selector.Target, List<CompiledSelector>> getCompiledSelectors() {
        return compiledSelectors;
    }

    public boolean canExtract() {
        if (compiledSelectors == null || compiledSelectors.isEmpty()) {
            return false;
        }
        for (List<CompiledSelector> selectorList: compiledSelectors.values()) {
            if (selectorList != null && !selectorList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public List<CompiledSelector> getCompiledSelectorsFor(Selector.Target target) {
        return compiledSelectors != null ? compiledSelectors.get(target) : null;
    }

    static class Builder {

        private Seed seed;
        private Script crawlScript;
        private Map<Selector.Target, List<CompiledSelector>> compiledSelectors;

        Builder withSeed(Seed seed) {
            this.seed = seed;
            return this;
        }

        Builder withCrawlScript(Script crawlScript) {
            this.crawlScript = crawlScript;
            return this;
        }

        Builder withCompiledSelector(Selector.Target target, CompiledSelector selector) {
            if (compiledSelectors == null) {
                compiledSelectors = new HashMap<>();
            }
            List<CompiledSelector> selectorList = compiledSelectors.get(target);
            if (selectorList == null) {
                selectorList = new ArrayList<>();
                compiledSelectors.put(target, selectorList);
            }
            selectorList.add(selector);
            return this;
        }

        CompiledSeed build() {
            return new CompiledSeed(seed, crawlScript, compiledSelectors);
        }
    }

    public static CompiledSeed compile(Seed seed, ScriptManager scriptManager) throws ScriptCompileException {
        Builder builder = new Builder();
        builder.withSeed(seed);
        builder.withCrawlScript(compileScriptOf(seed, scriptManager));
        for (Selector.Target target : seed.getSelectors().keySet()) {
            List<Selector> selectors = seed.getSelectorsFor(target);
            if (selectors != null && !selectors.isEmpty()) {
                for (Selector selector : selectors) {
                    builder.withCompiledSelector(target, CompiledSelector.compile(selector, scriptManager));
                }
            }
        }
        return builder.build();
    }

    private static Script compileScriptOf(Seed seed, ScriptManager scriptManager) throws ScriptCompileException {
        if (seed.getMetadata() == null || seed.getMetadata().isEmpty()) {
            return null;
        }
        String scriptUrlString = seed.getMetadata().get("x-script");
        if (scriptUrlString == null || scriptUrlString.isEmpty()) {
            return null;
        }
        return scriptManager.load(scriptUrlString);
    }
}
