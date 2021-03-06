package org.mskcc.oncokb.transcript.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mskcc.oncokb.transcript.IntegrationTest;
import org.mskcc.oncokb.transcript.domain.Transcript;
import org.mskcc.oncokb.transcript.domain.enumeration.ReferenceGenome;
import org.mskcc.oncokb.transcript.repository.TranscriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TranscriptResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TranscriptResourceIT {

    private static final Integer DEFAULT_ENTREZ_GENE_ID = 1;
    private static final Integer UPDATED_ENTREZ_GENE_ID = 2;

    private static final String DEFAULT_HUGO_SYMBOL = "AAAAAAAAAA";
    private static final String UPDATED_HUGO_SYMBOL = "BBBBBBBBBB";

    private static final ReferenceGenome DEFAULT_REFERENCE_GENOME = ReferenceGenome.GRCh37;
    private static final ReferenceGenome UPDATED_REFERENCE_GENOME = ReferenceGenome.GRCh38;

    private static final String DEFAULT_ENSEMBL_TRANSCRIPT_ID = "AAAAAAAAAA";
    private static final String UPDATED_ENSEMBL_TRANSCRIPT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_ENSEMBL_PROTEIN_ID = "AAAAAAAAAA";
    private static final String UPDATED_ENSEMBL_PROTEIN_ID = "BBBBBBBBBB";

    private static final String DEFAULT_REFERENCE_SEQUENCE_ID = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE_SEQUENCE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/transcripts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TranscriptRepository transcriptRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTranscriptMockMvc;

    private Transcript transcript;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transcript createEntity(EntityManager em) {
        Transcript transcript = new Transcript()
            .entrezGeneId(DEFAULT_ENTREZ_GENE_ID)
            .hugoSymbol(DEFAULT_HUGO_SYMBOL)
            .referenceGenome(DEFAULT_REFERENCE_GENOME)
            .ensemblTranscriptId(DEFAULT_ENSEMBL_TRANSCRIPT_ID)
            .ensemblProteinId(DEFAULT_ENSEMBL_PROTEIN_ID)
            .referenceSequenceId(DEFAULT_REFERENCE_SEQUENCE_ID)
            .description(DEFAULT_DESCRIPTION);
        return transcript;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transcript createUpdatedEntity(EntityManager em) {
        Transcript transcript = new Transcript()
            .entrezGeneId(UPDATED_ENTREZ_GENE_ID)
            .hugoSymbol(UPDATED_HUGO_SYMBOL)
            .referenceGenome(UPDATED_REFERENCE_GENOME)
            .ensemblTranscriptId(UPDATED_ENSEMBL_TRANSCRIPT_ID)
            .ensemblProteinId(UPDATED_ENSEMBL_PROTEIN_ID)
            .referenceSequenceId(UPDATED_REFERENCE_SEQUENCE_ID)
            .description(UPDATED_DESCRIPTION);
        return transcript;
    }

    @BeforeEach
    public void initTest() {
        transcript = createEntity(em);
    }

    @Test
    @Transactional
    void createTranscript() throws Exception {
        int databaseSizeBeforeCreate = transcriptRepository.findAll().size();
        // Create the Transcript
        restTranscriptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transcript)))
            .andExpect(status().isCreated());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeCreate + 1);
        Transcript testTranscript = transcriptList.get(transcriptList.size() - 1);
        assertThat(testTranscript.getEntrezGeneId()).isEqualTo(DEFAULT_ENTREZ_GENE_ID);
        assertThat(testTranscript.getHugoSymbol()).isEqualTo(DEFAULT_HUGO_SYMBOL);
        assertThat(testTranscript.getReferenceGenome()).isEqualTo(DEFAULT_REFERENCE_GENOME);
        assertThat(testTranscript.getEnsemblTranscriptId()).isEqualTo(DEFAULT_ENSEMBL_TRANSCRIPT_ID);
        assertThat(testTranscript.getEnsemblProteinId()).isEqualTo(DEFAULT_ENSEMBL_PROTEIN_ID);
        assertThat(testTranscript.getReferenceSequenceId()).isEqualTo(DEFAULT_REFERENCE_SEQUENCE_ID);
        assertThat(testTranscript.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void createTranscriptWithExistingId() throws Exception {
        // Create the Transcript with an existing ID
        transcript.setId(1L);

        int databaseSizeBeforeCreate = transcriptRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTranscriptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transcript)))
            .andExpect(status().isBadRequest());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEntrezGeneIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = transcriptRepository.findAll().size();
        // set the field null
        transcript.setEntrezGeneId(null);

        // Create the Transcript, which fails.

        restTranscriptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transcript)))
            .andExpect(status().isBadRequest());

        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkHugoSymbolIsRequired() throws Exception {
        int databaseSizeBeforeTest = transcriptRepository.findAll().size();
        // set the field null
        transcript.setHugoSymbol(null);

        // Create the Transcript, which fails.

        restTranscriptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transcript)))
            .andExpect(status().isBadRequest());

        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReferenceGenomeIsRequired() throws Exception {
        int databaseSizeBeforeTest = transcriptRepository.findAll().size();
        // set the field null
        transcript.setReferenceGenome(null);

        // Create the Transcript, which fails.

        restTranscriptMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transcript)))
            .andExpect(status().isBadRequest());

        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTranscripts() throws Exception {
        // Initialize the database
        transcriptRepository.saveAndFlush(transcript);

        // Get all the transcriptList
        restTranscriptMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transcript.getId().intValue())))
            .andExpect(jsonPath("$.[*].entrezGeneId").value(hasItem(DEFAULT_ENTREZ_GENE_ID)))
            .andExpect(jsonPath("$.[*].hugoSymbol").value(hasItem(DEFAULT_HUGO_SYMBOL)))
            .andExpect(jsonPath("$.[*].referenceGenome").value(hasItem(DEFAULT_REFERENCE_GENOME.toString())))
            .andExpect(jsonPath("$.[*].ensemblTranscriptId").value(hasItem(DEFAULT_ENSEMBL_TRANSCRIPT_ID)))
            .andExpect(jsonPath("$.[*].ensemblProteinId").value(hasItem(DEFAULT_ENSEMBL_PROTEIN_ID)))
            .andExpect(jsonPath("$.[*].referenceSequenceId").value(hasItem(DEFAULT_REFERENCE_SEQUENCE_ID)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getTranscript() throws Exception {
        // Initialize the database
        transcriptRepository.saveAndFlush(transcript);

        // Get the transcript
        restTranscriptMockMvc
            .perform(get(ENTITY_API_URL_ID, transcript.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transcript.getId().intValue()))
            .andExpect(jsonPath("$.entrezGeneId").value(DEFAULT_ENTREZ_GENE_ID))
            .andExpect(jsonPath("$.hugoSymbol").value(DEFAULT_HUGO_SYMBOL))
            .andExpect(jsonPath("$.referenceGenome").value(DEFAULT_REFERENCE_GENOME.toString()))
            .andExpect(jsonPath("$.ensemblTranscriptId").value(DEFAULT_ENSEMBL_TRANSCRIPT_ID))
            .andExpect(jsonPath("$.ensemblProteinId").value(DEFAULT_ENSEMBL_PROTEIN_ID))
            .andExpect(jsonPath("$.referenceSequenceId").value(DEFAULT_REFERENCE_SEQUENCE_ID))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingTranscript() throws Exception {
        // Get the transcript
        restTranscriptMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewTranscript() throws Exception {
        // Initialize the database
        transcriptRepository.saveAndFlush(transcript);

        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();

        // Update the transcript
        Transcript updatedTranscript = transcriptRepository.findById(transcript.getId()).get();
        // Disconnect from session so that the updates on updatedTranscript are not directly saved in db
        em.detach(updatedTranscript);
        updatedTranscript
            .entrezGeneId(UPDATED_ENTREZ_GENE_ID)
            .hugoSymbol(UPDATED_HUGO_SYMBOL)
            .referenceGenome(UPDATED_REFERENCE_GENOME)
            .ensemblTranscriptId(UPDATED_ENSEMBL_TRANSCRIPT_ID)
            .ensemblProteinId(UPDATED_ENSEMBL_PROTEIN_ID)
            .referenceSequenceId(UPDATED_REFERENCE_SEQUENCE_ID)
            .description(UPDATED_DESCRIPTION);

        restTranscriptMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTranscript.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTranscript))
            )
            .andExpect(status().isOk());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
        Transcript testTranscript = transcriptList.get(transcriptList.size() - 1);
        assertThat(testTranscript.getEntrezGeneId()).isEqualTo(UPDATED_ENTREZ_GENE_ID);
        assertThat(testTranscript.getHugoSymbol()).isEqualTo(UPDATED_HUGO_SYMBOL);
        assertThat(testTranscript.getReferenceGenome()).isEqualTo(UPDATED_REFERENCE_GENOME);
        assertThat(testTranscript.getEnsemblTranscriptId()).isEqualTo(UPDATED_ENSEMBL_TRANSCRIPT_ID);
        assertThat(testTranscript.getEnsemblProteinId()).isEqualTo(UPDATED_ENSEMBL_PROTEIN_ID);
        assertThat(testTranscript.getReferenceSequenceId()).isEqualTo(UPDATED_REFERENCE_SEQUENCE_ID);
        assertThat(testTranscript.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void putNonExistingTranscript() throws Exception {
        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();
        transcript.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTranscriptMockMvc
            .perform(
                put(ENTITY_API_URL_ID, transcript.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transcript))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTranscript() throws Exception {
        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();
        transcript.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTranscriptMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transcript))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTranscript() throws Exception {
        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();
        transcript.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTranscriptMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transcript)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTranscriptWithPatch() throws Exception {
        // Initialize the database
        transcriptRepository.saveAndFlush(transcript);

        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();

        // Update the transcript using partial update
        Transcript partialUpdatedTranscript = new Transcript();
        partialUpdatedTranscript.setId(transcript.getId());

        partialUpdatedTranscript
            .entrezGeneId(UPDATED_ENTREZ_GENE_ID)
            .hugoSymbol(UPDATED_HUGO_SYMBOL)
            .referenceGenome(UPDATED_REFERENCE_GENOME)
            .ensemblTranscriptId(UPDATED_ENSEMBL_TRANSCRIPT_ID)
            .description(UPDATED_DESCRIPTION);

        restTranscriptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTranscript.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTranscript))
            )
            .andExpect(status().isOk());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
        Transcript testTranscript = transcriptList.get(transcriptList.size() - 1);
        assertThat(testTranscript.getEntrezGeneId()).isEqualTo(UPDATED_ENTREZ_GENE_ID);
        assertThat(testTranscript.getHugoSymbol()).isEqualTo(UPDATED_HUGO_SYMBOL);
        assertThat(testTranscript.getReferenceGenome()).isEqualTo(UPDATED_REFERENCE_GENOME);
        assertThat(testTranscript.getEnsemblTranscriptId()).isEqualTo(UPDATED_ENSEMBL_TRANSCRIPT_ID);
        assertThat(testTranscript.getEnsemblProteinId()).isEqualTo(DEFAULT_ENSEMBL_PROTEIN_ID);
        assertThat(testTranscript.getReferenceSequenceId()).isEqualTo(DEFAULT_REFERENCE_SEQUENCE_ID);
        assertThat(testTranscript.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void fullUpdateTranscriptWithPatch() throws Exception {
        // Initialize the database
        transcriptRepository.saveAndFlush(transcript);

        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();

        // Update the transcript using partial update
        Transcript partialUpdatedTranscript = new Transcript();
        partialUpdatedTranscript.setId(transcript.getId());

        partialUpdatedTranscript
            .entrezGeneId(UPDATED_ENTREZ_GENE_ID)
            .hugoSymbol(UPDATED_HUGO_SYMBOL)
            .referenceGenome(UPDATED_REFERENCE_GENOME)
            .ensemblTranscriptId(UPDATED_ENSEMBL_TRANSCRIPT_ID)
            .ensemblProteinId(UPDATED_ENSEMBL_PROTEIN_ID)
            .referenceSequenceId(UPDATED_REFERENCE_SEQUENCE_ID)
            .description(UPDATED_DESCRIPTION);

        restTranscriptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTranscript.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTranscript))
            )
            .andExpect(status().isOk());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
        Transcript testTranscript = transcriptList.get(transcriptList.size() - 1);
        assertThat(testTranscript.getEntrezGeneId()).isEqualTo(UPDATED_ENTREZ_GENE_ID);
        assertThat(testTranscript.getHugoSymbol()).isEqualTo(UPDATED_HUGO_SYMBOL);
        assertThat(testTranscript.getReferenceGenome()).isEqualTo(UPDATED_REFERENCE_GENOME);
        assertThat(testTranscript.getEnsemblTranscriptId()).isEqualTo(UPDATED_ENSEMBL_TRANSCRIPT_ID);
        assertThat(testTranscript.getEnsemblProteinId()).isEqualTo(UPDATED_ENSEMBL_PROTEIN_ID);
        assertThat(testTranscript.getReferenceSequenceId()).isEqualTo(UPDATED_REFERENCE_SEQUENCE_ID);
        assertThat(testTranscript.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void patchNonExistingTranscript() throws Exception {
        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();
        transcript.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTranscriptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, transcript.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transcript))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTranscript() throws Exception {
        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();
        transcript.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTranscriptMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(transcript))
            )
            .andExpect(status().isBadRequest());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTranscript() throws Exception {
        int databaseSizeBeforeUpdate = transcriptRepository.findAll().size();
        transcript.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTranscriptMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(transcript))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Transcript in the database
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTranscript() throws Exception {
        // Initialize the database
        transcriptRepository.saveAndFlush(transcript);

        int databaseSizeBeforeDelete = transcriptRepository.findAll().size();

        // Delete the transcript
        restTranscriptMockMvc
            .perform(delete(ENTITY_API_URL_ID, transcript.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Transcript> transcriptList = transcriptRepository.findAll();
        assertThat(transcriptList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
