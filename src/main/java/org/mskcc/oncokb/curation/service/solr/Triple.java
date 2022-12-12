package org.mskcc.oncokb.curation.service.solr;

import java.util.List;
import java.util.Map;

public class Triple {

    private Map<String, List<String>> articleMap;
    private Map<String, List<String>> extraMap;
    private String newId;

    public Map<String, List<String>> getArticleMap() {
        return articleMap;
    }

    public void setArticleMap(Map<String, List<String>> articleMap) {
        this.articleMap = articleMap;
    }

    public Map<String, List<String>> getExtraMap() {
        return extraMap;
    }

    public void setExtraMap(Map<String, List<String>> extraMap) {
        this.extraMap = extraMap;
    }

    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }
}
