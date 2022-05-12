package org.mskcc.oncokb.curation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mskcc.oncokb.curation.web.rest.TestUtil;

class ReferenceGenomeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReferenceGenome.class);
        ReferenceGenome referenceGenome1 = new ReferenceGenome();
        referenceGenome1.setId(1L);
        ReferenceGenome referenceGenome2 = new ReferenceGenome();
        referenceGenome2.setId(referenceGenome1.getId());
        assertThat(referenceGenome1).isEqualTo(referenceGenome2);
        referenceGenome2.setId(2L);
        assertThat(referenceGenome1).isNotEqualTo(referenceGenome2);
        referenceGenome1.setId(null);
        assertThat(referenceGenome1).isNotEqualTo(referenceGenome2);
    }
}
