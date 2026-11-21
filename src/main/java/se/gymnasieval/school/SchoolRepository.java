package se.gymnasieval.school;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Spring Data skapar implementationen automatiskt (som Doctrines EntityRepository). */
public interface SchoolRepository extends JpaRepository<School, String> {

    /**
     * Skolor som erbjuder ett program, valfritt i en viss kommun.
     * Kommunen hämtas i samma fråga (join fetch), så att den kan läsas utan öppen transaktion.
     */
    @Query("""
            select s from School s
            left join fetch s.municipality m
            where exists (
                select 1 from SchoolProgram sp
                where sp.school = s and sp.program.code = :program)
            and (:municipality is null or m.code = :municipality)
            order by s.name
            """)
    List<School> search(@Param("program") String program, @Param("municipality") String municipality);

    /** Som findById, men med kommunen ifylld (@EntityGraph = join fetch utan egen JPQL). */
    @EntityGraph(attributePaths = "municipality")
    Optional<School> findWithMunicipalityBySchoolUnitCode(String schoolUnitCode);
}
