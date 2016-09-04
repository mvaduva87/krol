package ro.mv.krol.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mv.krol.browser.HtmlPage;
import ro.mv.krol.model.*;
import ro.mv.krol.storage.PageStorage;
import ro.mv.krol.storage.ResourceStorage;
import ro.mv.krol.storage.StoredType;
import ro.mv.krol.util.Args;
import ro.mv.krol.util.Metadata;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by mihai.vaduva on 04/09/2016.
 */
@Singleton
public class PageProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Extractor extractor;
    private final PageStorage pageStorage;
    private final ResourceStorage resourceStorage;

    @Inject
    public PageProcessor(Extractor extractor, PageStorage pageStorage, ResourceStorage resourceStorage) {
        this.extractor = Args.notNull(extractor, "extractor");
        this.pageStorage = Args.notNull(pageStorage, "pageStorage");
        this.resourceStorage = Args.notNull(resourceStorage, "resourceStorage");
    }

    public Page process(HtmlPage htmlPage, CompiledSeed origin) throws IOException {
        Metadata.transfer(origin.getMetadata(), htmlPage.getMetadata(), false);
        Page.Builder builder = Page.builder();
        builder.withUrl(htmlPage.getUrl());
        builder.withTimestamp(htmlPage.getTimestamp());
        builder.withCharset(htmlPage.getCharset());
        builder.withMetadata(htmlPage.getMetadata());
        Map<StoredType, String> locatorMap = pageStorage.store(htmlPage);
        builder.withLocator(locatorMap);
        if (origin.canExtract()) {
            Map<Selector.Target, List<Link>> linkMap = null;
            try {
                linkMap = extractor.extract(htmlPage, origin);
            } catch (IOException | RuntimeException e) {
                logger.warn("failed to extract links from page document", e);
            }
            if (linkMap != null) {
                builder.withLinks(linkMap.get(Selector.Target.LINKS));
                List<Link> resourceLinks = linkMap.get(Selector.Target.RESOURCES);
                if (resourceLinks != null && !resourceLinks.isEmpty()) {
                    builder.withResources(storeResourcesFrom(resourceLinks));
                }
            }
        }
        return builder.build();
    }

    private List<Resource> storeResourcesFrom(List<Link> links) {
        return links.stream()
                .map(link -> {
                    try {
                        return resourceStorage.store(link);
                    } catch (IOException e) {
                        logger.warn("failed to store resource " + link.getUrl(), e);
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

}
