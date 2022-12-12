package org.mskcc.oncokb.curation.web.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.joda.time.DateTimeZone;
import org.mskcc.oncokb.curation.domain.Article;
import org.mskcc.oncokb.curation.domain.ArticleFullText;
import org.mskcc.oncokb.curation.importer.PubMedImporter;
import org.mskcc.oncokb.curation.service.ArticleFullTextService;
import org.mskcc.oncokb.curation.service.ArticleService;
import org.mskcc.oncokb.curation.service.SolrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ArticleController {

    private final Logger log = LoggerFactory.getLogger(ArticleController.class);

    private final PubMedImporter pubMedImporter;

    private final ArticleService articleService;
    private final ArticleFullTextService articleFullTextService;

    private final SolrService solrService;

    public ArticleController(
        PubMedImporter pubMedImporter,
        ArticleService articleService,
        ArticleFullTextService articleFullTextService,
        SolrService solrService
    ) {
        this.pubMedImporter = pubMedImporter;
        this.articleService = articleService;
        this.articleFullTextService = articleFullTextService;
        this.solrService = solrService;
    }

    @GetMapping("/articles/import")
    public ResponseEntity<Void> importPubMeArticle(@RequestParam(required = true) String pmid) throws FileNotFoundException {
        importArticle(pmid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/articles/import-all")
    public ResponseEntity<Void> importPubMeArticle() throws FileNotFoundException {
        final int PAGE_SIZE = 50;
        Page<Article> page = articleService.findAll(Pageable.ofSize(PAGE_SIZE));
        for (int i = (int) Math.floor(Math.random() * page.getTotalPages()); i < page.getTotalPages(); i++) {
            log.info("On page: {}", i);
            Page<Article> pageResult = articleService.findAll(PageRequest.of(i, PAGE_SIZE));
            for (Article article : pageResult) {
                if (StringUtils.isEmpty(article.getTitle())) {
                    importArticle(article.getPmid());
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/articles/solr-index-all")
    public ResponseEntity<Void> solrIndexAll() throws FileNotFoundException {
        final int PAGE_SIZE = 50;

        return ResponseEntity.ok().build();
    }

    private void importArticle(String pmid) throws FileNotFoundException {
        boolean saved = pubMedImporter.addArticlePMID(pmid);
        if (saved) {
            saved = pubMedImporter.addArticleFullText(pmid);
            if (saved) {
                pubMedImporter.uploadArticleFullTextToS3(pmid);
            } else {
                log.warn("The full text for PMID {} wasn't saved", pmid);
            }
        } else {
            log.warn("The article for PMID {} wasn't saved", pmid);
        }
        pubMedImporter.removePublicationIfExist(pmid);
    }
}
