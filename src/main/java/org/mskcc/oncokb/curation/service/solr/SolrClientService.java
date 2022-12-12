package org.mskcc.oncokb.curation.service.solr;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
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
import org.mskcc.oncokb.curation.service.SolrService;
import org.mskcc.oncokb.curation.service.model.SolrItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SolrClientService {

    private static final Logger log = LoggerFactory.getLogger(SolrService.class);

    private org.apache.solr.client.solrj.SolrClient client;
    private ApplicationProperties applicationProperties;
    private static int count = 0;
    private int reloadRate = 15000;

    private String defaultField;
    private String collection;
    private String parser = "edismax";

    public SolrClientService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        boolean cloud = this.applicationProperties.getSolr().getUrls().size() > 1;
        if (cloud) HttpClientUtil.addRequestInterceptor(new SolrClientService.SolrPreemptiveAuthInterceptor());

        this.client =
            cloud
                ? new CloudSolrClient.Builder(this.applicationProperties.getSolr().getUrls()).withParallelUpdates(true).build()
                : new HttpSolrClient.Builder()
                    .withBaseSolrUrl(this.applicationProperties.getSolr().getUrls().iterator().next())
                    .allowCompression(true)
                    .build();
        this.collection = this.applicationProperties.getSolr().getCollection();
        defaultField = "text";
    }

    private String buildSearchExpression(
        List<String> terms,
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
        StringBuilder sb = new StringBuilder(256);
        int count = 0;
        if (genes != null) for (int i = 0; i < genes.size(); i++) {
            if (count > 0) sb.append(" AND ");
            sb.append("+("); //sb.append("(text:");
            sb.append(quote(escape(genes.get(i))));
            terms.add(genes.get(i));
            if (geneSynonyms != null && geneSynonyms.size() > i && geneSynonyms.get(i) != null && geneSynonyms.get(i).size() > 0) {
                for (int j = 0; j < geneSynonyms.get(i).size(); j++) {
                    sb.append(" OR "); //sb.append(" OR text:");
                    sb.append(quote(escape(geneSynonyms.get(i).get(j))));
                }
            }
            count++;
            sb.append(")");
        }
        if (mutations != null) for (int i = 0; i < mutations.size(); i++) {
            if (count > 0) sb.append(" AND ");
            sb.append("+("); //sb.append("(text:");
            sb.append(quote(escape(mutations.get(i))));
            terms.add(mutations.get(i));
            if (mutationSynonyms != null && mutationSynonyms.get(i) != null && mutationSynonyms.get(i).size() > 0) {
                for (int j = 0; j < mutationSynonyms.get(i).size(); j++) {
                    sb.append(" OR "); //sb.append(" OR text:");
                    sb.append(quote(escape(mutationSynonyms.get(i).get(j))));
                }
            }
            count++;
            sb.append(")");
        }
        if (drugs != null) for (int i = 0; i < drugs.size(); i++) {
            if (count > 0) sb.append(" AND ");
            sb.append("+("); //sb.append("(text:");
            sb.append(quote(escape(drugs.get(i))));
            terms.add(drugs.get(i));
            if (drugSynonyms != null && drugSynonyms.get(i).size() > 0) {
                for (int j = 0; j < drugSynonyms.get(i).size(); j++) {
                    sb.append(" OR "); //sb.append(" OR text:");
                    sb.append(quote(escape(drugSynonyms.get(i).get(j))));
                }
            }
            count++;
            sb.append(")");
        }
        if (cancers != null) for (int i = 0; i < cancers.size(); i++) {
            if (count > 0) sb.append(" AND ");
            sb.append("+("); //sb.append("(text:");
            sb.append(quote(escape(cancers.get(i))));
            terms.add(cancers.get(i));
            if (cancerSynonyms != null && cancerSynonyms.get(i).size() > 0) {
                for (int j = 0; j < cancerSynonyms.get(i).size(); j++) {
                    sb.append(" OR "); //sb.append(" OR text:");
                    sb.append(quote(escape(cancerSynonyms.get(i).get(j))));
                }
            }
            count++;
            sb.append(")");
        }
        if (keywords != null) for (String keyword : keywords) {
            if (count > 0) sb.append(" AND ");
            //sb.append("(");
            if (keyword.contains("\"")) sb.append(keyword); else sb.append(quote(keyword));
            terms.add(0, keyword);
            count++;
            //sb.append(")");
        }
        if (after != null) {
            sb.append(" AND date:[").append(after.toDateTime(DateTimeZone.UTC).toString()).append(" TO NOW]");
        }
        if (authors != null && authors.length() > 0) {
            sb.append(" AND authors:").append(quote(escape(authors)));
        }
        return sb.toString();
    }

    private static String randomId(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 90; // letter 'Z'
        Random random = new Random();

        return random
            .ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65))
            .limit(length)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    private static String randomId() {
        return randomId(20);
    }

    private static List<SolrItem> documentsToItems(SolrDocumentList sdl) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        return binder.getBeans(SolrItem.class, sdl);
    }

    @Deprecated // use paging here!
    private SolrDocumentList getAllDocuments() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("*:*");
        int maxArticles = 30000000;
        query.setRows(maxArticles);
        QueryResponse response = client.query(this.collection, query);
        return response.getResults();
    }

    /**
     * @param queryText The string with the query (e.g. "pmid:12345 AND text:BRAF")
     * @return The list of documents that match the query.
     * @throws IOException
     * @throws SolrServerException
     */
    private SolrDocumentList findDismax(String queryText) throws IOException, SolrServerException {
        //queryText = queryText.replace("\\*", "*"); // not needed
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText).setStart(0).setRows(10000).setIncludeScore(true);
        query.setParam("mm", "100%");
        query.setParam("df", defaultField).setParam("defType", "dismax");
        QueryResponse response = client.query(collection, query);
        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", queryText);
        }
        return results;
    }

    private SolrDocumentList findDismax(String queryText, String fields, int nResults, String freqTerm)
        throws IOException, SolrServerException {
        //queryText = queryText.replace("\\*", "*"); // unescape *, dismax doesn't have wildcards! but necessary? probably not.
        SolrQuery query = new SolrQuery();
        query.setParam("mm", "100%");
        query.setQuery(queryText).setFields(fields).setStart(0).setRows(nResults);
        query.setParam("df", defaultField);
        if (freqTerm != null && freqTerm.charAt(0) == '"' && freqTerm.substring(1).contains("\"")) query.addSort(
            SolrQuery.SortClause.desc("termfreq(" + defaultField + ", " + freqTerm + ")")
        ); else if (freqTerm != null) query.addSort(
            SolrQuery.SortClause.desc("termfreq(" + defaultField + ", " + '"' + freqTerm + '"' + ")")
        );
        query.addSort(SolrQuery.SortClause.desc("date"));
        query.setIncludeScore(true).setParam("defType", "dismax");
        QueryResponse response = client.query(collection, query);

        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", queryText);
        }
        return results;
    }

    public SolrDocumentList find(String queryText) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText).setStart(0).setRows(10000).setIncludeScore(true);
        //        query.setParam("mm", "100%");
        //        query.setParam("df", defaultField).setParam("defType", getParser()); // lucene or edismax (or dismax, etc)
        QueryResponse response = client.query(collection, query);
        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", queryText);
        }
        return results;
    }

    private ResultMap find(String queryText, boolean highlight) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText).setStart(0).setRows(10000).setIncludeScore(true);
        if (highlight) query
            .setHighlight(true)
            .addHighlightField(defaultField)
            .setHighlightSimplePre("<mark>")
            .setHighlightSimplePost("</mark>")
            .setHighlightFragsize(0);
        query.setParam("mm", "100%");
        query.setParam("df", defaultField).setParam("defType", getParser()); // lucene or edismax (or dismax, etc)
        QueryResponse response = client.query(collection, query);
        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", queryText);
        }
        return new ResultMap(response.getExplainMap(), results, response.getHighlighting());
    }

    private SolrDocumentList find(String queryText, String fields, int nResults, String freqTerm) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText).setStart(0).setRows(nResults);
        query.setParam("df", defaultField);
        if (freqTerm != null && freqTerm.charAt(0) == '"' && freqTerm.substring(1).contains("\"")) {
            query.setFields(fields, "termfreq(" + defaultField + ", " + freqTerm + ")");
            query.addSort(SolrQuery.SortClause.desc("termfreq(" + defaultField + ", " + freqTerm + ")"));
        } else if (freqTerm != null) {
            query.setFields(fields, "termfreq(" + defaultField + ", " + '"' + freqTerm + '"' + ")");
            query.addSort(SolrQuery.SortClause.desc("termfreq(" + defaultField + ", " + '"' + freqTerm + '"' + ")"));
        } else query.setFields(fields);
        //query.addSort(SolrQuery.SortClause.desc("date"));
        query.setIncludeScore(true).setParam("defType", "edismax");
        QueryResponse response = client.query(collection, query);

        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", queryText);
        }
        return results;
    }

    private SolrDocumentList deepPage(String query, int rows) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery(query)
            .setRows(rows)
            .setParam("df", defaultField)
            .setParam("defType", "edismax")
            .addSort(SolrQuery.SortClause.desc("id"))
            .addSort(SolrQuery.SortClause.desc("score"));
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        while (!done) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse response = client.query(this.collection, solrQuery);
            String nextCursorMark = response.getNextCursorMark();
            solrDocumentList.addAll(response.getResults());
            if (cursorMark.equals(nextCursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }
        return solrDocumentList;
    }

    private SolrDocumentList findClustering(String queryText) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText);
        query.setStart(0).setRows(200).setIncludeScore(true);
        query.setParam("qt", "/clustering");
        //query.setParam("df", "all");
        query.setParam("defType", "edismax"); // lucene or edismax (or dismax, etc)
        QueryResponse response = client.query(this.collection, query);
        if (response.getClusteringResponse() != null && response.getClusteringResponse().getClusters() != null) {
            List<Cluster> clusters = response.getClusteringResponse().getClusters();
            int item = 1;
            for (Cluster c : clusters) {
                log.info("Cluster " + item);
                log.info(c.toString());
                log.info(c.getLabels().toString());
                log.info(c.getDocs().toString());
                log.info("Subclusters:");
                int item2 = 1;
                if (c.getSubclusters() != null) for (Cluster c2 : c.getSubclusters()) {
                    log.info("Subcluster " + item2);
                    log.info(c2.toString());
                    log.info(c2.getLabels().toString());
                    log.info(c2.getDocs().toString());
                    item2++;
                }
                item++;
            }
        }
        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", queryText);
        }
        return results;
    }

    private ResultMap queryCount(String phrase, String fq) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery().setQuery(phrase).setParam("df", defaultField).setParam("fq", fq);
        query.setParam("debugQuery", "true").setFields("id,pmid,pmid_supporting");
        QueryResponse response = client.query(collection, query);
        return new ResultMap(response.getExplainMap(), response.getResults(), null);
    }

    private ResultMap queryHighlightFragments(String queryText, int nResults) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText).setFields("id,text,pmid,pmid_supporting");
        query.setHighlight(true).addHighlightField(defaultField).setHighlightSimplePre("<mark>").setHighlightSimplePost("</mark>");
        query.setIncludeScore(true); //.setParam("debugQuery", "true");
        query.setStart(0).setRows(nResults).setParam("defType", "edismax").setParam("df", defaultField);
        QueryResponse response = client.query(collection, query);
        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", query);
            return null;
        }
        return new ResultMap(response.getExplainMap(), results, response.getHighlighting());
    }

    private ResultMap queryHighlight(String queryText, int nResults) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText).setFields("id,pmid,pmid_supporting");
        query
            .setHighlight(true)
            .addHighlightField(defaultField)
            .setHighlightSimplePre("<mark>")
            .setHighlightSimplePost("</mark>")
            .setHighlightFragsize(0);
        query.setIncludeScore(true); //.setParam("debugQuery", "true");
        query.setStart(0).setRows(nResults).setParam("defType", "edismax").setParam("df", defaultField);
        QueryResponse response = client.query(collection, query);
        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", query);
            return null;
        }
        return new ResultMap(response.getExplainMap(), results, response.getHighlighting());
    }

    private ResultMap queryHighlight(String queryText, String fq, int nResults) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryText).setFields("id,pmid,pmid_supporting");
        query
            .setHighlight(true)
            .addHighlightField(defaultField)
            .setHighlightSimplePre("<mark>")
            .setHighlightSimplePost("</mark>")
            .setHighlightFragsize(0);
        query.setIncludeScore(true).setParam("fq", fq); //.setParam("debugQuery", "true")
        query.setStart(0).setRows(nResults).setParam("defType", "edismax").setParam("df", defaultField); // WAS lucene!!!!!
        QueryResponse response = client.query(collection, query);
        SolrDocumentList results = response.getResults();
        if (results.size() == 0) {
            log.info("No Solr query results found for {}", query);
            return null;
        }
        return new ResultMap(response.getExplainMap(), results, response.getHighlighting());
    }

    private SolrDocument getDocument(String id) throws SolrServerException, IOException {
        return client.getById(collection, id);
    }

    private SolrDocumentList getDocuments(List<String> ids) throws SolrServerException, IOException {
        return client.getById(collection, ids);
    }

    private boolean exists(String id) throws SolrServerException, IOException {
        return getDocument(id) != null;
    }

    private UpdateResponse add(String id, String title, String authors, String fileUrl, List<String> categories, String content)
        throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField("id", id);
        //        inputDoc.addField("attr_pdf_docinfo_title", Arrays.asList(title));
        inputDoc.addField("attr_author", Arrays.asList(authors));
        inputDoc.addField("attr_fileurl", Arrays.asList(fileUrl));
        //        if (categories != null) inputDoc.addField("categories", StringUtils.join(categories, ","));
        inputDoc.addField("attr_content", Arrays.asList(content));

        UpdateResponse response = client.add(collection, inputDoc);

        if (++count % reloadRate == 0) {
            CollectionAdminRequest.reloadCollection(collection).process(client);
        }
        return response;
    }

    private UpdateResponse addItems(List<Map<String, Object>> baseValues) throws SolrServerException, IOException {
        List<SolrInputDocument> inputDocs = new ArrayList<>();
        for (int i = 0; i < baseValues.size(); i++) {
            SolrInputDocument inputDoc = new SolrInputDocument();
            String id = Integer.toHexString(baseValues.get(i).hashCode());
            inputDoc.addField("id", id);
            //if (exists(collection, id)) {
            //    log.error("ID {} already exists in Solr collection", id);
            //    return null;
            //}
            for (String key : baseValues.get(i).keySet()) {
                inputDoc.addField(key, baseValues.get(i).get(key));
            }
            inputDocs.add(inputDoc);
        }
        UpdateResponse response = client.add(collection, inputDocs);
        client.commit(collection);
        return response;
    }

    private UpdateResponse addItem(Map<String, Object> baseValues, Map<String, Object> appendValues)
        throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        String id = Integer.toHexString(baseValues.hashCode());
        inputDoc.addField("id", id);
        if (exists(id)) {
            log.error("ID {} already exists in Solr collection", id);
            return null;
        }
        for (String key : baseValues.keySet()) {
            inputDoc.addField(key, baseValues.get(key));
        }
        if (appendValues != null) for (String key : appendValues.keySet()) {
            inputDoc.addField(key, appendValues.get(key));
        }
        UpdateResponse response = client.add(collection, inputDoc);
        if (++count % reloadRate == 0) client.commit(collection);
        return response;
    }

    private UpdateResponse addItem(Map<String, Object> baseValues) throws SolrServerException, IOException {
        return addItem(baseValues, null);
    }

    private void commit() throws SolrServerException, IOException {
        client.commit(this.collection);
    }

    private void commit(String collection) throws SolrServerException, IOException {
        client.commit(collection);
    }

    public UpdateResponse add(String id, Map<String, List<String>> properties, Map<String, List<String>> append)
        throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        inputDoc.addField("id", id);
        if (exists(id)) {
            log.error("ID {} already exists in Solr collection", id);
            return null;
        }
        for (String key : properties.keySet()) {
            inputDoc.addField(key, properties.get(key));
        }
        for (String key : append.keySet()) {
            inputDoc.addField(key, append.get(key));
        }
        UpdateResponse response = client.add(collection, inputDoc);
        client.commit(collection);
        //        if (++count % reloadRate == 0) client.commit(collection);

        return response;
    }

    private UpdateResponse update(Map<String, List<String>> properties, Map<String, List<String>> append)
        throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        String pmid = properties.get("pmid").get(0);
        String id = Integer.toHexString(properties.hashCode()) + (pmid.length() > 3 ? pmid.substring(pmid.length() - 3) : pmid);
        inputDoc.addField("id", id);
        for (String key : properties.keySet()) {
            inputDoc.addField(key, properties.get(key));
        }
        for (String key : append.keySet()) {
            inputDoc.addField(key, append.get(key));
        }
        UpdateResponse response = client.add(collection, inputDoc);
        if (++count % reloadRate == 0) client.commit(collection);

        return response;
    }

    private UpdateResponse addUpdateMany(
        List<String> id,
        List<String> title,
        List<String> authors,
        List<String> fileUrl,
        List<List<String>> categories,
        List<String> content
    ) throws SolrServerException, IOException {
        List<SolrInputDocument> inputDocuments = new ArrayList<>();
        for (int i = 0; i < id.size(); i++) {
            SolrInputDocument inputDoc = new SolrInputDocument();
            inputDoc.addField("id", id.get(i));
            inputDoc.addField("attr_pdf_docinfo_title", Arrays.asList(title.get(i)));
            inputDoc.addField("attr_author", Arrays.asList(authors.get(i)));
            inputDoc.addField("attr_fileurl", Arrays.asList(fileUrl.get(i)));
            if (categories != null) inputDoc.addField("categories", StringUtils.join(categories.get(i), ","));
            inputDoc.addField("attr_content", Arrays.asList(content.get(i)));
            inputDocuments.add(inputDoc);
        }
        UpdateResponse response = client.add(collection, inputDocuments);
        client.commit(collection);
        return response;
    }

    private UpdateResponse addUpdateMany(List<Map<String, List<String>>> properties, List<Map<String, List<String>>> append)
        throws SolrServerException, IOException {
        // sanity check, properties and appended items should be the same length!
        if (properties.size() != append.size()) return null;
        List<SolrInputDocument> inputDocs = new ArrayList<>();
        for (int i = 0; i < properties.size(); i++) {
            Map<String, List<String>> map = properties.get(i);
            SolrInputDocument inputDoc = new SolrInputDocument();
            String pmid = map.get("pmid").get(0);
            String id = Integer.toHexString(map.hashCode()) + (pmid.length() > 3 ? pmid.substring(pmid.length() - 3) : pmid);
            inputDoc.addField("id", id);
            for (String key : map.keySet()) {
                inputDoc.addField(key, map.get(key));
            }
            for (String key : append.get(i).keySet()) {
                inputDoc.addField(key, append.get(i).get(key));
            }
            inputDocs.add(inputDoc);
        }
        UpdateResponse response = client.add(collection, inputDocs);
        client.commit(collection);

        return response;
    }

    private UpdateResponse addUpdateDeleteMany(
        List<Map<String, List<String>>> properties,
        List<Map<String, List<String>>> append,
        List<String> newIds,
        List<String> deleteIds
    ) throws SolrServerException, IOException {
        // sanity check, properties and appended items should be the same length!
        if (properties.size() != append.size()) return null;
        List<SolrInputDocument> inputDocs = new ArrayList<>();
        for (int i = 0; i < properties.size(); i++) {
            Map<String, List<String>> map = properties.get(i);
            SolrInputDocument inputDoc = new SolrInputDocument();
            inputDoc.addField("id", newIds.get(i));
            for (String key : map.keySet()) {
                inputDoc.addField(key, map.get(key));
            }
            for (String key : append.get(i).keySet()) {
                inputDoc.addField(key, append.get(i).get(key));
            }
            inputDocs.add(inputDoc);
        }
        if (deleteIds.size() > 0) client.deleteById(collection, deleteIds);
        UpdateResponse response = client.add(collection, inputDocs);
        client.commit(collection);
        return response;
    }

    private UpdateResponse add(Map<String, List<String>> properties) throws SolrServerException, IOException {
        SolrInputDocument inputDoc = new SolrInputDocument();
        String pmid = properties.get("pmid").get(0);
        String id = Integer.toHexString(properties.hashCode()) + (pmid.length() > 3 ? pmid.substring(pmid.length() - 3) : pmid);
        inputDoc.addField("id", id);
        if (exists(id)) return null;
        for (String key : properties.keySet()) {
            inputDoc.addField(key, properties.get(key));
        }
        UpdateResponse response = client.add(collection, inputDoc);
        if (++count % reloadRate == 0) CollectionAdminRequest.reloadCollection(collection).process(client);
        return response;
    }

    // ???
    private NamedList<Object> extract(String id, String name, String URL, List<String> categories, File file, String contentType)
        throws SolrServerException, IOException {
        ContentStreamUpdateRequest update = new ContentStreamUpdateRequest("/$core/update/extract");
        String fullUrl = file.toURI().toURL().toExternalForm();
        if (URL != null && !URL.equals("")) fullUrl = URL;
        update.addFile(file, contentType);
        update.setParam("id", id);
        update.setParam("literal.id", id);
        update.setParam("name", name);
        update.setParam("literal.name", name);
        update.setParam("fileUrl", fullUrl);
        update.setParam("literal.fileUrl", fullUrl);
        update.setParam("categories", StringUtils.join(categories, ","));
        update.setParam("literal.categories", StringUtils.join(categories, ","));
        update.setParam("uprefix", "attr_");
        update.setParam("fmap.content", "attr_content");
        update.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        if (++count % reloadRate == 0) CollectionAdminRequest.reloadCollection(collection).process(client);
        return client.request(update);
    }

    private void refreshCollection(String collection) {
        try {
            log.info("Attempting to reload the Solr collection");
            CollectionAdminRequest.reloadCollection(collection).process(client);
        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
        }
    }

    private UpdateResponse deleteMany(List<String> deleteIds) throws SolrServerException, IOException {
        if (deleteIds != null && deleteIds.size() > 0) return client.deleteById(collection, deleteIds);
        return null;
    }

    private UpdateResponse deleteAll() throws SolrServerException, IOException {
        return client.deleteByQuery("*:*");
    }

    private UpdateResponse delete(String id) throws SolrServerException, IOException {
        UpdateResponse response = client.deleteById(collection, id);
        if (++count % reloadRate == 0) client.commit(collection);

        return response;
    }

    public UpdateResponse delete(String id, boolean commit) throws SolrServerException, IOException {
        UpdateResponse response = client.deleteById(collection, id);
        if (commit) client.commit(collection);
        return response;
    }

    private String getDefaultField() {
        return defaultField;
    }

    private void setDefaultField(String defaultField) {
        this.defaultField = defaultField;
    }

    private int getReloadRate() {
        return reloadRate;
    }

    private void setReloadRate(int reloadRate) {
        this.reloadRate = reloadRate;
    }

    private String getParser() {
        return parser;
    }

    private void setParser(String parser) {
        this.parser = parser;
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (
                c == '\\' ||
                c == '+' ||
                c == '-' ||
                c == '!' ||
                c == '(' ||
                c == ')' ||
                c == ':' ||
                c == '^' ||
                c == '[' ||
                c == ']' ||
                c == '\"' ||
                c == '{' ||
                c == '}' ||
                c == '~' ||
                c == '*' ||
                c == '?' ||
                c == '|' ||
                c == '&' ||
                c == ';' ||
                c == '/' ||
                Character.isWhitespace(c)
            ) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    static class SolrPreemptiveAuthInterceptor implements HttpRequestInterceptor {

        //final static Logger log = LoggerFactory.getLogger(SolrPreemptiveAuthInterceptor.class);

        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
            // If no auth scheme available yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                //log.info("No AuthState: set Basic Auth");

                HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());

                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);

                Credentials creds = credsProvider.getCredentials(authScope);
                if (creds == null) {
                    //log.info("No Basic Auth credentials: add them");
                    creds = getCredentials(authScope);
                    credsProvider.setCredentials(authScope, creds);
                    context.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);
                }
                authState.update(new BasicScheme(), creds);
            }
        }

        private Credentials getCredentials(AuthScope authScope) {
            String user = System.getenv("solrUsername");
            String password = System.getenv("solrPassword");
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(authScope, credentials);
            //log.info("Creating Basic Auth credentials for user {}", user);

            return credentialsProvider.getCredentials(authScope);
        }
    }
}
