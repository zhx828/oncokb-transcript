package org.mskcc.oncokb.transcript.repository;

import java.util.List;
import org.mskcc.oncokb.transcript.domain.Aact;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Aact entity.
 */
@Repository
public interface AactRepository extends JpaRepository<Aact, Long> {
    List<Aact> findAllByNameAndCityAndStateAndCountry(String name, String city, String state, String country);
}
