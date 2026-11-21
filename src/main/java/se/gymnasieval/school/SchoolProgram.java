package se.gymnasieval.school;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Ett program som en viss skola erbjuder, med antagningspoäng och inriktningar. */
@Entity
public class SchoolProgram {

    @EmbeddedId
    private SchoolProgramId id;

    // @MapsId: kolumnen school_unit_code är både del av nyckeln och FK till school.
    @MapsId("schoolUnitCode")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_unit_code")
    private School school;

    @MapsId("programCode")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_code")
    private Program program;

    @Column(precision = 5, scale = 1)
    private BigDecimal admissionPointsMin;

    @Column(precision = 5, scale = 1)
    private BigDecimal admissionPointsAvg;

    private Short admissionYear;

    @Column(length = 100)
    private String admissionNote;

    // Inriktningarna sparas i tabellen study_path, men är värden i Java, inte egna entiteter.
    @ElementCollection
    @CollectionTable(name = "study_path", joinColumns = {
            @JoinColumn(name = "school_unit_code", referencedColumnName = "school_unit_code"),
            @JoinColumn(name = "program_code", referencedColumnName = "program_code")
    })
    private Set<StudyPath> studyPaths = new LinkedHashSet<>();

    protected SchoolProgram() {
    }

    /** Skapas via {@link School#addProgram(Program)}. */
    SchoolProgram(School school, Program program) {
        this.id = new SchoolProgramId(school.getSchoolUnitCode(), program.getCode());
        this.school = school;
        this.program = program;
    }

    public void setAdmission(BigDecimal min, BigDecimal avg, Short year, String note) {
        this.admissionPointsMin = min;
        this.admissionPointsAvg = avg;
        this.admissionYear = year;
        this.admissionNote = note;
    }

    public void addStudyPath(String code, String name) {
        studyPaths.add(new StudyPath(code, name));
    }

    /** Ersätter alla inriktningar. Rör inte databasen om inget har ändrats. */
    public void replaceStudyPaths(Set<StudyPath> newStudyPaths) {
        if (!studyPaths.equals(newStudyPaths)) {
            studyPaths.clear();
            studyPaths.addAll(newStudyPaths);
        }
    }

    public SchoolProgramId getId() {
        return id;
    }

    public School getSchool() {
        return school;
    }

    public Program getProgram() {
        return program;
    }

    public BigDecimal getAdmissionPointsMin() {
        return admissionPointsMin;
    }

    public BigDecimal getAdmissionPointsAvg() {
        return admissionPointsAvg;
    }

    public Short getAdmissionYear() {
        return admissionYear;
    }

    public String getAdmissionNote() {
        return admissionNote;
    }

    public Set<StudyPath> getStudyPaths() {
        return Collections.unmodifiableSet(studyPaths);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SchoolProgram other && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
