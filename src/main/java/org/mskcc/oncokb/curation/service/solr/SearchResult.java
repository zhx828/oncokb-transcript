package org.mskcc.oncokb.curation.service.solr;

import java.util.List;
import org.apache.solr.common.SolrDocumentList;

public class SearchResult {

    private SolrDocumentList docs;
    private List<String> pmIds;
    private List<Float> scores;

    public SolrDocumentList getDocs() {
        return docs;
    }

    public void setDocs(SolrDocumentList docs) {
        this.docs = docs;
    }

    public List<String> getPmIds() {
        return pmIds;
    }

    public void setPmIds(List<String> pmIds) {
        this.pmIds = pmIds;
    }

    public List<Float> getScores() {
        return scores;
    }

    public void setScores(List<Float> scores) {
        this.scores = scores;
    }
}
