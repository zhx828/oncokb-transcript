package org.mskcc.oncokb.curation.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.mskcc.oncokb.curation.domain.ReferenceGenome;
import org.mskcc.oncokb.curation.repository.ReferenceGenomeRepository;
import org.mskcc.oncokb.curation.service.ReferenceGenomeService;
import org.mskcc.oncokb.curation.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link org.mskcc.oncokb.curation.domain.ReferenceGenome}.
 */
@RestController
@RequestMapping("/api")
public class ReferenceGenomeResource {

    private final Logger log = LoggerFactory.getLogger(ReferenceGenomeResource.class);

    private static final String ENTITY_NAME = "referenceGenome";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReferenceGenomeService referenceGenomeService;

    private final ReferenceGenomeRepository referenceGenomeRepository;

    public ReferenceGenomeResource(ReferenceGenomeService referenceGenomeService, ReferenceGenomeRepository referenceGenomeRepository) {
        this.referenceGenomeService = referenceGenomeService;
        this.referenceGenomeRepository = referenceGenomeRepository;
    }

    /**
     * {@code POST  /reference-genomes} : Create a new referenceGenome.
     *
     * @param referenceGenome the referenceGenome to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new referenceGenome, or with status {@code 400 (Bad Request)} if the referenceGenome has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/reference-genomes")
    public ResponseEntity<ReferenceGenome> createReferenceGenome(@Valid @RequestBody ReferenceGenome referenceGenome)
        throws URISyntaxException {
        log.debug("REST request to save ReferenceGenome : {}", referenceGenome);
        if (referenceGenome.getId() != null) {
            throw new BadRequestAlertException("A new referenceGenome cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ReferenceGenome result = referenceGenomeService.save(referenceGenome);
        return ResponseEntity
            .created(new URI("/api/reference-genomes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /reference-genomes/:id} : Updates an existing referenceGenome.
     *
     * @param id the id of the referenceGenome to save.
     * @param referenceGenome the referenceGenome to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated referenceGenome,
     * or with status {@code 400 (Bad Request)} if the referenceGenome is not valid,
     * or with status {@code 500 (Internal Server Error)} if the referenceGenome couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/reference-genomes/{id}")
    public ResponseEntity<ReferenceGenome> updateReferenceGenome(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReferenceGenome referenceGenome
    ) throws URISyntaxException {
        log.debug("REST request to update ReferenceGenome : {}, {}", id, referenceGenome);
        if (referenceGenome.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, referenceGenome.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!referenceGenomeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ReferenceGenome result = referenceGenomeService.save(referenceGenome);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, referenceGenome.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /reference-genomes/:id} : Partial updates given fields of an existing referenceGenome, field will ignore if it is null
     *
     * @param id the id of the referenceGenome to save.
     * @param referenceGenome the referenceGenome to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated referenceGenome,
     * or with status {@code 400 (Bad Request)} if the referenceGenome is not valid,
     * or with status {@code 404 (Not Found)} if the referenceGenome is not found,
     * or with status {@code 500 (Internal Server Error)} if the referenceGenome couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/reference-genomes/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReferenceGenome> partialUpdateReferenceGenome(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReferenceGenome referenceGenome
    ) throws URISyntaxException {
        log.debug("REST request to partial update ReferenceGenome partially : {}, {}", id, referenceGenome);
        if (referenceGenome.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, referenceGenome.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!referenceGenomeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReferenceGenome> result = referenceGenomeService.partialUpdate(referenceGenome);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, referenceGenome.getId().toString())
        );
    }

    /**
     * {@code GET  /reference-genomes} : get all the referenceGenomes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of referenceGenomes in body.
     */
    @GetMapping("/reference-genomes")
    public List<ReferenceGenome> getAllReferenceGenomes() {
        log.debug("REST request to get all ReferenceGenomes");
        return referenceGenomeService.findAll();
    }

    /**
     * {@code GET  /reference-genomes/:id} : get the "id" referenceGenome.
     *
     * @param id the id of the referenceGenome to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the referenceGenome, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/reference-genomes/{id}")
    public ResponseEntity<ReferenceGenome> getReferenceGenome(@PathVariable Long id) {
        log.debug("REST request to get ReferenceGenome : {}", id);
        Optional<ReferenceGenome> referenceGenome = referenceGenomeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(referenceGenome);
    }

    /**
     * {@code DELETE  /reference-genomes/:id} : delete the "id" referenceGenome.
     *
     * @param id the id of the referenceGenome to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/reference-genomes/{id}")
    public ResponseEntity<Void> deleteReferenceGenome(@PathVariable Long id) {
        log.debug("REST request to delete ReferenceGenome : {}", id);
        referenceGenomeService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
