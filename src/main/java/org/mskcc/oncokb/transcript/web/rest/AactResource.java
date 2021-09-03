package org.mskcc.oncokb.transcript.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.mskcc.oncokb.transcript.domain.Aact;
import org.mskcc.oncokb.transcript.repository.AactRepository;
import org.mskcc.oncokb.transcript.service.AactService;
import org.mskcc.oncokb.transcript.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link org.mskcc.oncokb.transcript.domain.Aact}.
 */
@RestController
@RequestMapping("/api")
public class AactResource {

    private final Logger log = LoggerFactory.getLogger(AactResource.class);

    private static final String ENTITY_NAME = "aact";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AactService aactService;

    private final AactRepository aactRepository;

    public AactResource(AactService aactService, AactRepository aactRepository) {
        this.aactService = aactService;
        this.aactRepository = aactRepository;
    }

    /**
     * {@code POST  /aacts} : Create a new aact.
     *
     * @param aact the aact to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new aact, or with status {@code 400 (Bad Request)} if the aact has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/aacts")
    public ResponseEntity<Aact> createAact(@Valid @RequestBody Aact aact) throws URISyntaxException {
        log.debug("REST request to save Aact : {}", aact);
        if (aact.getId() != null) {
            throw new BadRequestAlertException("A new aact cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Aact result = aactService.save(aact);
        return ResponseEntity
            .created(new URI("/api/aacts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /aacts/:id} : Updates an existing aact.
     *
     * @param id the id of the aact to save.
     * @param aact the aact to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated aact,
     * or with status {@code 400 (Bad Request)} if the aact is not valid,
     * or with status {@code 500 (Internal Server Error)} if the aact couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/aacts/{id}")
    public ResponseEntity<Aact> updateAact(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Aact aact)
        throws URISyntaxException {
        log.debug("REST request to update Aact : {}, {}", id, aact);
        if (aact.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, aact.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!aactRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Aact result = aactService.save(aact);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, aact.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /aacts/:id} : Partial updates given fields of an existing aact, field will ignore if it is null
     *
     * @param id the id of the aact to save.
     * @param aact the aact to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated aact,
     * or with status {@code 400 (Bad Request)} if the aact is not valid,
     * or with status {@code 404 (Not Found)} if the aact is not found,
     * or with status {@code 500 (Internal Server Error)} if the aact couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/aacts/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Aact> partialUpdateAact(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Aact aact
    ) throws URISyntaxException {
        log.debug("REST request to partial update Aact partially : {}, {}", id, aact);
        if (aact.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, aact.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!aactRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Aact> result = aactService.partialUpdate(aact);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, aact.getId().toString())
        );
    }

    /**
     * {@code GET  /aacts} : get all the aacts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of aacts in body.
     */
    @GetMapping("/aacts")
    public List<Aact> getAllAacts() {
        log.debug("REST request to get all Aacts");
        return aactService.findAll();
    }

    /**
     * {@code GET  /aacts/:id} : get the "id" aact.
     *
     * @param id the id of the aact to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the aact, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/aacts/{id}")
    public ResponseEntity<Aact> getAact(@PathVariable Long id) {
        log.debug("REST request to get Aact : {}", id);
        Optional<Aact> aact = aactService.findOne(id);
        return ResponseUtil.wrapOrNotFound(aact);
    }

    /**
     * {@code DELETE  /aacts/:id} : delete the "id" aact.
     *
     * @param id the id of the aact to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/aacts/{id}")
    public ResponseEntity<Void> deleteAact(@PathVariable Long id) {
        log.debug("REST request to delete Aact : {}", id);
        aactService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
