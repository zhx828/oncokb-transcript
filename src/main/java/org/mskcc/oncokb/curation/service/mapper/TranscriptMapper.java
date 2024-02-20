package org.mskcc.oncokb.curation.service.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.mapstruct.*;
import org.mskcc.oncokb.curation.domain.GenomeFragment;
import org.mskcc.oncokb.curation.domain.Sequence;
import org.mskcc.oncokb.curation.domain.Transcript;
import org.mskcc.oncokb.curation.domain.enumeration.GenomeFragmentType;
import org.mskcc.oncokb.curation.domain.enumeration.SequenceType;
import org.mskcc.oncokb.curation.repository.GenomeFragmentRepository;
import org.mskcc.oncokb.curation.repository.SequenceRepository;
import org.mskcc.oncokb.curation.service.dto.TranscriptDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper for the entity {@link Transcript} and its DTO {@link TranscriptDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public abstract class TranscriptMapper implements EntityMapper<TranscriptDTO, Transcript> {

    @Autowired
    GenomeFragmentRepository genomeFragmentRepository;

    @Autowired
    SequenceRepository sequenceRepository;

    /**
     * @param transcriptDTO
     */
    @AfterMapping
    protected void updateDTO(Transcript transcript, @MappingTarget TranscriptDTO transcriptDTO) {
        List<GenomeFragment> genomeFragmentList = genomeFragmentRepository.findAllByTranscriptId(transcriptDTO.getId());

        Optional<GenomeFragment> geneInfo = genomeFragmentList
            .stream()
            .filter(genomeFragment -> genomeFragment.getType().equals(GenomeFragmentType.GENE))
            .findFirst();
        if (geneInfo.isPresent()) {
            GenomeFragment gf = geneInfo.get();
            transcriptDTO.setChromosome(gf.getSeqRegion().getChromosome());
            transcriptDTO.setStart(gf.getStart());
            transcriptDTO.setEnd(gf.getEnd());
            transcriptDTO.setStrand(gf.getStrand());
        }

        List<GenomeFragment> exons = genomeFragmentList
            .stream()
            .filter(genomeFragment -> genomeFragment.getType().equals(GenomeFragmentType.EXON))
            .collect(Collectors.toList());
        transcriptDTO.setExons(exons);

        List<GenomeFragment> utrs = genomeFragmentList
            .stream()
            .filter(genomeFragment ->
                genomeFragment.getType().equals(GenomeFragmentType.FIVE_PRIME_UTR) ||
                genomeFragment.getType().equals(GenomeFragmentType.THREE_PRIME_UTR)
            )
            .collect(Collectors.toList());
        transcriptDTO.setUtrs(utrs);

        Optional<Sequence> sequenceOptional = sequenceRepository.findOneByTranscriptAndSequenceType(transcript, SequenceType.PROTEIN);
        if (sequenceOptional.isPresent()) {
            transcriptDTO.setProteinSequence(sequenceOptional.get());
        }
    }

    @AfterMapping
    protected void update(@MappingTarget Transcript transcript) {}
}
