package org.mskcc.oncokb.transcript.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A Drug.
 */
@Entity
@Table(name = "drug")
public class Drug implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Lob
    @Column(name = "semantic_type")
    private String semanticType;

    @OneToMany(mappedBy = "drug", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = { "drug" }, allowSetters = true)
    private Set<DrugSynonym> synonyms = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "rel_drug__arm", joinColumns = @JoinColumn(name = "drug_id"), inverseJoinColumns = @JoinColumn(name = "arm_id"))
    @JsonIgnoreProperties(value = { "clinicalTrials", "drugs" }, allowSetters = true)
    private Set<Arm> arms = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Drug id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Drug name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public Drug code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSemanticType() {
        return this.semanticType;
    }

    public Drug semanticType(String semanticType) {
        this.semanticType = semanticType;
        return this;
    }

    public void setSemanticType(String semanticType) {
        this.semanticType = semanticType;
    }

    public Set<DrugSynonym> getSynonyms() {
        return this.synonyms;
    }

    public Drug synonyms(Set<DrugSynonym> drugSynonyms) {
        this.setSynonyms(drugSynonyms);
        return this;
    }

    public Drug addSynonyms(DrugSynonym drugSynonym) {
        this.synonyms.add(drugSynonym);
        drugSynonym.setDrug(this);
        return this;
    }

    public Drug removeSynonyms(DrugSynonym drugSynonym) {
        this.synonyms.remove(drugSynonym);
        drugSynonym.setDrug(null);
        return this;
    }

    public void setSynonyms(Set<DrugSynonym> drugSynonyms) {
        if (this.synonyms != null) {
            this.synonyms.forEach(i -> i.setDrug(null));
        }
        if (drugSynonyms != null) {
            drugSynonyms.forEach(i -> i.setDrug(this));
        }
        this.synonyms = drugSynonyms;
    }

    public Set<Arm> getArms() {
        return this.arms;
    }

    public Drug arms(Set<Arm> arms) {
        this.setArms(arms);
        return this;
    }

    public Drug addArm(Arm arm) {
        this.arms.add(arm);
        arm.getDrugs().add(this);
        return this;
    }

    public Drug removeArm(Arm arm) {
        this.arms.remove(arm);
        arm.getDrugs().remove(this);
        return this;
    }

    public void setArms(Set<Arm> arms) {
        this.arms = arms;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Drug)) {
            return false;
        }
        return id != null && id.equals(((Drug) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Drug{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", code='" + getCode() + "'" +
            ", semanticType='" + getSemanticType() + "'" +
            "}";
    }
}
