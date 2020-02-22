package dev.jcri.mdde.registry.shared.store.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;
import java.util.Map;

/**
 * A container class for fragment location and meta values.
 *
 * We map String Ids of the Nodes and Fragments to integers that are valid within the scope of this container and
 * must be unwrapped on the destination end in the desired format.
 */
public class FragmentCatalog {
    public static final String NODES_FILED = "nodes";
    public static final String FRAGMENTS_FIELD = "fragments";
    public static final String NODE_FRAGMENTS_FIELD = "nodefrags";
    public static final String NODE_LIST_GLOBAL_META_TAGS_FIELD = "mtaggl";
    public static final String NODE_LIST_EXEMPLAR_META_TAGS_FIELD = "mtagex";
    public static final String FRAGMENT_GLOBAL_META_VALUES_FIELD = "fmglval";
    public static final String FRAGMENT_EXEMPLAR_META_VALUES_FIELD = "fmexval";

    /**
     * <local id, node unique id>
     */
    private Map<String, Integer> _nodes;
    private Map<String, Integer> _fragments;
    private Map<Integer, List<Integer>> _nodeContent;
    /**
     * Ordered list of the exemplar meta values. Meta values in _metaValuesExemplar are ordered according to this List
     */
    private List<String> _metaTagsExemplar;
    /**
     * Ordered list of global meta values. Meta values in _metaValuesGlobal are ordered according to this List
     */
    private List<String> _metaTagsGlobal;
    /**
     * <Fragment local id, <Meta values>>
     */
    private Map<Integer, List<String>> _metaValuesGlobal;
    /**
     * <Node local Id, <Fragment local id, Meta values>>
     */
    private Map<Integer, Map<Integer, List<String>>> _metaValuesExemplar;

    /**
     * Default constructor
     */
    public FragmentCatalog(){}

    public FragmentCatalog(Map<String, Integer> nodes,
                           Map<String, Integer> fragments,
                           Map<Integer, List<Integer>> nodeContent,
                           List<String> metaTagsExemplar,
                           List<String> metaTagsGlobal,
                           Map<Integer, List<String>> metaValuesGlobal,
                           Map<Integer, Map<Integer, List<String>>> metaValuesExemplar){
        _nodes = nodes;
        _fragments = fragments;
        _nodeContent = nodeContent;
        _metaTagsExemplar = metaTagsExemplar;
        _metaTagsGlobal = metaTagsGlobal;
        _metaValuesGlobal = metaValuesGlobal;
        _metaValuesExemplar = metaValuesExemplar;
    }

    @JsonGetter(NODES_FILED)
    public Map<String, Integer> getNodes() {
        return _nodes;
    }
    @JsonSetter(NODES_FILED)
    public void setNodes(Map<String, Integer> nodes) {
        this._nodes = nodes;
    }

    @JsonGetter(FRAGMENTS_FIELD)
    public Map<String, Integer> getFragments() {
        return _fragments;
    }
    @JsonSetter(FRAGMENTS_FIELD)
    public void setFragments(Map<String, Integer> fragments) {
        this._fragments = fragments;
    }

    @JsonGetter(NODE_FRAGMENTS_FIELD)
    public Map<Integer, List<Integer>> getNodeContent() {
        return _nodeContent;
    }
    @JsonSetter(NODE_FRAGMENTS_FIELD)
    public void setNodeContent(Map<Integer, List<Integer>> nodeContent) {
        this._nodeContent = nodeContent;
    }

    @JsonGetter(NODE_LIST_EXEMPLAR_META_TAGS_FIELD)
    public List<String> getMetaTagsExemplar() {
        return _metaTagsExemplar;
    }
    @JsonSetter(NODE_LIST_EXEMPLAR_META_TAGS_FIELD)
    public void setMetaTagsExemplar(List<String> metaTagsExemplar) {
        this._metaTagsExemplar = metaTagsExemplar;
    }

    @JsonGetter(NODE_LIST_GLOBAL_META_TAGS_FIELD)
    public List<String> getMetaTagsGlobal() {
        return _metaTagsGlobal;
    }
    @JsonSetter(NODE_LIST_GLOBAL_META_TAGS_FIELD)
    public void setMetaTagsGlobal(List<String> metaTagsGlobal) {
        this._metaTagsGlobal = metaTagsGlobal;
    }

    @JsonGetter(FRAGMENT_EXEMPLAR_META_VALUES_FIELD)
    public Map<Integer, List<String>> getMetaValuesGlobal() {
        return _metaValuesGlobal;
    }
    @JsonSetter(FRAGMENT_EXEMPLAR_META_VALUES_FIELD)
    public void setMetaValuesGlobal(Map<Integer, List<String>> metaValuesGlobal) {
        this._metaValuesGlobal = metaValuesGlobal;
    }

    @JsonGetter(FRAGMENT_GLOBAL_META_VALUES_FIELD)
    public Map<Integer, Map<Integer, List<String>>> getMetaValuesExemplar() {
        return _metaValuesExemplar;
    }
    @JsonSetter(FRAGMENT_GLOBAL_META_VALUES_FIELD)
    public void setMetaValuesExemplar(Map<Integer, Map<Integer, List<String>>> metaValuesExemplar) {
        this._metaValuesExemplar = metaValuesExemplar;
    }
}
