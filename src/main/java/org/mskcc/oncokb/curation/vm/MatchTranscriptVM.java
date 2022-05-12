package org.mskcc.oncokb.curation.vm;

import org.mskcc.oncokb.curation.domain.enumeration.EnsemblReferenceGenome;

/**
 * Created by Hongxin Zhang on 7/15/20.
 */
public class MatchTranscriptVM {

    TranscriptPairVM transcript;
    EnsemblReferenceGenome targetReferenceGenome;

    public TranscriptPairVM getTranscript() {
        return transcript;
    }

    public void setTranscript(TranscriptPairVM transcript) {
        this.transcript = transcript;
    }

    public EnsemblReferenceGenome getTargetReferenceGenome() {
        return targetReferenceGenome;
    }

    public void setTargetReferenceGenome(EnsemblReferenceGenome targetReferenceGenome) {
        this.targetReferenceGenome = targetReferenceGenome;
    }
}
