package se.gymnasieval.workspace;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.gymnasieval.school.Program;
import se.gymnasieval.school.ProgramRepository;
import se.gymnasieval.school.School;
import se.gymnasieval.school.SchoolRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class WorkspaceService {

    /** Skydd mot missbruk: en arbetsyta kan inte växa obegränsat. */
    static final int MAX_WATCHED_SCHOOLS = 200;

    /** lastUsedAt uppdateras högst så här ofta, så att varje läsning inte blir en skrivning. */
    private static final Duration LAST_USED_RESOLUTION = Duration.ofHours(1);

    private final WorkspaceRepository workspaces;
    private final WorkspaceSchoolRepository watched;
    private final SchoolRepository schools;
    private final ProgramRepository programs;

    WorkspaceService(WorkspaceRepository workspaces, WorkspaceSchoolRepository watched,
                     SchoolRepository schools, ProgramRepository programs) {
        this.workspaces = workspaces;
        this.watched = watched;
        this.schools = schools;
        this.programs = programs;
    }

    /**
     * Nyckelparet i klartext. Returneras bara när arbetsytan skapas; därefter finns
     * nycklarna bara hos användaren (i länken), aldrig i databasen.
     */
    public record CreatedWorkspace(Long id, String editKey, String viewKey) {}

    @Transactional
    public CreatedWorkspace create() {
        String editKey = WorkspaceKey.generate();
        String viewKey = WorkspaceKey.deriveViewKey(editKey);
        Workspace workspace = workspaces.save(
                new Workspace(WorkspaceKey.hash(editKey), WorkspaceKey.hash(viewKey), Instant.now()));
        return new CreatedWorkspace(workspace.getId(), editKey, viewKey);
    }

    /** Slår upp en nyckel. Tomt resultat om nyckeln är okänd eller har fel format. */
    @Transactional
    public Optional<WorkspaceAccess> resolve(String key) {
        if (!WorkspaceKey.isValidFormat(key)) {
            return Optional.empty();
        }
        String hash = WorkspaceKey.hash(key);
        Optional<WorkspaceAccess> access = workspaces.findByEditKeyHash(hash)
                .map(workspace -> new WorkspaceAccess(workspace, AccessLevel.EDIT))
                .or(() -> workspaces.findByViewKeyHash(hash)
                        .map(workspace -> new WorkspaceAccess(workspace, AccessLevel.VIEW)));
        Instant now = Instant.now();
        access.map(WorkspaceAccess::workspace)
                .filter(w -> w.getLastUsedAt().isBefore(now.minus(LAST_USED_RESOLUTION)))
                .ifPresent(w -> w.markUsed(now));
        return access;
    }

    /** Som {@link #resolve}, men kräver redigeringsnyckeln. */
    @Transactional
    public WorkspaceAccess requireEdit(String key) {
        WorkspaceAccess access = resolve(key).orElseThrow(WorkspaceAccessDeniedException::missingKey);
        if (!access.canEdit()) {
            throw WorkspaceAccessDeniedException.readOnly();
        }
        return access;
    }

    /** Läsnyckeln, men bara för den som har redigeringsnyckeln. */
    public Optional<String> viewKeyFor(String key, WorkspaceAccess access) {
        return access.canEdit() ? Optional.of(WorkspaceKey.deriveViewKey(key)) : Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<WorkspaceSchool> watchedSchools(Long workspaceId) {
        return watched.findWatched(workspaceId);
    }

    /** Bevakar en skola. Är den redan bevakad byts bara programmet. */
    @Transactional
    public void watch(String key, String schoolUnitCode, String programCode) {
        Long workspaceId = requireEdit(key).workspace().getId();
        School school = schools.findById(schoolUnitCode)
                .orElseThrow(() -> new IllegalArgumentException("Okänd skola: " + schoolUnitCode));
        Program program = programs.findById(programCode)
                .orElseThrow(() -> new IllegalArgumentException("Okänt program: " + programCode));

        Optional<WorkspaceSchool> existing = watched.findById(new WorkspaceSchoolId(workspaceId, schoolUnitCode));
        if (existing.isPresent()) {
            existing.get().setProgram(program);
            return;
        }
        if (watched.countByIdWorkspaceId(workspaceId) >= MAX_WATCHED_SCHOOLS) {
            throw new IllegalArgumentException("Högst " + MAX_WATCHED_SCHOOLS + " skolor kan bevakas.");
        }
        watched.save(new WorkspaceSchool(workspaces.getReferenceById(workspaceId), school, program));
    }

    @Transactional
    public void unwatch(String key, String schoolUnitCode) {
        Long workspaceId = requireEdit(key).workspace().getId();
        watched.deleteById(new WorkspaceSchoolId(workspaceId, schoolUnitCode));
    }
}
