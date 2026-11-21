package se.gymnasieval.school;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * En inriktning inom ett program på en skola, t.ex. "TEINF" = Informations- och medieteknik.
 * Är ett värdeobjekt (ingen egen identitet) och ägs helt av sitt {@link SchoolProgram}.
 */
@Embeddable
public record StudyPath(
        @Column(nullable = false, length = 20) String code,
        @Column(nullable = false, length = 200) String name) {
}
