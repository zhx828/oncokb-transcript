package org.mskcc.oncokb.curation.importer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mskcc.oncokb.curation.domain.Flag;
import org.mskcc.oncokb.curation.domain.Gene;
import org.mskcc.oncokb.curation.domain.enumeration.FlagType;
import org.mskcc.oncokb.curation.service.dto.TranscriptDTO;

public class TranscriptComparison {

    Gene gene;

    List<TranscriptDTO> grch37Transcripts = new ArrayList<>();
    List<TranscriptDTO> grch38Transcripts = new ArrayList<>();
    Integer numOfVariants = 0;

    TranscriptWithSeq grch37OncokbTranscript;
    TranscriptWithSeq mskTranscript;
    Boolean sameOncokbMSkId = false;
    Boolean sameOncokbMSkSeq = false;
    TranscriptWithSeq grch37EnsemblCanonical;
    TranscriptWithSeq grch37GnCanonical;
    Boolean grch37SameEnsemblGnId = null;
    Boolean grch37SameEnsemblGnSeq = null;
    TranscriptWithSeq grch38OncokbTranscript;
    TranscriptWithSeq grch38EnsemblCanonical;
    TranscriptWithSeq grch38GnCanonical;
    TranscriptWithSeq maneSelect;
    Boolean sameManeSelectGrch37EnsemblId = null;
    Boolean sameManeSelectGrch37EnsemblSeq = null;
    Boolean sameManeSelectGrch37GnId = null;
    Boolean sameManeSelectGrch37GnSeq = null;
    List<TranscriptWithSeq> manePlusClinical = new ArrayList<>();
    Boolean sameManeClinicalGrch37EnsemblId = null;
    Boolean sameManeClinicalGrch37EnsemblSeq = null;
    Boolean sameManeClinicalGrch37GnId = null;
    Boolean sameManeClinicalGrch37GnSeq = null;
    Boolean manePlusClinicalIsLonger = null;
    TranscriptWithSeq recommendedGrch37Canonical;

    Boolean grch37SameToRecommendation;
    String recommendedGrch37CanonicalNote = "";
    String recommendedGrch37CanonicalPanel = "";
    TranscriptWithSeq recommendedGrch38Canonical;

    Boolean grch38SameToRecommendation;
    String recommendedGrch38CanonicalNote = "";
    String recommendedGrch38CanonicalPanel = "";

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public List<TranscriptDTO> getGrch37Transcripts() {
        return grch37Transcripts;
    }

    public void setGrch37Transcripts(List<TranscriptDTO> grch37Transcripts) {
        this.grch37Transcripts = grch37Transcripts;
    }

    public List<TranscriptDTO> getGrch38Transcripts() {
        return grch38Transcripts;
    }

    public void setGrch38Transcripts(List<TranscriptDTO> grch38Transcripts) {
        this.grch38Transcripts = grch38Transcripts;
    }

    public Integer getNumOfVariants() {
        return numOfVariants;
    }

    public void setNumOfVariants(Integer numOfVariants) {
        this.numOfVariants = numOfVariants;
    }

    public TranscriptWithSeq getGrch37OncokbTranscript() {
        return grch37OncokbTranscript;
    }

    public void setGrch37OncokbTranscript(TranscriptWithSeq grch37OncokbTranscript) {
        this.grch37OncokbTranscript = grch37OncokbTranscript;
    }

    public TranscriptWithSeq getMskTranscript() {
        return mskTranscript;
    }

    public void setMskTranscript(TranscriptWithSeq mskTranscript) {
        this.mskTranscript = mskTranscript;
    }

    public Boolean getSameOncokbMSkId() {
        return sameOncokbMSkId;
    }

    public void setSameOncokbMSkId(Boolean sameOncokbMSkId) {
        this.sameOncokbMSkId = sameOncokbMSkId;
    }

    public Boolean getSameOncokbMSkSeq() {
        return sameOncokbMSkSeq;
    }

    public void setSameOncokbMSkSeq(Boolean sameOncokbMSkSeq) {
        this.sameOncokbMSkSeq = sameOncokbMSkSeq;
    }

    public TranscriptWithSeq getGrch37EnsemblCanonical() {
        return grch37EnsemblCanonical;
    }

    public void setGrch37EnsemblCanonical(TranscriptWithSeq grch37EnsemblCanonical) {
        this.grch37EnsemblCanonical = grch37EnsemblCanonical;
    }

    public TranscriptWithSeq getGrch37GnCanonical() {
        return grch37GnCanonical;
    }

    public void setGrch37GnCanonical(TranscriptWithSeq grch37GnCanonical) {
        this.grch37GnCanonical = grch37GnCanonical;
    }

    public TranscriptWithSeq getGrch38OncokbTranscript() {
        return grch38OncokbTranscript;
    }

