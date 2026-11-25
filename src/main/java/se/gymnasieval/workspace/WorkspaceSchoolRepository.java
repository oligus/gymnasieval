package se.gymnasieval.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface WorkspaceSchoolRepository extends JpaRepository<WorkspaceSchool, WorkspaceSchoolId> {

    /** Bevakade skolor med skola, kommun, program och betyg ifyllda, så att GraphQL kan läsa dem utan transaktion. */
    @Query("""
            select ws from WorkspaceSchool ws
            join fetch ws.school s
            left join fetch s.municipality
            join fetch ws.program
            left join fetch ws.scores
            where ws.id.workspaceId = :workspaceId
            order by s.name
            """)
    List<WorkspaceSchool> findWatched(@Param("workspaceId") Long workspaceId);

    long countByIdWorkspaceId(Long workspaceId);
}
