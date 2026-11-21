package se.gymnasieval.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SchoolProgramRepository extends JpaRepository<SchoolProgram, SchoolProgramId> {

    /**
     * Alla program för flera skolor i en fråga, med program och inriktningar ifyllda.
     * Används av GraphQL för att undvika en fråga per skola (N+1).
     */
    @Query("""
            select distinct sp from SchoolProgram sp
            join fetch sp.program
            left join fetch sp.studyPaths
            where sp.id.schoolUnitCode in :schoolUnitCodes
            order by sp.program.name
            """)
    List<SchoolProgram> findWithDetailsBySchoolUnitCodes(@Param("schoolUnitCodes") Collection<String> schoolUnitCodes);
}
