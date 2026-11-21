package se.gymnasieval.school;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Spring Data skapar implementationen automatiskt (som Doctrines EntityRepository). */
public interface SchoolRepository extends JpaRepository<School, String> {

    /**
     * Skolor i hela Sverige som erbjuder ett program.
     * Kommunen hämtas i samma fråga (join fetch), så att den kan läsas utan öppen transaktion.
     */
    @Query("""
            select s from School s
            left join fetch s.municipality
            where exists (
                select 1 from SchoolProgram sp
                where sp.school = s and sp.program.code = :program)
            order by s.name
            """)
    List<School> findByProgram(@Param("program") String program);

    /** Som {@link #findByProgram}, men bara i de angivna kommunerna. Listan får inte vara tom. */
    @Query("""
            select s from School s
            join fetch s.municipality m
            where exists (
                select 1 from SchoolProgram sp
                where sp.school = s and sp.program.code = :program)
            and m.code in :municipalities
            order by s.name
            """)
    List<School> findByProgramInMunicipalities(@Param("program") String program,
                                               @Param("municipalities") Collection<String> municipalities);

    /** Som findById, men med kommunen ifylld (@EntityGraph = join fetch utan egen JPQL). */
    @EntityGraph(attributePaths = "municipality")
    Optional<School> findWithMunicipalityBySchoolUnitCode(String schoolUnitCode);
}
