package org.mskcc.oncokb.transcript.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A Site.
 */
@Entity
@Table(name = "site")
public class Site implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name = "";

    @NotNull
    @Column(name = "city", nullable = false)
    private String city = "";

    @NotNull
    @Column(name = "country", nullable = false)
    private String country = "";

    @NotNull
    @Column(name = "state", nullable = false)
    private String state = "";

    @NotNull
    @Column(name = "address", nullable = false)
    private String address = "";

    @NotNull
    @Column(name = "coordinates", nullable = false)
    private String coordinates = "";

    @Lob
    @Column(name = "google_map_result", nullable = false)
    private String googleMapResult = "";

    @OneToMany(mappedBy = "site")
    @JsonIgnoreProperties(value = { "site" }, allowSetters = true)
    private Set<Aact> aacts = new HashSet<>();

    @ManyToMany(mappedBy = "sites")
    @JsonIgnoreProperties(value = { "sites", "arms" }, allowSetters = true)
    private Set<ClinicalTrial> clinicalTrials = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Site id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Site name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return this.city;
    }

    public Site city(String city) {
        this.city = city;
        return this;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return this.country;
    }

    public Site country(String country) {
        this.country = country;
        return this;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return this.state;
    }

    public Site state(String state) {
        this.state = state;
        return this;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAddress() {
        return this.address;
    }

    public Site address(String address) {
        this.address = address;
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCoordinates() {
        return this.coordinates;
    }

    public Site coordinates(String coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getGoogleMapResult() {
        return this.googleMapResult;
    }

    public Site googleMapResult(String googleMapResult) {
        this.googleMapResult = googleMapResult;
        return this;
    }

    public void setGoogleMapResult(String googleMapResult) {
        this.googleMapResult = googleMapResult;
    }

    public Set<Aact> getAacts() {
        return this.aacts;
    }

    public Site aacts(Set<Aact> aacts) {
        this.setAacts(aacts);
        return this;
    }

    public Site addAact(Aact aact) {
        this.aacts.add(aact);
        aact.setSite(this);
        return this;
    }

    public Site removeAact(Aact aact) {
        this.aacts.remove(aact);
        aact.setSite(null);
        return this;
    }

    public void setAacts(Set<Aact> aacts) {
        if (this.aacts != null) {
            this.aacts.forEach(i -> i.setSite(null));
        }
        if (aacts != null) {
            aacts.forEach(i -> i.setSite(this));
        }
        this.aacts = aacts;
    }

    public Set<ClinicalTrial> getClinicalTrials() {
        return this.clinicalTrials;
    }

    public Site clinicalTrials(Set<ClinicalTrial> clinicalTrials) {
        this.setClinicalTrials(clinicalTrials);
        return this;
    }

    public Site addClinicalTrial(ClinicalTrial clinicalTrial) {
        this.clinicalTrials.add(clinicalTrial);
        clinicalTrial.getSites().add(this);
        return this;
    }

    public Site removeClinicalTrial(ClinicalTrial clinicalTrial) {
        this.clinicalTrials.remove(clinicalTrial);
        clinicalTrial.getSites().remove(this);
        return this;
    }

    public void setClinicalTrials(Set<ClinicalTrial> clinicalTrials) {
        if (this.clinicalTrials != null) {
            this.clinicalTrials.forEach(i -> i.removeSite(this));
        }
        if (clinicalTrials != null) {
            clinicalTrials.forEach(i -> i.addSite(this));
        }
        this.clinicalTrials = clinicalTrials;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Site)) {
            return false;
        }
        return id != null && id.equals(((Site) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Site{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", city='" + getCity() + "'" +
            ", country='" + getCountry() + "'" +
            ", state='" + getState() + "'" +
            ", address='" + getAddress() + "'" +
            ", coordinates='" + getCoordinates() + "'" +
            ", googleMapResult='" + getGoogleMapResult() + "'" +
            "}";
    }
}
