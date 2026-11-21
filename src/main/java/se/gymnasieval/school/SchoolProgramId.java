package se.gymnasieval.school;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Sammansatt nyckel för {@link SchoolProgram}: (skolenhetskod, programkod).
 * En record ger equals/hashCode automatiskt, vilket JPA kräver av nyckelklasser.
 */
@Embeddable
public record SchoolProgramId(
        @Column(length = 8) String schoolUnitCode,
        @Column(length = 10) String programCode) {
}
