package org.mskcc.oncokb.transcript.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mskcc.oncokb.transcript.web.rest.TestUtil;

class AactTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Aact.class);
        Aact aact1 = new Aact();
        aact1.setId(1L);
        Aact aact2 = new Aact();
        aact2.setId(aact1.getId());
        assertThat(aact1).isEqualTo(aact2);
        aact2.setId(2L);
        assertThat(aact1).isNotEqualTo(aact2);
        aact1.setId(null);
        assertThat(aact1).isNotEqualTo(aact2);
    }
}
