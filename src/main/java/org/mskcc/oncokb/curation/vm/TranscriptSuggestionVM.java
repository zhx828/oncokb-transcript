package org.mskcc.oncokb.curation.vm;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.oncokb.curation.domain.enumeration.EnsemblReferenceGenome;

/**
 * Created by Hongxin Zhang on 7/15/20.
 */
public class TranscriptSuggestionVM {

    EnsemblReferenceGenome referenceGenome;
    String note = "";
    List<String> suggestions = new ArrayList<>();

    public EnsemblReferenceGenome getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(EnsemblReferenceGenome referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
