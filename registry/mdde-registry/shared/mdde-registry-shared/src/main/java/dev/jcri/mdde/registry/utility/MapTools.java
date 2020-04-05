package dev.jcri.mdde.registry.utility;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common Map helper functions
 */
public class MapTools {
    /**
     * Invert the map Key and Value. From <Key:Value> to <Value:Key>
     * @param map Map to invert
     * @param <TVal> Type of the original map value
     * @param <TKey> Type of the original map key
     * @return
     */
    public static <TVal, TKey> Map<TVal, TKey> invert(Map<TKey, TVal> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
}
