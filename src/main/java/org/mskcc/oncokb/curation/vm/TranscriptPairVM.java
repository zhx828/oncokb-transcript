package org.mskcc.oncokb.curation.vm;

import org.mskcc.oncokb.curation.domain.enumeration.EnsemblReferenceGenome;

/**
 * Created by Hongxin Zhang on 7/15/20.
 */
public class TranscriptPairVM {

    EnsemblReferenceGenome referenceGenome;
    String transcript;

    public EnsemblReferenceGenome getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(EnsemblReferenceGenome referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }
}
