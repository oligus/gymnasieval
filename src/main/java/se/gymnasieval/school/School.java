package se.gymnasieval.school;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * En gymnasieskola (skolenhet) enligt Skolverket.
 * Nyckeln är Skolverkets skolenhetskod, så samma skola får samma id vid varje import.
 */
@Entity
public class School {

    @Id
    @Column(length = 8)
    private String schoolUnitCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String organizerName;

    // Hibernate vill annars använda MySQL:s ENUM-typ; vi lagrar som VARCHAR.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private OrganizerType organizerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_code")
    private Municipality municipality;

    @Column(length = 200)
    private String street;

    @Column(length = 10)
    private String zipCode;

    @Column(length = 100)
    private String city;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(length = 500)
    private String webUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SchoolStatus status;

    @Column(nullable = false)
    private Instant importedAt;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchoolProgram> programs = new ArrayList<>();

    protected School() {
    }

    public School(String schoolUnitCode, String name, SchoolStatus status, Instant importedAt) {
        this.schoolUnitCode = schoolUnitCode;
        this.name = name;
        this.status = status;
        this.importedAt = importedAt;
    }

    /** Lägger till ett program och håller båda sidor av relationen i synk. */
    public SchoolProgram addProgram(Program program) {
        SchoolProgram schoolProgram = new SchoolProgram(this, program);
        programs.add(schoolProgram);
        return schoolProgram;
    }

    /** Returnerar skolans befintliga rad för programmet, eller lägger till en ny. */
    public SchoolProgram programFor(Program program) {
        return programs.stream()
                .filter(p -> p.getId().programCode().equals(program.getCode()))
                .findFirst()
                .orElseGet(() -> addProgram(program));
    }

    /** Tar bort program som inte finns bland {@code programCodes} (raderas tack vare orphanRemoval). */
    public void retainPrograms(Collection<String> programCodes) {
        programs.removeIf(p -> !programCodes.contains(p.getId().programCode()));
    }

    public List<SchoolProgram> getPrograms() {
        return Collections.unmodifiableList(programs);
    }

    public String getSchoolUnitCode() {
        return schoolUnitCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public OrganizerType getOrganizerType() {
        return organizerType;
    }

    public void setOrganizerType(OrganizerType organizerType) {
        this.organizerType = organizerType;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public SchoolStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolStatus status) {
        this.status = status;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(Instant importedAt) {
        this.importedAt = importedAt;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof School other && Objects.equals(schoolUnitCode, other.schoolUnitCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(schoolUnitCode);
    }
}
