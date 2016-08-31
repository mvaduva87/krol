package ro.mv.krol.model;

import groovy.lang.Script;

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

    public List<CompiledSelector> getCompiledSelectorsFor(Selector.Target target) {
        return compiledSelectors != null ? compiledSelectors.get(target) : null;
    }
}
