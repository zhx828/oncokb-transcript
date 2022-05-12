package org.mskcc.oncokb.curation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.mskcc.oncokb.curation.domain.enumeration.EnsemblReferenceGenome;

/**
 * A ReferenceGenome.
 */
@Entity
@Table(name = "reference_genome")
public class ReferenceGenome implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "version", nullable = false, unique = true)
    private EnsemblReferenceGenome version;

    @OneToMany(mappedBy = "referenceGenome")
    @JsonIgnoreProperties(value = { "transcripts", "referenceGenome", "gene" }, allowSetters = true)
    private Set<EnsemblGene> ensemblGenes = new HashSet<>();

    @ManyToMany(mappedBy = "referenceGenomes")
    @JsonIgnoreProperties(value = { "deviceUsageIndications", "genes", "referenceGenomes", "consequence" }, allowSetters = true)
    private Set<Alteration> alterations = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ReferenceGenome id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EnsemblReferenceGenome getVersion() {
        return this.version;
    }

    public ReferenceGenome version(EnsemblReferenceGenome version) {
        this.setVersion(version);
        return this;
    }

    public void setVersion(EnsemblReferenceGenome version) {
        this.version = version;
    }

    public Set<EnsemblGene> getEnsemblGenes() {
        return this.ensemblGenes;
    }

    public void setEnsemblGenes(Set<EnsemblGene> ensemblGenes) {
        if (this.ensemblGenes != null) {
            this.ensemblGenes.forEach(i -> i.setReferenceGenome(null));
        }
        if (ensemblGenes != null) {
            ensemblGenes.forEach(i -> i.setReferenceGenome(this));
        }
        this.ensemblGenes = ensemblGenes;
    }

    public ReferenceGenome ensemblGenes(Set<EnsemblGene> ensemblGenes) {
        this.setEnsemblGenes(ensemblGenes);
        return this;
    }

    public ReferenceGenome addEnsemblGene(EnsemblGene ensemblGene) {
        this.ensemblGenes.add(ensemblGene);
        ensemblGene.setReferenceGenome(this);
        return this;
    }

    public ReferenceGenome removeEnsemblGene(EnsemblGene ensemblGene) {
        this.ensemblGenes.remove(ensemblGene);
        ensemblGene.setReferenceGenome(null);
        return this;
    }

    public Set<Alteration> getAlterations() {
        return this.alterations;
    }

    public void setAlterations(Set<Alteration> alterations) {
        if (this.alterations != null) {
            this.alterations.forEach(i -> i.removeReferenceGenome(this));
        }
        if (alterations != null) {
            alterations.forEach(i -> i.addReferenceGenome(this));
        }
        this.alterations = alterations;
    }

    public ReferenceGenome alterations(Set<Alteration> alterations) {
        this.setAlterations(alterations);
        return this;
    }

    public ReferenceGenome addAlteration(Alteration alteration) {
        this.alterations.add(alteration);
        alteration.getReferenceGenomes().add(this);
        return this;
    }

    public ReferenceGenome removeAlteration(Alteration alteration) {
        this.alterations.remove(alteration);
        alteration.getReferenceGenomes().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReferenceGenome)) {
            return false;
        }
        return id != null && id.equals(((ReferenceGenome) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReferenceGenome{" +
            "id=" + getId() +
            ", version='" + getVersion() + "'" +
            "}";
    }
}
