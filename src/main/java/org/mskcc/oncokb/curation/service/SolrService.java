package org.mskcc.oncokb.curation.service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.*;
import org.apache.solr.client.solrj.response.Cluster;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.util.NamedList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mskcc.oncokb.curation.config.ApplicationProperties;
import org.mskcc.oncokb.curation.domain.Article;
import org.mskcc.oncokb.curation.domain.ArticleFullText;
import org.mskcc.oncokb.curation.service.model.SolrItem;
import org.mskcc.oncokb.curation.service.solr.ResultMap;
import org.mskcc.oncokb.curation.service.solr.SearchResult;
import org.mskcc.oncokb.curation.service.solr.SolrClientService;
import org.mskcc.oncokb.curation.service.solr.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

// TODO: The code is subjected to further review/refactoring. It comes from AI/ML team, developed by Luke Czapla.
@Service
public class SolrService {

    private static final Logger log = LoggerFactory.getLogger(SolrService.class);

    private static final int PAGE_SIZE = 50;

    private SolrClientService solrClientService;
    private ArticleService articleService;
    private ArticleFullTextService articleFullTextService;

    public SolrService(SolrClientService solrClientService, ArticleService articleService, ArticleFullTextService articleFullTextService) {
        this.solrClientService = solrClientService;
        this.articleService = articleService;
        this.articleFullTextService = articleFullTextService;
    }

