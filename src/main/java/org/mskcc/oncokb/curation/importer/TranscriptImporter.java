package org.mskcc.oncokb.curation.importer;

import static org.mskcc.oncokb.curation.domain.enumeration.TranscriptFlagEnum.MSK_IMPACT_CANONICAL;
import static org.mskcc.oncokb.curation.util.FileUtils.parseDelimitedFile;
import static org.mskcc.oncokb.curation.util.FileUtils.readDelimitedLinesStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.genome_nexus.ApiException;
import org.genome_nexus.client.EnsemblTranscript;
import org.mskcc.oncokb.curation.config.application.ApplicationProperties;
import org.mskcc.oncokb.curation.domain.EnsemblGene;
import org.mskcc.oncokb.curation.domain.Flag;
import org.mskcc.oncokb.curation.domain.Gene;
import org.mskcc.oncokb.curation.domain.Sequence;
import org.mskcc.oncokb.curation.domain.enumeration.*;
import org.mskcc.oncokb.curation.importer.model.ManeTranscript;
import org.mskcc.oncokb.curation.importer.model.OncokbTranscript;
import org.mskcc.oncokb.curation.importer.model.TranscriptComparison;
import org.mskcc.oncokb.curation.importer.model.TranscriptWithSeq;
import org.mskcc.oncokb.curation.service.*;
import org.mskcc.oncokb.curation.service.dto.TranscriptDTO;
import org.mskcc.oncokb.curation.service.mapper.TranscriptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

// To import transcript info and sequence form Ensembl for both GRCh37 and 38
@Component
public class TranscriptImporter {

    private final Logger log = LoggerFactory.getLogger(TranscriptImporter.class);
    final String MANE_FILE_PATH = "data/transcript/mane/MANE.GRCh38.v1.2.summary.txt";
    final String MANE_VERSION = "v1.2";
    final String MANE_VERSION_DATE = "2023-01-01";

    final String ONCOKB_FILE_PATH = "data/transcript/oncokb_transcript_v4_8.tsv";
    final String ONCOKB_VERSION = "v4.8";
    final String ONCOKB_VERSION_DATE = "2023-01-01";
    final GeneService geneService;
    final AlterationService alterationService;

    final GenomeNexusService genomeNexusService;
    final EnsemblGeneService ensemblGeneService;
    final TranscriptService transcriptService;
    final TranscriptMapper transcriptMapper;
    final SequenceService sequenceService;
    final FlagService flagService;
    final InfoService infoService;
    final MainService mainService;

    final ApplicationProperties applicationProperties;

    final String DATA_DIRECTORY;
    final String TRANSCRIPT_DATA_FOLDER_PATH;

    public TranscriptImporter(
        GeneService geneService,
        AlterationService alterationService,
        GenomeNexusService genomeNexusService,
        EnsemblGeneService ensemblGeneService,
        TranscriptService transcriptService,
        TranscriptMapper transcriptMapper,
        SequenceService sequenceService,
        FlagService flagService,
        InfoService infoService,
        MainService mainService,
        ApplicationProperties applicationProperties
    ) {
        this.geneService = geneService;
        this.alterationService = alterationService;
        this.genomeNexusService = genomeNexusService;
        this.ensemblGeneService = ensemblGeneService;
        this.transcriptService = transcriptService;
        this.transcriptMapper = transcriptMapper;
        this.sequenceService = sequenceService;
        this.flagService = flagService;
        this.infoService = infoService;
        this.mainService = mainService;

        this.applicationProperties = applicationProperties;

        DATA_DIRECTORY = applicationProperties.getOncokbDataRepoDir() + "/curation";
        TRANSCRIPT_DATA_FOLDER_PATH = DATA_DIRECTORY + "/transcript";
    }