    public void setGrch38OncokbTranscript(TranscriptWithSeq grch38OncokbTranscript) {
        this.grch38OncokbTranscript = grch38OncokbTranscript;
    }

    public TranscriptWithSeq getGrch38EnsemblCanonical() {
        return grch38EnsemblCanonical;
    }

    public void setGrch38EnsemblCanonical(TranscriptWithSeq grch38EnsemblCanonical) {
        this.grch38EnsemblCanonical = grch38EnsemblCanonical;
    }

    public TranscriptWithSeq getGrch38GnCanonical() {
        return grch38GnCanonical;
    }

    public void setGrch38GnCanonical(TranscriptWithSeq grch38GnCanonical) {
        this.grch38GnCanonical = grch38GnCanonical;
    }

    public TranscriptWithSeq getManeSelect() {
        return maneSelect;
    }

    public void setManeSelect(TranscriptWithSeq maneSelect) {
        this.maneSelect = maneSelect;
    }

    public List<TranscriptWithSeq> getManePlusClinical() {
        return manePlusClinical;
    }

    public void setManePlusClinical(List<TranscriptWithSeq> manePlusClinical) {
        this.manePlusClinical = manePlusClinical;
    }

    public Boolean getManePlusClinicalIsLonger() {
        return manePlusClinicalIsLonger;
    }

    public void setManePlusClinicalIsLonger(Boolean manePlusClinicalIsLonger) {
        this.manePlusClinicalIsLonger = manePlusClinicalIsLonger;
    }

    public Boolean getGrch37SameEnsemblGnId() {
        return grch37SameEnsemblGnId;
    }

    public void setGrch37SameEnsemblGnId(Boolean grch37SameEnsemblGnId) {
        this.grch37SameEnsemblGnId = grch37SameEnsemblGnId;
    }

    public Boolean getGrch37SameEnsemblGnSeq() {
        return grch37SameEnsemblGnSeq;
    }

    public void setGrch37SameEnsemblGnSeq(Boolean grch37SameEnsemblGnSeq) {
        this.grch37SameEnsemblGnSeq = grch37SameEnsemblGnSeq;
    }

    public Boolean getSameManeSelectGrch37EnsemblId() {
        return sameManeSelectGrch37EnsemblId;
    }

    public void setSameManeSelectGrch37EnsemblId(Boolean sameManeSelectGrch37EnsemblId) {
        this.sameManeSelectGrch37EnsemblId = sameManeSelectGrch37EnsemblId;
    }

    public Boolean getSameManeSelectGrch37EnsemblSeq() {
        return sameManeSelectGrch37EnsemblSeq;
    }

    public void setSameManeSelectGrch37EnsemblSeq(Boolean sameManeSelectGrch37EnsemblSeq) {
        this.sameManeSelectGrch37EnsemblSeq = sameManeSelectGrch37EnsemblSeq;
    }

    public Boolean getSameManeSelectGrch37GnId() {
        return sameManeSelectGrch37GnId;
    }

    public void setSameManeSelectGrch37GnId(Boolean sameManeSelectGrch37GnId) {
        this.sameManeSelectGrch37GnId = sameManeSelectGrch37GnId;
    }

    public Boolean getSameManeSelectGrch37GnSeq() {
        return sameManeSelectGrch37GnSeq;
    }

    public void setSameManeSelectGrch37GnSeq(Boolean sameManeSelectGrch37GnSeq) {
        this.sameManeSelectGrch37GnSeq = sameManeSelectGrch37GnSeq;
    }

    public Boolean getSameManeClinicalGrch37EnsemblId() {
        return sameManeClinicalGrch37EnsemblId;
    }

    public void setSameManeClinicalGrch37EnsemblId(Boolean sameManeClinicalGrch37EnsemblId) {
        this.sameManeClinicalGrch37EnsemblId = sameManeClinicalGrch37EnsemblId;
    }

    public Boolean getSameManeClinicalGrch37EnsemblSeq() {
        return sameManeClinicalGrch37EnsemblSeq;
    }

    public void setSameManeClinicalGrch37EnsemblSeq(Boolean sameManeClinicalGrch37EnsemblSeq) {
        this.sameManeClinicalGrch37EnsemblSeq = sameManeClinicalGrch37EnsemblSeq;
    }

    public Boolean getSameManeClinicalGrch37GnId() {
        return sameManeClinicalGrch37GnId;
    }

    public void setSameManeClinicalGrch37GnId(Boolean sameManeClinicalGrch37GnId) {
        this.sameManeClinicalGrch37GnId = sameManeClinicalGrch37GnId;
    }

    public Boolean getSameManeClinicalGrch37GnSeq() {
        return sameManeClinicalGrch37GnSeq;
    }

    public void setSameManeClinicalGrch37GnSeq(Boolean sameManeClinicalGrch37GnSeq) {
        this.sameManeClinicalGrch37GnSeq = sameManeClinicalGrch37GnSeq;
    }

