package ro.mv.krol.util;

import ro.mv.krol.storage.StoredType;
import org.apache.commons.jexl2.*;
import ro.mv.krol.storage.StorageKey;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mihai.vaduva on 13/08/2016.
 */
@Singleton
public class PathTemplate {

    public static final String CONST_TEMPLATE_MAP = "storage.template";
    private final Map<StoredType, String> templateMap;
    private final JexlEngine jexlEngine;
    private final Pattern pattern = Pattern.compile("\\{[^\\{\\}]+\\}");

    @Inject
    public PathTemplate(@Named(CONST_TEMPLATE_MAP) Map<StoredType, String> templateMap) {
        this.templateMap = Args.notEmpty(templateMap, CONST_TEMPLATE_MAP);
        jexlEngine = new JexlEngine();
        Map<String, Object> functions = new HashMap<>();
        functions.put("date", new DateHelper());
        jexlEngine.setFunctions(functions);
    }

    public String getPathFor(StorageKey key) {
        String template = templateMap.get(key.getType());
        if (template == null) {
            throw new NullPointerException("no template found for storage type " + key.getType());
        }
        JexlContext context = createEvaluationContextFor(key);
        Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            String group = matcher.group();
            String expr = group.substring(1, group.length() - 1);
            Expression expression = jexlEngine.createExpression(expr);
            String value = String.valueOf(expression.evaluate(context));
            template = template.replace(group, value);
        }
        return template;
    }

    private JexlContext createEvaluationContextFor(StorageKey key) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", key.getName());
        params.put("timestamp", key.getTimestamp());
        params.put("metadata", key.getMetadata());
        return new ReadonlyContext(new MapContext(params));
    }
}
