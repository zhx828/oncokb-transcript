package org.mskcc.oncokb.curation.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mskcc.oncokb.curation.IntegrationTest;
import org.mskcc.oncokb.curation.domain.ReferenceGenome;
import org.mskcc.oncokb.curation.domain.enumeration.EnsemblReferenceGenome;
import org.mskcc.oncokb.curation.repository.ReferenceGenomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ReferenceGenomeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ReferenceGenomeResourceIT {

    private static final EnsemblReferenceGenome DEFAULT_VERSION = EnsemblReferenceGenome.GRCh37;
    private static final EnsemblReferenceGenome UPDATED_VERSION = EnsemblReferenceGenome.GRCh38;

    private static final String ENTITY_API_URL = "/api/reference-genomes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ReferenceGenomeRepository referenceGenomeRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReferenceGenomeMockMvc;

    private ReferenceGenome referenceGenome;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReferenceGenome createEntity(EntityManager em) {
        ReferenceGenome referenceGenome = new ReferenceGenome().version(DEFAULT_VERSION);
        return referenceGenome;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReferenceGenome createUpdatedEntity(EntityManager em) {
        ReferenceGenome referenceGenome = new ReferenceGenome().version(UPDATED_VERSION);
        return referenceGenome;
    }

    @BeforeEach
    public void initTest() {
        referenceGenome = createEntity(em);
    }

    @Test
    @Transactional
    void createReferenceGenome() throws Exception {
        int databaseSizeBeforeCreate = referenceGenomeRepository.findAll().size();
        // Create the ReferenceGenome
        restReferenceGenomeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isCreated());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeCreate + 1);
        ReferenceGenome testReferenceGenome = referenceGenomeList.get(referenceGenomeList.size() - 1);
        assertThat(testReferenceGenome.getVersion()).isEqualTo(DEFAULT_VERSION);
    }

    @Test
    @Transactional
    void createReferenceGenomeWithExistingId() throws Exception {
        // Create the ReferenceGenome with an existing ID
        referenceGenome.setId(1L);

        int databaseSizeBeforeCreate = referenceGenomeRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReferenceGenomeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = referenceGenomeRepository.findAll().size();
        // set the field null
        referenceGenome.setVersion(null);

        // Create the ReferenceGenome, which fails.

        restReferenceGenomeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isBadRequest());

        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReferenceGenomes() throws Exception {
        // Initialize the database
        referenceGenomeRepository.saveAndFlush(referenceGenome);

        // Get all the referenceGenomeList
        restReferenceGenomeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(referenceGenome.getId().intValue())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION.toString())));
    }

    @Test
    @Transactional
    void getReferenceGenome() throws Exception {
        // Initialize the database
        referenceGenomeRepository.saveAndFlush(referenceGenome);

        // Get the referenceGenome
        restReferenceGenomeMockMvc
            .perform(get(ENTITY_API_URL_ID, referenceGenome.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(referenceGenome.getId().intValue()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION.toString()));
    }

    @Test
    @Transactional
    void getNonExistingReferenceGenome() throws Exception {
        // Get the referenceGenome
        restReferenceGenomeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewReferenceGenome() throws Exception {
        // Initialize the database
        referenceGenomeRepository.saveAndFlush(referenceGenome);

        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();

        // Update the referenceGenome
        ReferenceGenome updatedReferenceGenome = referenceGenomeRepository.findById(referenceGenome.getId()).get();
        // Disconnect from session so that the updates on updatedReferenceGenome are not directly saved in db
        em.detach(updatedReferenceGenome);
        updatedReferenceGenome.version(UPDATED_VERSION);

        restReferenceGenomeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedReferenceGenome.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedReferenceGenome))
            )
            .andExpect(status().isOk());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
        ReferenceGenome testReferenceGenome = referenceGenomeList.get(referenceGenomeList.size() - 1);
        assertThat(testReferenceGenome.getVersion()).isEqualTo(UPDATED_VERSION);
    }

    @Test
    @Transactional
    void putNonExistingReferenceGenome() throws Exception {
        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();
        referenceGenome.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReferenceGenomeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, referenceGenome.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReferenceGenome() throws Exception {
        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();
        referenceGenome.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferenceGenomeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReferenceGenome() throws Exception {
        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();
        referenceGenome.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferenceGenomeMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReferenceGenomeWithPatch() throws Exception {
        // Initialize the database
        referenceGenomeRepository.saveAndFlush(referenceGenome);

        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();

        // Update the referenceGenome using partial update
        ReferenceGenome partialUpdatedReferenceGenome = new ReferenceGenome();
        partialUpdatedReferenceGenome.setId(referenceGenome.getId());

        partialUpdatedReferenceGenome.version(UPDATED_VERSION);

        restReferenceGenomeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReferenceGenome.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReferenceGenome))
            )
            .andExpect(status().isOk());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
        ReferenceGenome testReferenceGenome = referenceGenomeList.get(referenceGenomeList.size() - 1);
        assertThat(testReferenceGenome.getVersion()).isEqualTo(UPDATED_VERSION);
    }

    @Test
    @Transactional
    void fullUpdateReferenceGenomeWithPatch() throws Exception {
        // Initialize the database
        referenceGenomeRepository.saveAndFlush(referenceGenome);

        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();

        // Update the referenceGenome using partial update
        ReferenceGenome partialUpdatedReferenceGenome = new ReferenceGenome();
        partialUpdatedReferenceGenome.setId(referenceGenome.getId());

        partialUpdatedReferenceGenome.version(UPDATED_VERSION);

        restReferenceGenomeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReferenceGenome.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedReferenceGenome))
            )
            .andExpect(status().isOk());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
        ReferenceGenome testReferenceGenome = referenceGenomeList.get(referenceGenomeList.size() - 1);
        assertThat(testReferenceGenome.getVersion()).isEqualTo(UPDATED_VERSION);
    }

    @Test
    @Transactional
    void patchNonExistingReferenceGenome() throws Exception {
        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();
        referenceGenome.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReferenceGenomeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, referenceGenome.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReferenceGenome() throws Exception {
        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();
        referenceGenome.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferenceGenomeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReferenceGenome() throws Exception {
        int databaseSizeBeforeUpdate = referenceGenomeRepository.findAll().size();
        referenceGenome.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReferenceGenomeMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(referenceGenome))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReferenceGenome in the database
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReferenceGenome() throws Exception {
        // Initialize the database
        referenceGenomeRepository.saveAndFlush(referenceGenome);

        int databaseSizeBeforeDelete = referenceGenomeRepository.findAll().size();

        // Delete the referenceGenome
        restReferenceGenomeMockMvc
            .perform(delete(ENTITY_API_URL_ID, referenceGenome.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ReferenceGenome> referenceGenomeList = referenceGenomeRepository.findAll();
        assertThat(referenceGenomeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
