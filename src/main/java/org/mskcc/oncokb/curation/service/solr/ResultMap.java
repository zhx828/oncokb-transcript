package org.mskcc.oncokb.curation.service.solr;

import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrDocumentList;

public class ResultMap {

    private Map<String, Object> explainMap;
    private SolrDocumentList docs;
    private Map<String, Map<String, List<String>>> highlightingMap;

    public ResultMap(Map<String, Object> explainMap, SolrDocumentList docs, Map<String, Map<String, List<String>>> highlightingMap) {
        this.explainMap = explainMap;
        this.docs = docs;
        this.highlightingMap = highlightingMap;
    }

    public Map<String, Object> getExplainMap() {
        return explainMap;
    }

    public void setExplainMap(Map<String, Object> explainMap) {
        this.explainMap = explainMap;
    }

    public SolrDocumentList getDocs() {
        return docs;
    }

    public void setDocs(SolrDocumentList docs) {
        this.docs = docs;
    }

    public Map<String, Map<String, List<String>>> getHighlightingMap() {
        return highlightingMap;
    }

    public void setHighlightingMap(Map<String, Map<String, List<String>>> highlightingMap) {
        this.highlightingMap = highlightingMap;
    }
}
