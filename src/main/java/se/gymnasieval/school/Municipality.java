package se.gymnasieval.school;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Objects;

/** En kommun, identifierad av SCB:s kommunkod (t.ex. "0114" = Upplands Väsby). */
@Entity
public class Municipality {

    @Id
    @Column(length = 4)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /** Krävs av JPA. Hibernate skapar objektet via denna och fyller sedan fälten. */
    protected Municipality() {
    }

    public Municipality(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Länet, härlett ur kommunkoden. Sparas inte i databasen. */
    public String getCounty() {
        return Counties.nameForMunicipality(code);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Municipality other && Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