    public TranscriptWithSeq getRecommendedGrch37Canonical() {
        return recommendedGrch37Canonical;
    }

    public void setRecommendedGrch37Canonical(TranscriptWithSeq recommendedGrch37Canonical) {
        this.recommendedGrch37Canonical = recommendedGrch37Canonical;
    }

    public String getRecommendedGrch37CanonicalNote() {
        return recommendedGrch37CanonicalNote;
    }

    public void setRecommendedGrch37CanonicalNote(String recommendedGrch37CanonicalNote) {
        this.recommendedGrch37CanonicalNote = recommendedGrch37CanonicalNote;
    }

    public TranscriptWithSeq getRecommendedGrch38Canonical() {
        return recommendedGrch38Canonical;
    }

    public void setRecommendedGrch38Canonical(TranscriptWithSeq recommendedGrch38Canonical) {
        this.recommendedGrch38Canonical = recommendedGrch38Canonical;
    }

    public String getRecommendedGrch38CanonicalNote() {
        return recommendedGrch38CanonicalNote;
    }

    public void setRecommendedGrch38CanonicalNote(String recommendedGrch38CanonicalNote) {
        this.recommendedGrch38CanonicalNote = recommendedGrch38CanonicalNote;
    }

    public String getRecommendedGrch37CanonicalPanel() {
        return recommendedGrch37CanonicalPanel;
    }

    public void setRecommendedGrch37CanonicalPanel(String recommendedGrch37CanonicalPanel) {
        this.recommendedGrch37CanonicalPanel = recommendedGrch37CanonicalPanel;
    }

    public String getRecommendedGrch38CanonicalPanel() {
        return recommendedGrch38CanonicalPanel;
    }

    public void setRecommendedGrch38CanonicalPanel(String recommendedGrch38CanonicalPanel) {
        this.recommendedGrch38CanonicalPanel = recommendedGrch38CanonicalPanel;
    }

    public Boolean getGrch37SameToRecommendation() {
        return grch37SameToRecommendation;
    }

    public void setGrch37SameToRecommendation(Boolean grch37SameToRecommendation) {
        this.grch37SameToRecommendation = grch37SameToRecommendation;
    }

    public Boolean getGrch38SameToRecommendation() {
        return grch38SameToRecommendation;
    }

    public void setGrch38SameToRecommendation(Boolean grch38SameToRecommendation) {
        this.grch38SameToRecommendation = grch38SameToRecommendation;
    }

    @Override
    public String toString() {
        return (
            gene.getId() +
            "\t" +
            gene.getEntrezGeneId() +
            "\t" +
            gene.getHgncId() +
            "\t" +
            gene.getHugoSymbol() +
            "\t" +
            gene
                .getFlags()
                .stream()
                .filter(flag -> FlagType.GENE_PANEL.name().equals(flag.getType()))
                .map(Flag::getFlag)
                .collect(Collectors.joining(", ")) +
            "\t" +
            numOfVariants +
            "\t" +
            mskTranscript +
            "\t" +
            sameOncokbMSkId +
            "\t" +
            sameOncokbMSkSeq +
            "\t" +
            grch37EnsemblCanonical +
            "\t" +
            grch37GnCanonical +
            "\t" +
            grch37SameEnsemblGnId +
            "\t" +
            grch37SameEnsemblGnSeq +
            "\t" +
            grch38EnsemblCanonical +
            "\t" +
            grch38GnCanonical +
            "\t" +
            maneSelect +
            "\t" +
            sameManeSelectGrch37EnsemblId +
            "\t" +
            sameManeSelectGrch37EnsemblSeq +
            "\t" +
            sameManeSelectGrch37GnId +
            "\t" +
            sameManeSelectGrch37GnSeq +
            "\t" +
            manePlusClinical.stream().map(TranscriptWithSeq::toString).collect(Collectors.joining(", ")) +
            "\t" +
            sameManeClinicalGrch37EnsemblId +
            "\t" +
            sameManeClinicalGrch37EnsemblSeq +
            "\t" +
            sameManeClinicalGrch37GnId +
            "\t" +
            sameManeClinicalGrch37GnSeq +
            "\t" +
            manePlusClinicalIsLonger +
            "\t" +
            recommendedGrch37Canonical +
            "\t" +
            grch37OncokbTranscript +
            "\t" +
            grch37SameToRecommendation +
            "\t" +
            recommendedGrch37CanonicalNote +
            "\t" +
            recommendedGrch37CanonicalPanel +
            "\t" +
            recommendedGrch38Canonical +
            "\t" +
            grch38OncokbTranscript +
            "\t" +
            grch38SameToRecommendation +
            "\t" +
            recommendedGrch38CanonicalNote +
            "\t" +
            recommendedGrch38CanonicalPanel
        );
    }
}
