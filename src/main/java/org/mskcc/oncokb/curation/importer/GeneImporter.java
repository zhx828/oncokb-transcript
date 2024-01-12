package org.mskcc.oncokb.curation.importer;

import static org.mskcc.oncokb.curation.util.FileUtils.readTrimmedLinesStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.oncokb.curation.config.application.ApplicationProperties;
import org.mskcc.oncokb.curation.domain.Flag;
import org.mskcc.oncokb.curation.domain.Gene;
import org.mskcc.oncokb.curation.domain.Synonym;
import org.mskcc.oncokb.curation.domain.enumeration.FlagType;
import org.mskcc.oncokb.curation.domain.enumeration.InfoType;
import org.mskcc.oncokb.curation.domain.enumeration.SynonymType;
import org.mskcc.oncokb.curation.service.FlagService;
import org.mskcc.oncokb.curation.service.GeneService;
import org.mskcc.oncokb.curation.service.InfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GeneImporter {

    private final Logger log = LoggerFactory.getLogger(GeneImporter.class);
    private final String SYNONYM_SEPARATOR = "\\|";
    private final String GENE_FILE_PATH = "data/gene/cbioportal_v4/gene-info-Apr-11-2023.txt";
    private final String GENE_PANEL_FILE_PATH = "data/gene/all_gene_panels_09152023.txt";
    private final String GENE_VERSION = "v4";
    private final String GENE_VERSION_DATE = "2023-04-11"; // see allowed format https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html

    final CurationDataService curationDataService;
    final InfoService infoService;
    final GeneService geneService;
    final FlagService flagService;

    public GeneImporter(
        ApplicationProperties applicationProperties,
        CurationDataService curationDataService,
        InfoService infoService,
        GeneService geneService,
        FlagService flagService
    ) {
        this.curationDataService = curationDataService;
        this.infoService = infoService;
        this.geneService = geneService;
        this.flagService = flagService;
    }

    private List<String[]> getPortalGenes() {
        URL geneFileUrl = getClass().getClassLoader().getResource(GENE_FILE_PATH);
        List<String> readmeFileLines;
        try {
            InputStream is = geneFileUrl.openStream();
            readmeFileLines = readTrimmedLinesStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return readmeFileLines
            .stream()
            .map(line -> line.split("\t"))
            .filter(line -> {
                // as long as gene has entrez gene id and hugo symbol, we import
                if (line.length >= 2) {
                    return StringUtils.isNumeric(line[0]) && Integer.parseInt(line[0]) > 0;
                } else {
                    return false;
                }
            })
            .sorted(Comparator.comparingInt(o -> Integer.parseInt(o[0])))
            .collect(Collectors.toList());
    }

    public void importPortalGenes() {
        List<String[]> portalGenes = getPortalGenes();
        for (var i = 0; i < portalGenes.size(); i++) {
            String[] line = portalGenes.get(i);
            Integer entrezGeneId = Integer.parseInt(line[0]);
            String hugoSymbol = line[1];
            Set<Synonym> geneSynonyms = new HashSet<>();

            if (line.length > 5) {
                geneSynonyms =
                    Arrays
                        .stream(line[5].split(SYNONYM_SEPARATOR))
                        .filter(synonym -> StringUtils.isNotEmpty(synonym.trim()))
                        .map(synonym -> {
                            Synonym geneSynonym = new Synonym();
                            geneSynonym.setName(synonym.trim());
                            geneSynonym.setType(SynonymType.GENE.name());
                            return geneSynonym;
                        })
                        .collect(Collectors.toSet());
            }

            Optional<Gene> geneOptional = this.geneService.findGeneByEntrezGeneId(entrezGeneId);
            if (geneOptional.isPresent()) {
                Gene gene = geneOptional.get();
                gene.setHugoSymbol(hugoSymbol);
                gene.setSynonyms(geneSynonyms);
                this.geneService.partialUpdate(gene);
                log.debug("Updated gene {}", gene);
            } else {
                Gene gene = new Gene();
                gene.setEntrezGeneId(entrezGeneId);
                gene.setHugoSymbol(hugoSymbol);
                gene.setSynonyms(geneSynonyms);
                this.geneService.save(gene);
                log.debug("Saved gene {}", gene);
            }
            if ((i + 1) % 1000 == 0) {
                log.info("Saved/updated {} genes", i + 1);
            }
        }

        this.infoService.updateInfo(InfoType.GENE_VERSION, GENE_VERSION, GENE_VERSION_DATE);
    }

    public void upgradePortalGeneVersion(String newGeneVersionFolderName) {
        String newGeneVersionFolderDict = curationDataService.getDataDirectory() + "/" + newGeneVersionFolderName;
        String UPDATE_FILE_PATH = newGeneVersionFolderDict + "/gene-updates.md";

        try {
            List<String> readmeFileLines = readTrimmedLinesStream(new FileInputStream(UPDATE_FILE_PATH));
            deleteGeneWhileUpgrading(readmeFileLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.infoService.updateInfo(InfoType.GENE_VERSION, GENE_VERSION, GENE_VERSION_DATE);
    }

    private void deleteGeneWhileUpgrading(List<String> lines) {
        List<String> genesRemovedLines = findGeneUpdateSectionLines(lines, "Genes Removed");
        if (genesRemovedLines.size() > 0) {
            log.warn("Found genes that have been removed");
            for (String line : genesRemovedLines) {
                String[] cells = line.split("\t");
                if (cells.length >= 2) {
                    String hugoSymbol = cells[0];
                    String entrezGeneId = cells[1];
                    if (StringUtil.isNotEmpty(entrezGeneId)) {
                        Optional<Gene> geneOptional = geneService.findGeneByEntrezGeneId(Integer.parseInt(entrezGeneId));
                        if (geneOptional.isEmpty()) {
                            log.error("Cannot find the gene {}", line);
                        } else {
                            Gene gene = geneOptional.get();
                            if (!gene.getHugoSymbol().equalsIgnoreCase(hugoSymbol)) {
                                log.warn("Gene hugo symbol does not match, New: {}, Old: {}", hugoSymbol, gene.getHugoSymbol());
                            }
                            geneService.delete(gene.getId());
                            log.info("Deleted gene {} {}", entrezGeneId, hugoSymbol);
                        }
                    } else {
                        log.error("Gene that been removed does not have an entrez gene id {}", line);
                    }
                }
            }
        }
    }

    private void updateGeneHugoSymbolWhileUpgrading(List<String> lines) {
        List<String> hugoSymbolChangedLines = findGeneUpdateSectionLines(lines, "Hugo Symbol Update");
        if (hugoSymbolChangedLines.size() > 0) {
            log.warn("Found genes that have been removed");
            for (String line : hugoSymbolChangedLines) {
                String[] cells = line.split("\t");
                if (cells.length >= 2) {
                    String hugoSymbol = cells[0];
                    String entrezGeneId = cells[1];
                    if (StringUtil.isNotEmpty(entrezGeneId)) {
                        Optional<Gene> geneOptional = geneService.findGeneByEntrezGeneId(Integer.parseInt(entrezGeneId));
                        if (geneOptional.isEmpty()) {
                            log.error("Cannot find the gene {}", line);
                        } else {
                            Gene gene = geneOptional.get();
                            if (!gene.getHugoSymbol().equalsIgnoreCase(hugoSymbol)) {
                                log.warn("Gene hugo symbol does not match, New: {}, Old: {}", hugoSymbol, gene.getHugoSymbol());
                            }
                            geneService.delete(gene.getId());
                            log.info("Deleted gene {} {}", entrezGeneId, hugoSymbol);
                        }
                    } else {
                        log.error("Gene that been removed does not have an entrez gene id {}", line);
                    }
                }
            }
        }
    }

    private List<String> findGeneUpdateSectionLines(List<String> lines, String sectionName) {
        List<String> section = findSectionByCharacter(lines, "# " + sectionName);
        if (section.size() > 0) {
            section = findSectionByCharacter(section, "```");
        }
        return section;
    }

    private List<String> findSectionByCharacter(List<String> lines, String characters) {
        int start = -1, end = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(characters)) {
                if (start == -1) start = i + 1; else end = i - 1;
            }
        }

        if (start != -1 && start <= end) {
            return lines.subList(start, end);
        } else {
            return new ArrayList<>();
        }
    }

    private List<String[]> getGenePanels() {
        URL fileUrl = getClass().getClassLoader().getResource(GENE_PANEL_FILE_PATH);
        List<String> readmeFileLines;
        try {
            InputStream is = fileUrl.openStream();
            readmeFileLines = readTrimmedLinesStream(is);
            if (readmeFileLines.size() > 0) {
                readmeFileLines.remove(0);
            } else {
                log.error("Empty gene panel file.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return readmeFileLines.stream().map(line -> line.split("\t")).collect(Collectors.toList());
    }

    public void importGenePanels() {
        List<String[]> genePanels = getGenePanels();
        for (var i = 0; i < genePanels.size(); i++) {
            String[] line = genePanels.get(i);
            // as long as gene has entrez gene id or hugo symbol, we import
            if (line.length < 2) {
                log.error("The line does not have necessary data points {}", line);
            }
            String panel = line[0];
            Integer entrezGeneId = StringUtils.isEmpty(line[1]) ? null : Integer.parseInt(line[1]);
            String hugoSymbol = line.length > 2 ? line[2] : null;
            Optional<Flag> geneFlagOptional = flagService.findByTypeAndFlag(FlagType.GENE_PANEL, panel);
            if (geneFlagOptional.isEmpty()) {
                log.error("Cannot find panel {}", panel);
                continue;
            }
            Optional<Gene> geneOptional = Optional.empty();
            if (entrezGeneId != null) {
                geneOptional = geneService.findGeneByEntrezGeneId(entrezGeneId);
            } else if (StringUtils.isNotEmpty(hugoSymbol)) {
                List<Gene> genes = geneService.findGeneByHugoSymbolOrGeneAliasesIn(hugoSymbol);
                if (genes.size() > 0) {
                    geneOptional = Optional.of(genes.get(0));
                }
            }

            if (geneOptional.isEmpty()) {
                log.error("Cannot find gene {} {} {}", panel, entrezGeneId, hugoSymbol);
            } else {
                if (geneOptional.get().getFlags().contains(geneFlagOptional.get())) {
                    log.error("The gene flag already included {} {}", geneFlagOptional.get().getFlag(), line);
                } else {
                    geneOptional.get().getFlags().add(geneFlagOptional.get());
                    geneService.partialUpdate(geneOptional.get());
                }
            }

            if ((i + 1) % 1000 == 0) {
                log.info("Saved/updated {} panel genes", i + 1);
            }
        }
    }
}
