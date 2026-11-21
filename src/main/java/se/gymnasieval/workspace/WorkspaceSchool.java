package se.gymnasieval.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import se.gymnasieval.school.Program;
import se.gymnasieval.school.School;

import java.util.Objects;

/** En skola som bevakas i en arbetsyta, för ett visst program. */
@Entity
@Table(name = "workspace_school")
public class WorkspaceSchool {

    @EmbeddedId
    private WorkspaceSchoolId id;

    @MapsId("workspaceId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @MapsId("schoolUnitCode")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_unit_code")
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_code", nullable = false)
    private Program program;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private WatchStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    protected WorkspaceSchool() {
    }

    WorkspaceSchool(Workspace workspace, School school, Program program) {
        this.id = new WorkspaceSchoolId(workspace.getId(), school.getSchoolUnitCode());
        this.workspace = workspace;
        this.school = school;
        this.program = program;
    }

    public WorkspaceSchoolId getId() {
        return id;
    }

    public School getSchool() {
        return school;
    }

    public Program getProgram() {
        return program;
    }

    void setProgram(Program program) {
        this.program = program;
    }

    public WatchStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WorkspaceSchool other && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
