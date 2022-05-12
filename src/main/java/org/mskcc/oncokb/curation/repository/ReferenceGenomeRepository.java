package org.mskcc.oncokb.curation.repository;

import java.util.Optional;
import org.mskcc.oncokb.curation.domain.ReferenceGenome;
import org.mskcc.oncokb.curation.domain.enumeration.EnsemblReferenceGenome;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the ReferenceGenome entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReferenceGenomeRepository extends JpaRepository<ReferenceGenome, Long> {
    Optional<ReferenceGenome> findByVersion(EnsemblReferenceGenome version);
}
