package org.mskcc.oncokb.curation.importer.model;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.oncokb.curation.service.dto.TranscriptDTO;

public class TranscriptWithSeq {

    TranscriptDTO transcriptDTO;
    String seq = "";
    Integer seqLength = 0;

    public TranscriptDTO getTranscriptDTO() {
        return transcriptDTO;
    }

    public void setTranscriptDTO(TranscriptDTO transcriptDTO) {
        this.transcriptDTO = transcriptDTO;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public Integer getSeqLength() {
        return seqLength;
    }

    public void setSeqLength(Integer seqLength) {
        this.seqLength = seqLength;
    }

    @Override
    public String toString() {
        return transcriptDTO != null ? (transcriptDTO.getEnsemblTranscriptId() + " (" + seqLength + ')') : "";
    }
}
