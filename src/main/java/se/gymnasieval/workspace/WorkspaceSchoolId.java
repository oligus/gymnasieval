package se.gymnasieval.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Sammansatt nyckel för {@link WorkspaceSchool}: (arbetsyta, skolenhetskod). */
@Embeddable
public record WorkspaceSchoolId(
        Long workspaceId,
        @Column(length = 8) String schoolUnitCode) {
}