    public void updateSolrFullTextArticles() {
        Page<Article> page = articleService.findAll(Pageable.ofSize(PAGE_SIZE));
        for (int i = 0; i < page.getTotalPages(); i++) {
            log.info("On page: {}", i);
            Page<Article> pageResult = articleService.findAll(PageRequest.of(i, PAGE_SIZE));
            for (Article article : pageResult) {
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(article.getTitle())) {
                    Optional<ArticleFullText> articleFullTextOptional = articleFullTextService.findByArticle(article);

                    Map<String, List<String>> articleMap = new HashMap<>();
                    Map<String, List<String>> extraMap = new HashMap<>();
                    String text = this.toText(article, articleFullTextOptional.isPresent() ? articleFullTextOptional.get() : null);
                    articleMap.put("text", Collections.singletonList(text));
                    articleMap.put("pmid", Collections.singletonList(article.getPmid()));

                    if (article.getPubDate() != null) extraMap.put("date", Collections.singletonList(article.getPubDate().toString()));
                    extraMap.put("hasFullText", Collections.singletonList(Boolean.toString(articleFullTextOptional.isPresent())));
                    try {
                        SolrDocumentList sdl = solrClientService.find("id:" + article.getPmid());
                        if (sdl.size() > 0) {
                            for (SolrDocument doc : sdl) {
                                solrClientService.delete((String) doc.get("id"), true);
                            }
                        }
                        solrClientService.add(article.getPmid(), articleMap, extraMap);
                    } catch (SolrServerException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        log.info("Done updating Solr collection");
    }

    public void updateSolrSupportingInformation(int pageNumber, int pageSize) {
        Page<FullText> fullTextList;
        List<Map<String, List<String>>> articleMaps = new ArrayList<>();
        List<Map<String, List<String>>> extraMaps = new ArrayList<>();
        List<Triple> values = Collections.synchronizedList(new ArrayList<>());
        List<String> newIds = new ArrayList<>();
        List<String> deletedIds = Collections.synchronizedList(new ArrayList<>());
        do {
            fullTextList = fullTextRepository.findAllSupplementary(PageRequest.of(pageNumber++, pageSize)); //fullTextRepository.findAll().stream().filter(f -> f.getPmId().contains("S")).sorted(Comparator.comparing(FullText::getPmId)).collect(Collectors.toList());
            List<FullText> fullTextsS = fullTextList.getContent();
            log.info("{} supporting text (PDF/DOC/DOCX) items to look at", fullTextsS.size());
            fullTextsS
                .parallelStream()
                .forEach(ft -> {
                    Article a = articleRepository.findByPmId(ft.getPmId().substring(0, ft.getPmId().indexOf("S")));
                    boolean update = false;
                    if (
                        a.getSupportingText() != null &&
                        (a.getSupportingText().endsWith(ft.getPmId()) || a.getSupportingText().contains(ft.getPmId() + ","))
                    ) {
                        //log.info("{} already contained in article {} supportingText field", ft.getPmId(), a.getPmId());
                    } else if (a.getSupportingText() != null && a.getSupportingText().length() > 0) {
                        if (a.getSupportingText().endsWith(",")) a.setSupportingText(
                            a.getSupportingText() + ft.getPmId()
                        ); else a.setSupportingText(a.getSupportingText() + "," + ft.getPmId());
                        update = true;
                    } else {
                        a.setSupportingText(ft.getPmId());
                        update = true;
                    }
                    if (update) articleRepository.save(a);
                    Map<String, List<String>> articleMap = new HashMap<>();
                    Map<String, List<String>> extraMap = new HashMap<>();
                    articleMap.put("text", Collections.singletonList(ft.getTextEntry()));
                    articleMap.put("authors", Collections.singletonList(a.getAuthors()));
                    articleMap.put("pmid", Collections.singletonList(a.getPmId()));
                    articleMap.put("pmid_supporting", Collections.singletonList(ft.getPmId()));
                    if (a.getPublicationDate() != null) extraMap.put(
                        "date",
                        Collections.singletonList(a.getPublicationDate().toDateTime(DateTimeZone.UTC).toString())
                    ); else if (a.getPubDate() != null) extraMap.put(
                        "date",
                        Collections.singletonList(a.getPubDate().toDateTime(DateTimeZone.UTC).toString())
                    );
                    extraMap.put("hasFullText", Collections.singletonList("true"));
                    String newId =
                        Integer.toHexString(articleMap.hashCode()) +
                        (ft.getPmId().length() > 3 ? ft.getPmId().substring(ft.getPmId().length() - 3) : ft.getPmId());

                    try {
                        SolrDocumentList sdl = this.find("pmid_supporting:" + ft.getPmId());
                        if (sdl.size() > 0) {
                            for (SolrDocument doc : sdl) {
                                deletedIds.add((String) doc.get("id"));
                            }
                        }
                        values.add(new Triple(articleMap, extraMap, newId));
                    } catch (IOException | SolrServerException e) {
                        log.error(e.getMessage());
                    }
                });
            log.info("Sending batch to Solr addUpdateDeleteMany() with size {}", values.size());
            try {
                if (values.size() > 0) {
                    values.forEach(t -> {
                        articleMaps.add(t.articleMap);
                        extraMaps.add(t.extraMap);
                        newIds.add(t.newId);
                    });
                    values.clear();
                    solrClientTool.addUpdateDeleteMany(this.collection, articleMaps, extraMaps, newIds, deletedIds);
                } else if (deletedIds.size() > 0) {
                    log.info("Nothing to add but {} items to delete", deletedIds.size());
                    solrClientTool.deleteMany(this.collection, deletedIds);
                }
            } catch (SolrServerException | IOException e) {
                log.error("SolrClientTool.addUpdateDeleteMany/deleteMany: {}", e.getMessage());
                e.printStackTrace();
            }
            newIds.clear();
            deletedIds.clear();
            articleMaps.clear();
            extraMaps.clear();
        } while (fullTextList.hasNext());
    }

    public void updateSolrSupportingInformation() {
        updateSolrSupportingInformation(0, 3000);
    }

    public void addArticle(String pmid) {
        Article a = articleRepository.findByPmId(pmid);
        if (a == null) {
            log.error("Cannot add to Solr collection: No article with PMID {} exists", pmid);
            return;
        }
        Map<String, List<String>> articleMap = new HashMap<>();
        Map<String, List<String>> extraMap = new HashMap<>();
        String text = Article.toText(a);
        articleMap.put("text", Collections.singletonList(text));
        articleMap.put("pmid", Collections.singletonList(a.getPmId()));
        a.setSolrId(
            Integer.toHexString(articleMap.hashCode()) +
            (a.getPmId().length() > 3 ? a.getPmId().substring(a.getPmId().length() - 3) : a.getPmId())
        );
        if (a.getPublicationDate() != null) extraMap.put(
            "date",
            Collections.singletonList(a.getPublicationDate().toDateTime(DateTimeZone.UTC).toString())
        ); else if (a.getPubDate() != null) extraMap.put(
            "date",
            Collections.singletonList(a.getPubDate().toDateTime(DateTimeZone.UTC).toString())
        );
        if (a.getFulltext() == null) extraMap.put("hasFullText", Collections.singletonList("false")); else extraMap.put(
            "hasFullText",
            Collections.singletonList("true")
        );
        try {
            SolrDocumentList sdl = this.find("pmid:" + a.getPmId());
            if (sdl.size() > 0) {
                for (SolrDocument doc : sdl) {
                    solrClientTool.delete(this.collection, (String) doc.get("id"), true);
                }
            }
            solrClientTool.add(this.collection, articleMap, extraMap);
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }

    public SearchResult searchSolr(
        int limit,
        List<String> genes,
        List<List<String>> geneSynonyms,
        List<String> mutations,
        List<List<String>> mutationSynonyms,
        List<String> drugs,
        List<List<String>> drugSynonyms,
        List<String> cancers,
        List<List<String>> cancerSynonyms,
        List<String> keywords,
        String authors,
        DateTime after
    ) {
        List<String> terms = new ArrayList<>();
        String query = buildSearchExpression(
            terms,
            genes,
            geneSynonyms,
            mutations,
            mutationSynonyms,
            drugs,
            drugSynonyms,
            cancers,
            cancerSynonyms,
            keywords,
            authors,
            after
        );
        log.info(query);
        String freqTerm = null;
        if (terms.size() == 1) freqTerm = escape(terms.get(0)); else if (keywords != null && keywords.size() == 1) freqTerm =
            escape(terms.get(0)); else {
            if (mutations != null && mutations.size() > 0) freqTerm = escape(mutations.get(0)); else freqTerm = escape(terms.get(0));
        }
        try {
            SolrDocumentList result;
            if (query.contains("*")) solrClientTool.setDefaultField("text_ws"); else solrClientTool.setDefaultField("text");
            result = this.find(query, "pmid,pmid_supporting,date,score,text", limit, freqTerm);
            log.info(result.toString());
            List<String> pmIds = new ArrayList<>(result.size());
            List<Float> scores = new ArrayList<>(result.size());
            for (SolrDocument item : result) {
                List<Long> values = (ArrayList<Long>) (item.getFieldValue("pmid"));
                if (item.getFieldValue("pmid_supporting") != null) {
                    List<String> supportingPMID = (ArrayList<String>) (item.getFieldValue("pmid_supporting"));
                    pmIds.add(supportingPMID.get(0));
                } else pmIds.add(values.get(0) + "");
                scores.add((Float) (item.getFieldValue("score")));
            }
            return new SearchResult(result, pmIds, scores);
        } catch (IOException | SolrServerException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public SearchResult searchSolr(int limit, String searchTerm, DateTime after, boolean rank) {
        try {
            SolrDocumentList result;
            solrClientTool.setDefaultField("text");
            if (!searchTerm.contains(":")) result =
                this.find(searchTerm, "pmid,pmid_supporting", limit, rank ? searchTerm : null); else result =
                this.find(searchTerm, rank ? "pmid,pmid_supporting" : "pmid,pmid_supporting,text", limit, searchTerm);
            log.info(result.toString());
            List<String> pmIds = new ArrayList<>(result.size());
            List<Float> scores = new ArrayList<>(result.size());
            for (SolrDocument item : result) {
                //log.info(item.getFieldValue("pmid").getClass().getSimpleName());
                List<Long> values = (ArrayList<Long>) (item.getFieldValue("pmid"));
                if (item.getFieldValue("pmid_supporting") != null) {
                    List<String> supportingPMID = (ArrayList<String>) (item.getFieldValue("pmid_supporting"));
                    pmIds.add(supportingPMID.get(0));
                } else pmIds.add(values.get(0) + "");
                scores.add((Float) (item.getFieldValue("score")));
            }
            return new SearchResult(result, pmIds, scores);
        } catch (IOException | SolrServerException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public SolrDocument findArticle(String pmid) {
        boolean supplementary = pmid.contains("S");
        try {
            SolrDocumentList sdl = this.find((supplementary ? "pmid_supporting:" : "-pmid_supporting:* AND pmid:") + '"' + pmid + '"');
            if (sdl.size() == 1) {
                return sdl.get(0);
            }
            if (sdl.size() == 0) {
                log.error("No SolrDocument found for PMID {}, trying to create", pmid);
                addArticle(pmid);
                this.refreshCollection(this.collection);
                sdl = this.find((supplementary ? "pmid_supporting:" : "-pmid_supporting:* AND pmid:") + '"' + pmid + '"');
                if (sdl.size() == 1) return sdl.get(0);
                log.error("Still could not find the record in collection");
                return null;
            } else { // one or more duplicates! delete one.
                Long max = 0L;
                SolrDocument maxDoc = sdl.get(0);
                for (int i = 0; i < sdl.size(); i++) {
                    SolrDocument doc = sdl.get(i);
                    Long d = (Long) doc.get("_version_");
                    if (d > max) {
                        max = d;
                        maxDoc = doc;
                    }
                }
                for (int i = 0; i < sdl.size(); i++) {
                    SolrDocument doc = sdl.get(i);
                    Long d = (Long) doc.get("_version_");
                    if (d < max) {
                        log.info("Duplicate Solr document: Deleting result {}", i);
                        this.delete((String) doc.get("id"), true);
                    }
                }
                return maxDoc;
            }
        } catch (IOException | SolrServerException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public boolean deleteArticle(String pmid) {
        boolean supplementary = pmid.contains("S");
        try {
            SolrDocumentList sdl = this.find((supplementary ? "pmid_supporting:" : "-pmid_supporting:* AND pmid:") + '"' + pmid + '"');
            log.info("{} document(s) to delete", sdl.size());
            if (sdl.size() == 0) return false;
            for (SolrDocument doc : sdl) {
                Collection<String> fields = doc.getFieldNames();
                if (!supplementary && fields.contains("pmid_supporting")) continue;
                String id = (String) doc.get("id");
                solrClientTool.delete(this.collection, id);
                //System.out.println(doc);
            }
        } catch (IOException | SolrServerException e) {
            log.error(e.getMessage());
        }
        return true;
    }

    public String getText(SolrDocument item) {
        if (item.get("text") instanceof List) return ((List<String>) item.get("text")).get(0); else return (String) item.get("text");
    }

    public static String toText(Article article, ArticleFullText articleFullText) {
        StringBuilder sb = new StringBuilder(50000);
        sb.append(" {!title} ");
        sb.append(article.getTitle());
        if (article.getPubAbstract() != null) {
            sb.append(" {!abstract} ");
            sb.append(article.getPubAbstract());
        }
        if (articleFullText != null) {
            sb.append(" {!fulltext} ");
            if (articleFullText.getText() != null) {
                sb.append(articleFullText.getText());
            }
        }
        return sb.toString();
    }
}
