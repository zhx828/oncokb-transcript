package org.mskcc.oncokb.curation.service;

import java.util.List;
import java.util.Optional;
import org.mskcc.oncokb.curation.domain.ReferenceGenome;
import org.mskcc.oncokb.curation.domain.enumeration.EnsemblReferenceGenome;
import org.mskcc.oncokb.curation.repository.ReferenceGenomeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ReferenceGenome}.
 */
@Service
@Transactional
public class ReferenceGenomeService {

    private final Logger log = LoggerFactory.getLogger(ReferenceGenomeService.class);

    private final ReferenceGenomeRepository referenceGenomeRepository;

    public ReferenceGenomeService(ReferenceGenomeRepository referenceGenomeRepository) {
        this.referenceGenomeRepository = referenceGenomeRepository;
    }

    /**
     * Save a referenceGenome.
     *
     * @param referenceGenome the entity to save.
     * @return the persisted entity.
     */
    public ReferenceGenome save(ReferenceGenome referenceGenome) {
        log.debug("Request to save ReferenceGenome : {}", referenceGenome);
        return referenceGenomeRepository.save(referenceGenome);
    }

    /**
     * Partially update a referenceGenome.
     *
     * @param referenceGenome the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReferenceGenome> partialUpdate(ReferenceGenome referenceGenome) {
        log.debug("Request to partially update ReferenceGenome : {}", referenceGenome);

        return referenceGenomeRepository
            .findById(referenceGenome.getId())
            .map(existingReferenceGenome -> {
                if (referenceGenome.getVersion() != null) {
                    existingReferenceGenome.setVersion(referenceGenome.getVersion());
                }

                return existingReferenceGenome;
            })
            .map(referenceGenomeRepository::save);
    }

    /**
     * Get all the referenceGenomes.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ReferenceGenome> findAll() {
        log.debug("Request to get all ReferenceGenomes");
        return referenceGenomeRepository.findAll();
    }

    /**
     * Get one referenceGenome by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReferenceGenome> findOne(Long id) {
        log.debug("Request to get ReferenceGenome : {}", id);
        return referenceGenomeRepository.findById(id);
    }

    /**
     * Get one referenceGenome by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReferenceGenome> findOneByVersion(EnsemblReferenceGenome referenceGenome) {
        log.debug("Request to get ReferenceGenome by version : {}", referenceGenome);
        return referenceGenomeRepository.findByVersion(referenceGenome);
    }

    /**
     * Delete the referenceGenome by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ReferenceGenome : {}", id);
        referenceGenomeRepository.deleteById(id);
    }
}
