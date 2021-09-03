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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mskcc.oncokb.transcript.IntegrationTest;
import org.mskcc.oncokb.transcript.domain.Aact;
import org.mskcc.oncokb.transcript.repository.AactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AactResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AactResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_COUNTRY = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY = "BBBBBBBBBB";

    private static final String DEFAULT_STATE = "AAAAAAAAAA";
    private static final String UPDATED_STATE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/aacts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AactRepository aactRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAactMockMvc;

    private Aact aact;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Aact createEntity(EntityManager em) {
        Aact aact = new Aact().name(DEFAULT_NAME).city(DEFAULT_CITY).country(DEFAULT_COUNTRY).state(DEFAULT_STATE);
        return aact;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Aact createUpdatedEntity(EntityManager em) {
        Aact aact = new Aact().name(UPDATED_NAME).city(UPDATED_CITY).country(UPDATED_COUNTRY).state(UPDATED_STATE);
        return aact;
    }

    @BeforeEach
    public void initTest() {
        aact = createEntity(em);
    }

    @Test
    @Transactional
    void createAact() throws Exception {
        int databaseSizeBeforeCreate = aactRepository.findAll().size();
        // Create the Aact
        restAactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isCreated());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeCreate + 1);
        Aact testAact = aactList.get(aactList.size() - 1);
        assertThat(testAact.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAact.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testAact.getCountry()).isEqualTo(DEFAULT_COUNTRY);
        assertThat(testAact.getState()).isEqualTo(DEFAULT_STATE);
    }

    @Test
    @Transactional
    void createAactWithExistingId() throws Exception {
        // Create the Aact with an existing ID
        aact.setId(1L);

        int databaseSizeBeforeCreate = aactRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isBadRequest());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = aactRepository.findAll().size();
        // set the field null
        aact.setName(null);

        // Create the Aact, which fails.

        restAactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isBadRequest());

        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCityIsRequired() throws Exception {
        int databaseSizeBeforeTest = aactRepository.findAll().size();
        // set the field null
        aact.setCity(null);

        // Create the Aact, which fails.

        restAactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isBadRequest());

        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCountryIsRequired() throws Exception {
        int databaseSizeBeforeTest = aactRepository.findAll().size();
        // set the field null
        aact.setCountry(null);

        // Create the Aact, which fails.

        restAactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isBadRequest());

        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStateIsRequired() throws Exception {
        int databaseSizeBeforeTest = aactRepository.findAll().size();
        // set the field null
        aact.setState(null);

        // Create the Aact, which fails.

        restAactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isBadRequest());

        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAacts() throws Exception {
        // Initialize the database
        aactRepository.saveAndFlush(aact);

        // Get all the aactList
        restAactMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(aact.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].country").value(hasItem(DEFAULT_COUNTRY)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)));
    }

    @Test
    @Transactional
    void getAact() throws Exception {
        // Initialize the database
        aactRepository.saveAndFlush(aact);

        // Get the aact
        restAactMockMvc
            .perform(get(ENTITY_API_URL_ID, aact.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(aact.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY))
            .andExpect(jsonPath("$.country").value(DEFAULT_COUNTRY))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE));
    }

    @Test
    @Transactional
    void getNonExistingAact() throws Exception {
        // Get the aact
        restAactMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAact() throws Exception {
        // Initialize the database
        aactRepository.saveAndFlush(aact);

        int databaseSizeBeforeUpdate = aactRepository.findAll().size();

        // Update the aact
        Aact updatedAact = aactRepository.findById(aact.getId()).get();
        // Disconnect from session so that the updates on updatedAact are not directly saved in db
        em.detach(updatedAact);
        updatedAact.name(UPDATED_NAME).city(UPDATED_CITY).country(UPDATED_COUNTRY).state(UPDATED_STATE);

        restAactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAact.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAact))
            )
            .andExpect(status().isOk());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
        Aact testAact = aactList.get(aactList.size() - 1);
        assertThat(testAact.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAact.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testAact.getCountry()).isEqualTo(UPDATED_COUNTRY);
        assertThat(testAact.getState()).isEqualTo(UPDATED_STATE);
    }

    @Test
    @Transactional
    void putNonExistingAact() throws Exception {
        int databaseSizeBeforeUpdate = aactRepository.findAll().size();
        aact.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, aact.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(aact))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAact() throws Exception {
        int databaseSizeBeforeUpdate = aactRepository.findAll().size();
        aact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(aact))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAact() throws Exception {
        int databaseSizeBeforeUpdate = aactRepository.findAll().size();
        aact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAactMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAactWithPatch() throws Exception {
        // Initialize the database
        aactRepository.saveAndFlush(aact);

        int databaseSizeBeforeUpdate = aactRepository.findAll().size();

        // Update the aact using partial update
        Aact partialUpdatedAact = new Aact();
        partialUpdatedAact.setId(aact.getId());

        partialUpdatedAact.name(UPDATED_NAME);

        restAactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAact))
            )
            .andExpect(status().isOk());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
        Aact testAact = aactList.get(aactList.size() - 1);
        assertThat(testAact.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAact.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testAact.getCountry()).isEqualTo(DEFAULT_COUNTRY);
        assertThat(testAact.getState()).isEqualTo(DEFAULT_STATE);
    }

    @Test
    @Transactional
    void fullUpdateAactWithPatch() throws Exception {
        // Initialize the database
        aactRepository.saveAndFlush(aact);

        int databaseSizeBeforeUpdate = aactRepository.findAll().size();

        // Update the aact using partial update
        Aact partialUpdatedAact = new Aact();
        partialUpdatedAact.setId(aact.getId());

        partialUpdatedAact.name(UPDATED_NAME).city(UPDATED_CITY).country(UPDATED_COUNTRY).state(UPDATED_STATE);

        restAactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAact))
            )
            .andExpect(status().isOk());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
        Aact testAact = aactList.get(aactList.size() - 1);
        assertThat(testAact.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAact.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testAact.getCountry()).isEqualTo(UPDATED_COUNTRY);
        assertThat(testAact.getState()).isEqualTo(UPDATED_STATE);
    }

    @Test
    @Transactional
    void patchNonExistingAact() throws Exception {
        int databaseSizeBeforeUpdate = aactRepository.findAll().size();
        aact.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, aact.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(aact))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAact() throws Exception {
        int databaseSizeBeforeUpdate = aactRepository.findAll().size();
        aact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAactMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(aact))
            )
            .andExpect(status().isBadRequest());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAact() throws Exception {
        int databaseSizeBeforeUpdate = aactRepository.findAll().size();
        aact.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAactMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(aact)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Aact in the database
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAact() throws Exception {
        // Initialize the database
        aactRepository.saveAndFlush(aact);

        int databaseSizeBeforeDelete = aactRepository.findAll().size();

        // Delete the aact
        restAactMockMvc
            .perform(delete(ENTITY_API_URL_ID, aact.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Aact> aactList = aactRepository.findAll();
        assertThat(aactList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