    private List<ManeTranscript> getManeTranscript() {
        URL geneFileUrl = getClass().getClassLoader().getResource(MANE_FILE_PATH);
        try {
            InputStream is = geneFileUrl.openStream();
            return readDelimitedLinesStream(is, "\t", true)
                .stream()
                .filter(line -> line.size() > 0 && !line.get(0).startsWith("#"))
                .map(line -> {
                    ManeTranscript maneTranscript = new ManeTranscript();
                    if (line.size() < 10) {
                        log.error("Line does not have all required columns {}", line);
                    } else {
                        maneTranscript.setEntrezGeneId(Integer.parseInt(line.get(0).replace("GeneID:", "")));
                        maneTranscript.setEnsemblGeneId(line.get(1));
                        maneTranscript.setHugoSymbol(line.get(3));
                        maneTranscript.setRefSeqTranscriptId(line.get(5));
                        maneTranscript.setRefSeqProteinId(line.get(6));
                        maneTranscript.setEnsemblTranscriptId(line.get(7));
                        maneTranscript.setEnsemblProteinId(line.get(8));
                        maneTranscript.setManeStatus(line.get(9));
                    }
                    return maneTranscript;
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<OncokbTranscript> getOncokbTranscript() {
        URL geneFileUrl = getClass().getClassLoader().getResource(ONCOKB_FILE_PATH);
        try {
            InputStream is = geneFileUrl.openStream();
            return readDelimitedLinesStream(is, "\t", true)
                .stream()
                .filter(line -> line.size() > 0 && StringUtils.isNumeric(line.get(0)) && Integer.parseInt(line.get(0)) > 0)
                .map(line -> {
                    OncokbTranscript oncokbTranscript = new OncokbTranscript();
                    oncokbTranscript.setEntrezGeneId(Integer.parseInt(line.get(0)));
                    if (line.size() >= 3) {
                        oncokbTranscript.setGrch37EnsemblTranscript(line.get(2));
                        if (line.size() >= 4) {
                            oncokbTranscript.setGrch37refSeq(line.get(3));
                            if (line.size() >= 5) {
                                oncokbTranscript.setGrch38EnsemblTranscript(line.get(4));
                                if (line.size() >= 6) {
                                    oncokbTranscript.setGrch38refSeq(line.get(5));
                                }
                            }
                        }
                    }
                    return oncokbTranscript;
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<List<String>> parseTsvTranscriptFile(String fileName) throws IOException {
        return parseDelimitedFile(TRANSCRIPT_DATA_FOLDER_PATH + "/" + fileName, "\t", true);
    }

    private Optional<TranscriptDTO> createTranscript(
        ReferenceGenome referenceGenome,
        String transcriptId,
        Integer entrezGeneId,
        String hugoSymbol,
        TranscriptFlagEnum transcriptFlag
    ) {
        Optional<TranscriptDTO> transcriptDTOOptional = mainService.createTranscript(
            referenceGenome,
            transcriptId,
            entrezGeneId,
            hugoSymbol,
            null,
            null,
            null,
            Collections.singletonList(transcriptFlag)
        );
        if (transcriptDTOOptional.isEmpty()) {
            log.error("Failed to create transcript {} {} {}", referenceGenome, entrezGeneId, transcriptId);
        } else {
            log.info("Saved/updated for {}.", referenceGenome);
        }
        return transcriptDTOOptional;
    }

    public void importCanonicalEnsemblGenes() {
        int pageSize = 10;
        final PageRequest pageable = PageRequest.of(0, pageSize);
        Page<Long> firstPageGeneIds = geneService.findAllGeneIds(pageable);
        List<org.mskcc.oncokb.curation.domain.Gene> allGenes = new ArrayList<>();
        for (int i = 0; i < firstPageGeneIds.getTotalPages(); i++) {
            PageRequest genePage = PageRequest.of(i, pageSize);
            Page<Long> pageGeneIds = geneService.findAllGeneIds(genePage);
            allGenes.addAll(geneService.findAllByIdInWithGeneAliasAndEnsemblGenes(pageGeneIds.getContent()));
        }
        for (ReferenceGenome rg : ReferenceGenome.values()) {
            for (org.mskcc.oncokb.curation.domain.Gene gene : allGenes) {
                mainService.createCanonicalEnsemblGene(rg, gene.getEntrezGeneId());
            }
        }
    }

    public void importTranscripts() {
        // import MANE 38
        for (ManeTranscript maneTranscript : getManeTranscript()) {
            //            log.info("Saving MANE transcript {} ", maneTranscript);
            ReferenceGenome referenceGenome = ReferenceGenome.GRCh38;
            Optional<Gene> geneOptional = geneService.findGeneByEntrezGeneId(maneTranscript.getEntrezGeneId());
            if (geneOptional.isEmpty()) {
                log.warn("Gene {} in MANE list cannot be found in DB", maneTranscript.getEntrezGeneId());
                continue;
            }
            String maneStatus = maneTranscript.getManeStatus();
            Optional<TranscriptDTO> transcriptDTOOptional = transcriptService.findByReferenceGenomeAndEnsemblTranscriptId(
                referenceGenome,
                maneTranscript.getEnsemblTranscriptId()
            );
            if (transcriptDTOOptional.isEmpty()) {
                log.info("No transcript found for {}, creating new one", maneTranscript.getEnsemblTranscriptId());
                if ("MANE Select".equals(maneStatus)) {
                    transcriptDTOOptional =
                        mainService.createTranscript(
                            referenceGenome,
                            maneTranscript.getEnsemblTranscriptId(),
                            maneTranscript.getEntrezGeneId(),
                            maneTranscript.getHugoSymbol(),
                            maneTranscript.getEnsemblProteinId(),
                            maneTranscript.getRefSeqTranscriptId(),
                            null,
                            Collections.singletonList(TranscriptFlagEnum.MANE_SELECT)
                        );
                } else if ("MANE Plus Clinical".equals(maneStatus)) {
                    transcriptDTOOptional =
                        mainService.createTranscript(
                            referenceGenome,
                            maneTranscript.getEnsemblTranscriptId(),
                            maneTranscript.getEntrezGeneId(),
                            maneTranscript.getHugoSymbol(),
                            maneTranscript.getEnsemblProteinId(),
                            maneTranscript.getRefSeqTranscriptId(),
                            null,
                            Collections.singletonList(TranscriptFlagEnum.MANE_PLUS_CLINICAL)
                        );
                }
                if (transcriptDTOOptional.isEmpty()) {
                    log.error("Failed to create MANE transcript {}", maneTranscript);
                } else {
                    log.info("Saved/updated.");
                }
            } else {
                List<Flag> flags = new ArrayList<>();
                for (Flag flag : transcriptDTOOptional.get().getFlags()) {
                    if (
                        !TranscriptFlagEnum.MANE_SELECT.name().equals(flag.getFlag()) &&
                        !TranscriptFlagEnum.MANE_PLUS_CLINICAL.name().equals(flag.getFlag())
                    ) {
                        flags.add(flag);
                    }
                }
                if ("MANE Select".equals(maneStatus)) {
                    flags.add(flagService.findByTypeAndFlag(FlagType.TRANSCRIPT, TranscriptFlagEnum.MANE_SELECT.name()).get());
                }
                if ("MANE Plus Clinical".equals(maneStatus)) {
                    flags.add(flagService.findByTypeAndFlag(FlagType.TRANSCRIPT, TranscriptFlagEnum.MANE_PLUS_CLINICAL.name()).get());
                }
                transcriptDTOOptional.get().setFlags(flags);
                transcriptService.partialUpdate(transcriptDTOOptional.get());
            }
        }
    }

    public void importMskGrch37Transcripts() throws IOException {
        List<List<String>> transcriptLines = parseTsvTranscriptFile("mskcc_grch37_2020.txt");
        Optional<Flag> flagOptional = flagService.findByTypeAndFlag(FlagType.TRANSCRIPT, MSK_IMPACT_CANONICAL.name());
        if (flagOptional.isEmpty()) {
            Flag flag = new Flag();
            flag.setType(FlagType.TRANSCRIPT.name());
            flag.setFlag(MSK_IMPACT_CANONICAL.name());
            flag.setName("MSK-IMPACT Canonical");
            flag.setDescription("A single transcript chosen by MSK-IMPACT bioinformatics team.");
            flagOptional = Optional.of(flagService.save(flag));
        }

        Optional<Flag> finalFlagOptional = flagOptional;
        transcriptLines.forEach(line -> {
            String hugoSymbol = line.get(1);
            Optional<Gene> geneOptional = geneService.findGeneByHugoSymbol(hugoSymbol);
            if (geneOptional.isEmpty()) {
                log.error("Cannot find hugo symbol {}", hugoSymbol);
                return;
            }
            String transcriptId = line.get(0);
            Optional<TranscriptDTO> transcriptOptional = transcriptService.findByReferenceGenomeAndEnsemblTranscriptIdWithoutSubversion(
                ReferenceGenome.GRCh37,
                transcriptId
            );
            if (transcriptOptional.isEmpty()) {
                log.error("Cannot find transcript {}, creating...", transcriptId);
                transcriptOptional =
                    createTranscript(
                        ReferenceGenome.GRCh37,
                        transcriptId,
                        geneOptional.get().getEntrezGeneId(),
                        geneOptional.get().getHugoSymbol(),
                        MSK_IMPACT_CANONICAL
                    );
                if (transcriptOptional.isEmpty()) {
                    log.error("Cannot create transcript {}", transcriptId);
                    return;
                }
            }
            transcriptOptional.get().getFlags().add(finalFlagOptional.get());
            transcriptService.partialUpdate(transcriptOptional.get());
        });
    }

    private String getTranscriptMainId(String transcriptId) {
        if (StringUtils.isEmpty(transcriptId)) {
            transcriptId = "";
        }
        if (transcriptId.contains(".")) {
            int dotIndex = transcriptId.indexOf(".");
            return transcriptId.substring(0, dotIndex);
        }
        return transcriptId;
    }

    private boolean compareTranscriptId(TranscriptWithSeq transcriptA, TranscriptWithSeq transcriptB) {
        if (transcriptA == null) {
            return false;
        }
        if (transcriptB == null) {
            return false;
        }
        if (transcriptA.getTranscriptDTO() == null) {
            return false;
        }
        if (transcriptB.getTranscriptDTO() == null) {
            return false;
        }
        String mainA = getTranscriptMainId(transcriptA.getTranscriptDTO().getEnsemblTranscriptId());
        String mainB = getTranscriptMainId(transcriptB.getTranscriptDTO().getEnsemblTranscriptId());
        return mainA.equals(mainB);
    }

    private boolean compareTranscriptSeq(TranscriptWithSeq transcriptA, TranscriptWithSeq transcriptB) {
        String transcriptSeqA = "";
        String transcriptSeqB = "";
        if (transcriptA != null) {
            transcriptSeqA = transcriptA.getSeq();
        }
        if (transcriptB != null) {
            transcriptSeqB = transcriptB.getSeq();
        }
        return transcriptSeqA.equals(transcriptSeqB);
    }

    private TranscriptWithSeq getTranscriptWithSeq(TranscriptDTO transcript) {
        TranscriptWithSeq transcriptWithSeq = new TranscriptWithSeq();
        transcriptWithSeq.setTranscriptDTO(transcript);
        if (transcript.getProteinSequence() != null) {
            transcriptWithSeq.setSeq(transcript.getProteinSequence().getSequence());
            transcriptWithSeq.setSeqLength(transcript.getProteinSequence().getSequence().length());
        }
        return transcriptWithSeq;
    }

    private void getTranscriptRecommendation(TranscriptComparison transcriptComparison) {
        if (transcriptComparison.getRecommendedGrch37Canonical() == null && transcriptComparison.getRecommendedGrch38Canonical() == null) {
            // We do not curate transcript if the gene is not included in MANE
            if (transcriptComparison.getManeSelect() != null) {
                if (transcriptComparison.getManePlusClinical().size() > 0) {
                    transcriptComparison
                        .getManePlusClinical()
                        .stream()
                        .sorted(Comparator.comparing(TranscriptWithSeq::getSeqLength).reversed());
                    TranscriptWithSeq clinical = transcriptComparison.getManePlusClinical().get(0);
                    if (clinical.getSeqLength() > transcriptComparison.getManeSelect().getSeqLength()) {
                        transcriptComparison.setRecommendedGrch38Canonical(clinical);
                        transcriptComparison.setRecommendedGrch38CanonicalNote("Longest MANE Plus Clinical");
                        transcriptComparison.setRecommendedGrch38CanonicalPanel(
                            transcriptComparison
                                .getRecommendedGrch38Canonical()
                                .getTranscriptDTO()
                                .getFlags()
                                .stream()
                                .map(Flag::getFlag)
                                .collect(Collectors.joining(", "))
                        );
                    }
                }
                if (transcriptComparison.getRecommendedGrch38Canonical() == null) {
                    transcriptComparison.setRecommendedGrch38Canonical(transcriptComparison.getManeSelect());
                    transcriptComparison.setRecommendedGrch38CanonicalNote("MANE Select");
                    transcriptComparison.setRecommendedGrch38CanonicalPanel(
                        transcriptComparison
                            .getRecommendedGrch38Canonical()
                            .getTranscriptDTO()
                            .getFlags()
                            .stream()
                            .map(Flag::getFlag)
                            .collect(Collectors.joining(", "))
                    );
                }
            }

            if (
                transcriptComparison.getRecommendedGrch38Canonical() == null &&
                transcriptComparison.getGrch38EnsemblCanonical() != null &&
                transcriptComparison.getGrch38EnsemblCanonical().getSeqLength() > 0
            ) {
                transcriptComparison.setRecommendedGrch38Canonical(transcriptComparison.getGrch38EnsemblCanonical());
                transcriptComparison.setRecommendedGrch38CanonicalNote("Ensembl Canonical");
                transcriptComparison.setRecommendedGrch38CanonicalPanel(
                    transcriptComparison
                        .getRecommendedGrch38Canonical()
                        .getTranscriptDTO()
                        .getFlags()
                        .stream()
                        .map(Flag::getFlag)
                        .collect(Collectors.joining(", "))
                );
            }

            if (transcriptComparison.getRecommendedGrch38Canonical() != null) {
                // Find corresponding transcript in GRCh37
                TranscriptWithSeq recommendedGrch38Transcript = transcriptComparison.getRecommendedGrch38Canonical();
                String recommendedGrch38TranscriptMainId = getTranscriptMainId(
                    recommendedGrch38Transcript.getTranscriptDTO().getEnsemblTranscriptId()
                );
                List<TranscriptDTO> sameSeq = transcriptComparison
                    .getGrch37Transcripts()
                    .stream()
                    .filter(transcriptDTO ->
                        transcriptDTO.getProteinSequence() != null &&
                        recommendedGrch38Transcript.getSeq().equals(transcriptDTO.getProteinSequence().getSequence())
                    )
                    .sorted((o1, o2) -> {
                        String o1MainId = getTranscriptMainId(o1.getEnsemblTranscriptId());
                        String o2MainId = getTranscriptMainId(o2.getEnsemblTranscriptId());
                        if (o1MainId.equals(recommendedGrch38TranscriptMainId)) {
                            return -1;
                        }
                        if (o2MainId.equals(recommendedGrch38TranscriptMainId)) {
                            return 1;
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());

                if (sameSeq.size() > 0) {
                    transcriptComparison.setRecommendedGrch37Canonical(getTranscriptWithSeq(sameSeq.get(0)));
                    transcriptComparison.setRecommendedGrch37CanonicalNote("Matched from GRCh38 recommendation");
                    transcriptComparison.setRecommendedGrch37CanonicalPanel(
                        transcriptComparison
                            .getRecommendedGrch37Canonical()
                            .getTranscriptDTO()
                            .getFlags()
                            .stream()
                            .map(Flag::getFlag)
                            .collect(Collectors.joining(", "))
                    );
                } else {
                    // if no same seq found, try to find the same transcript with the same main id
                    List<TranscriptDTO> sameMainId = transcriptComparison
                        .getGrch37Transcripts()
                        .stream()
                        .filter(transcriptDTO ->
                            transcriptDTO.getProteinSequence() != null && transcriptDTO.getProteinSequence().getSequence().length() > 0
                        )
                        .sorted((o1, o2) -> {
                            String o1MainId = getTranscriptMainId(o1.getEnsemblTranscriptId());
                            String o2MainId = getTranscriptMainId(o2.getEnsemblTranscriptId());
                            if (o1MainId.equals(recommendedGrch38TranscriptMainId)) {
                                return -1;
                            }
                            if (o2MainId.equals(recommendedGrch38TranscriptMainId)) {
                                return 1;
                            }
                            return 0;
                        })
                        .collect(Collectors.toList());
                    if (sameMainId.size() > 0) {
                        transcriptComparison.setRecommendedGrch37Canonical(getTranscriptWithSeq(sameMainId.get(0)));
                        transcriptComparison.setRecommendedGrch37CanonicalNote("Same transcript main id from GRCh38 recommendation");
                        if (
                            transcriptComparison
                                .getRecommendedGrch38Canonical()
                                .getSeq()
                                .contains(transcriptComparison.getRecommendedGrch37Canonical().getSeq())
                        ) {
                            transcriptComparison.setRecommendedGrch37CanonicalNote(
                                transcriptComparison.getRecommendedGrch37CanonicalNote() + ", GRCh38 contains GRCh37"
                            );
                        } else if (
                            Math.abs(
                                transcriptComparison.getRecommendedGrch38Canonical().getSeqLength() -
                                transcriptComparison.getRecommendedGrch37Canonical().getSeqLength()
                            ) <
                            10
                        ) {
                            transcriptComparison.setRecommendedGrch37CanonicalNote(
                                transcriptComparison.getRecommendedGrch37CanonicalNote() + ", length diff less than 10"
                            );
                        } else {
                            transcriptComparison.setRecommendedGrch37CanonicalNote(
                                transcriptComparison.getRecommendedGrch37CanonicalNote() + ", length equal or more than 10"
                            );
                        }
                        transcriptComparison.setRecommendedGrch37CanonicalPanel(
                            transcriptComparison
                                .getRecommendedGrch37Canonical()
                                .getTranscriptDTO()
                                .getFlags()
                                .stream()
                                .map(Flag::getFlag)
                                .collect(Collectors.joining(", "))
                        );
                    }
                    if (transcriptComparison.getRecommendedGrch37Canonical() == null) {
                        List<TranscriptDTO> sortedLengthDesc = transcriptComparison
                            .getGrch37Transcripts()
                            .stream()
                            .filter(transcriptDTO ->
                                transcriptDTO.getProteinSequence() != null && transcriptDTO.getProteinSequence().getSequence().length() > 0
                            )
                            .sorted((o1, o2) ->
                                o2.getProteinSequence().getSequence().length() - o1.getProteinSequence().getSequence().length()
                            )
                            .collect(Collectors.toList());
                        if (sortedLengthDesc.size() > 0) {
                            transcriptComparison.setRecommendedGrch37Canonical(getTranscriptWithSeq(sortedLengthDesc.get(0)));
                            transcriptComparison.setRecommendedGrch37CanonicalNote("Longest GRCh37 transcript");
                            transcriptComparison.setRecommendedGrch37CanonicalPanel(
                                transcriptComparison
                                    .getRecommendedGrch37Canonical()
                                    .getTranscriptDTO()
                                    .getFlags()
                                    .stream()
                                    .map(Flag::getFlag)
                                    .collect(Collectors.joining(", "))
                            );
                        }
                    }
                }
            }
        }

        // use predefined GRCh37 if exists
        if (transcriptComparison.getGrch37OncokbTranscript() != null) {
            transcriptComparison.setGrch37SameToRecommendation(
                compareTranscriptId(transcriptComparison.getGrch37OncokbTranscript(), transcriptComparison.getRecommendedGrch37Canonical())
            );
        }
        // use predefined GRCh38 if exists
        if (transcriptComparison.getGrch38OncokbTranscript() != null) {
            transcriptComparison.setGrch38SameToRecommendation(
                compareTranscriptId(transcriptComparison.getGrch38OncokbTranscript(), transcriptComparison.getRecommendedGrch38Canonical())
            );
        }
    }

    public void validateEnsemblCanonicalGeneAndTranscript() {
        // Make sure ensembl grch38 has ensembl canonical gene and transcript for all the genes that we have
        Pageable pageable = Pageable.ofSize(50);
        Page<Gene> genePage = geneService.findAll(pageable);
        while (!genePage.isEmpty()) {
            genePage
                .get()
                .forEach(gene -> {
                    Optional<EnsemblGene> ensemblGeneOptional = ensemblGeneService.findCanonicalEnsemblGene(
                        gene.getEntrezGeneId(),
                        ReferenceGenome.GRCh37
                    );
                    if (ensemblGeneOptional.isEmpty()) {
                        log.error(
                            "The gene {} {} does not have a canonical transcript in ensembl gene... try to import",
                            gene.getEntrezGeneId(),
                            gene.getHugoSymbol()
                        );
                        mainService.createCanonicalEnsemblGene(ReferenceGenome.GRCh37, gene.getEntrezGeneId());
                    }
                });

            pageable = pageable.next();
            genePage = geneService.findAll(pageable);
        }
    }

    public void checkTranscript() {
        Pageable pageable = Pageable.ofSize(50);
        Page<Gene> genePage = geneService.findAll(pageable);
        List<TranscriptComparison> comparisonResult = new ArrayList<>();
        log.info(
            Arrays
                .asList(
                    new String[] {
                        "gene_id",
                        "entrez_gene_id",
                        "hgnc_id",
                        "hugo_symbol",
                        "gene_panels",
                        "num_of_variants",
                        "grch37_msk_transcript",
                        "grch37_same_oncokb_msk_id",
                        "grch37_same_oncokb_msk_seq",
                        "grch37_ensembl_canonical",
                        "grch37_gn_canonical",
                        "grch37_same_ensembl_gn_id",
                        "grch37_same_ensembl_gn_seq",
                        "grch38_ensembl_canonical",
                        "grch38_gn_canonical",
                        "grch38_mane_select",
                        "grch38_same_mane_select_grch37_ensembl_id",
                        "grch38_same_mane_select_grch37_ensembl_seq",
                        "grch38_same_mane_select_grch37_gn_id",
                        "grch38_same_mane_select_grch37_gn_seq",
                        "grch38_mane_plus_clinical",
                        "grch38_same_mane_clinical_grch37_ensembl_id",
                        "grch38_same_mane_clinical_grch37_ensembl_seq",
                        "grch38_same_mane_clinical_grch37_gn_id",
                        "grch38_same_mane_clinical_grch37_gn_seq",
                        "grch38_mane_plus_clinical_is_longer_than_select",
                        "recommended_grch37_canonical",
                        "grch37_oncokb_transcript",
                        "grch37_same_to_recommendation",
                        "recommended_grch37_canonical_note",
                        "recommended_grch37_canonical_panel",
                        "recommended_grch38_canonical",
                        "grch38_oncokb_transcript",
                        "grch38_same_to_recommendation",
                        "recommended_grch38_canonical_note",
                        "recommended_grch38_canonical_panel",
                    }
                )
                .stream()
                .collect(Collectors.joining("\t"))
        );
        while (!genePage.isEmpty()) {
            genePage
                .get()
                .forEach(gene -> {
                    //                if (gene.getFlags().stream().filter(flag -> FlagType.GENE_PANEL.name().equals(flag.getType()) && GenePanelFlagEnum.MSK_IMPACT_505.name().equals(flag.getFlag())).findAny().isEmpty())
                    //                    return;
                    TranscriptComparison transcriptComparison = new TranscriptComparison();
                    transcriptComparison.setGene(gene);

                    transcriptComparison.setNumOfVariants(alterationService.findByGeneId(gene.getId()).size());

                    // First compare the MSK-IMPACT transcript with OncoKB transcript to see whether they are aligned.
                    List<TranscriptDTO> transcriptDTOs = transcriptService.findByGene(gene);
                    transcriptComparison.setGrch37Transcripts(
                        transcriptDTOs
                            .stream()
                            .filter(transcriptDTO -> ReferenceGenome.GRCh37.equals(transcriptDTO.getReferenceGenome()))
                            .collect(Collectors.toList())
                    );
                    transcriptComparison.setGrch38Transcripts(
                        transcriptDTOs
                            .stream()
                            .filter(transcriptDTO -> ReferenceGenome.GRCh38.equals(transcriptDTO.getReferenceGenome()))
                            .collect(Collectors.toList())
                    );

                    Optional<TranscriptDTO> grch37OncokbTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            ReferenceGenome.GRCh37.equals(transcriptDTO.getReferenceGenome()) &&
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.ONCOKB.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (grch37OncokbTranscript.isPresent()) {
                        transcriptComparison.setGrch37OncokbTranscript(getTranscriptWithSeq(grch37OncokbTranscript.get()));
                    }

                    Optional<TranscriptDTO> mskTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> MSK_IMPACT_CANONICAL.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (mskTranscript.isPresent()) {
                        transcriptComparison.setMskTranscript(getTranscriptWithSeq(mskTranscript.get()));
                    }

                    transcriptComparison.setSameOncokbMSkId(
                        compareTranscriptId(transcriptComparison.getGrch37OncokbTranscript(), transcriptComparison.getMskTranscript())
                    );
                    transcriptComparison.setSameOncokbMSkSeq(
                        compareTranscriptSeq(transcriptComparison.getGrch37OncokbTranscript(), transcriptComparison.getMskTranscript())
                    );

                    Optional<TranscriptDTO> grch37EnsemblTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            ReferenceGenome.GRCh37.equals(transcriptDTO.getReferenceGenome()) &&
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.ENSEMBL_CANONICAL.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (
                        grch37EnsemblTranscript.isPresent() &&
                        grch37EnsemblTranscript.get().getProteinSequence() != null &&
                        grch37EnsemblTranscript.get().getProteinSequence().getSequence().length() > 0
                    ) {
                        transcriptComparison.setGrch37EnsemblCanonical(getTranscriptWithSeq(grch37EnsemblTranscript.get()));
                    }
                    Optional<TranscriptDTO> grch37GnlTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            ReferenceGenome.GRCh37.equals(transcriptDTO.getReferenceGenome()) &&
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.GN_CANONICAL.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (
                        grch37GnlTranscript.isPresent() &&
                        grch37GnlTranscript.get().getProteinSequence() != null &&
                        grch37GnlTranscript.get().getProteinSequence().getSequence().length() > 0
                    ) {
                        transcriptComparison.setGrch37GnCanonical(getTranscriptWithSeq(grch37GnlTranscript.get()));
                    }
                    transcriptComparison.setGrch37SameEnsemblGnId(
                        compareTranscriptId(transcriptComparison.getGrch37EnsemblCanonical(), transcriptComparison.getGrch37GnCanonical())
                    );
                    transcriptComparison.setGrch37SameEnsemblGnSeq(
                        compareTranscriptSeq(transcriptComparison.getGrch37EnsemblCanonical(), transcriptComparison.getGrch37GnCanonical())
                    );

                    Optional<TranscriptDTO> grch38OncokbTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            ReferenceGenome.GRCh38.equals(transcriptDTO.getReferenceGenome()) &&
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.ONCOKB.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (grch38OncokbTranscript.isPresent()) {
                        transcriptComparison.setGrch38OncokbTranscript(getTranscriptWithSeq(grch38OncokbTranscript.get()));
                    }

                    Optional<TranscriptDTO> grch38EnsemblTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            ReferenceGenome.GRCh38.equals(transcriptDTO.getReferenceGenome()) &&
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.ENSEMBL_CANONICAL.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (
                        grch38EnsemblTranscript.isPresent() &&
                        grch38EnsemblTranscript.get().getProteinSequence() != null &&
                        grch38EnsemblTranscript.get().getProteinSequence().getSequence().length() > 0
                    ) {
                        transcriptComparison.setGrch38EnsemblCanonical(getTranscriptWithSeq(grch38EnsemblTranscript.get()));
                    }
                    Optional<TranscriptDTO> grch38GnlTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            ReferenceGenome.GRCh38.equals(transcriptDTO.getReferenceGenome()) &&
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.GN_CANONICAL.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (
                        grch38GnlTranscript.isPresent() &&
                        grch38GnlTranscript.get().getProteinSequence() != null &&
                        grch38GnlTranscript.get().getProteinSequence().getSequence().length() > 0
                    ) {
                        transcriptComparison.setGrch38GnCanonical(getTranscriptWithSeq(grch38GnlTranscript.get()));
                    }

                    Optional<TranscriptDTO> maneSelectTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.MANE_SELECT.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .findFirst();
                    if (maneSelectTranscript.isPresent()) {
                        transcriptComparison.setManeSelect(getTranscriptWithSeq(maneSelectTranscript.get()));
                    }
                    transcriptComparison.setSameManeSelectGrch37EnsemblId(
                        compareTranscriptId(transcriptComparison.getManeSelect(), transcriptComparison.getGrch37EnsemblCanonical())
                    );
                    transcriptComparison.setSameManeSelectGrch37EnsemblSeq(
                        compareTranscriptSeq(transcriptComparison.getManeSelect(), transcriptComparison.getGrch37EnsemblCanonical())
                    );
                    transcriptComparison.setSameManeSelectGrch37GnId(
                        compareTranscriptId(transcriptComparison.getManeSelect(), transcriptComparison.getGrch37GnCanonical())
                    );
                    transcriptComparison.setSameManeSelectGrch37GnSeq(
                        compareTranscriptSeq(transcriptComparison.getManeSelect(), transcriptComparison.getGrch37GnCanonical())
                    );

                    List<TranscriptDTO> manePlusClinicalTranscript = transcriptDTOs
                        .stream()
                        .filter(transcriptDTO ->
                            transcriptDTO
                                .getFlags()
                                .stream()
                                .filter(flag -> TranscriptFlagEnum.MANE_PLUS_CLINICAL.name().equals(flag.getFlag()))
                                .findAny()
                                .isPresent()
                        )
                        .collect(Collectors.toList());
                    if (!manePlusClinicalTranscript.isEmpty()) {
                        transcriptComparison.setManePlusClinical(
                            manePlusClinicalTranscript
                                .stream()
                                .map(transcriptDTO -> getTranscriptWithSeq(transcriptDTO))
                                .collect(Collectors.toList())
                        );
                    }
                    if (transcriptComparison.getManePlusClinical().size() == 1) {
                        transcriptComparison.setManePlusClinicalIsLonger(
                            transcriptComparison.getManePlusClinical().get(0).getSeqLength() >
                            transcriptComparison.getManeSelect().getSeqLength()
                        );
                        transcriptComparison.setSameManeClinicalGrch37EnsemblId(
                            compareTranscriptId(
                                transcriptComparison.getManePlusClinical().get(0),
                                transcriptComparison.getGrch37EnsemblCanonical()
                            )
                        );
                        transcriptComparison.setSameManeClinicalGrch37EnsemblSeq(
                            compareTranscriptSeq(
                                transcriptComparison.getManePlusClinical().get(0),
                                transcriptComparison.getGrch37EnsemblCanonical()
                            )
                        );
                        transcriptComparison.setSameManeClinicalGrch37GnId(
                            compareTranscriptId(
                                transcriptComparison.getManePlusClinical().get(0),
                                transcriptComparison.getGrch37GnCanonical()
                            )
                        );
                        transcriptComparison.setSameManeClinicalGrch37GnSeq(
                            compareTranscriptSeq(
                                transcriptComparison.getManePlusClinical().get(0),
                                transcriptComparison.getGrch37GnCanonical()
                            )
                        );
                    }
                    getTranscriptRecommendation(transcriptComparison);
                    comparisonResult.add(transcriptComparison);
                    log.info(transcriptComparison.toString());
                });

            pageable = pageable.next();
            genePage = geneService.findAll(pageable);
        }
    }
}
