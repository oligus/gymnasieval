package se.gymnasieval.workspace;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.gymnasieval.school.Program;
import se.gymnasieval.school.ProgramRepository;
import se.gymnasieval.school.School;
import se.gymnasieval.school.SchoolRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkspaceService {

    /** Skydd mot missbruk: en arbetsyta kan inte växa obegränsat. */
    static final int MAX_WATCHED_SCHOOLS = 200;
    static final int MAX_NOTE_LENGTH = 5000;
    static final double MAX_MERIT = 340;

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

    /** En bevakad skola som GraphQL visar den, med totalpoäng uträknad. */
    public record WatchedSchoolView(School school, Program program, WatchStatus status, String note,
                                    List<Score> scores, Double totalScore, int missingScores) {}

    public record Score(Criterion criterion, int score) {}

    public record Weight(Criterion criterion, int weight) {}

    @Transactional(readOnly = true)
    public List<WatchedSchoolView> watchedSchools(Long workspaceId) {
        Map<Criterion, Integer> weights = workspaces.getReferenceById(workspaceId).getWeights();
        return watched.findWatched(workspaceId).stream()
                .map(ws -> {
                    TotalScore total = TotalScore.of(ws.getScores(), weights);
                    List<Score> scores = ws.getScores().entrySet().stream()
                            .map(e -> new Score(e.getKey(), e.getValue()))
                            .sorted(Comparator.comparing(Score::criterion))
                            .toList();
                    return new WatchedSchoolView(ws.getSchool(), ws.getProgram(), ws.getStatus(), ws.getNote(),
                            scores, total.value(), total.missing());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Weight> weights(Long workspaceId) {
        return workspaces.getReferenceById(workspaceId).getWeights().entrySet().stream()
                .map(e -> new Weight(e.getKey(), e.getValue()))
                .toList();
    }

    @Transactional
    public void setMerit(String key, Double merit) {
        if (merit != null && (merit < 0 || merit > MAX_MERIT)) {
            throw new IllegalArgumentException("Meritvärdet ska vara mellan 0 och " + MAX_MERIT + ".");
        }
        requireEdit(key).workspace().setMeritValue(merit == null ? null : BigDecimal.valueOf(merit).setScale(1, RoundingMode.HALF_UP));
    }

    @Transactional
    public void setWeight(String key, Criterion criterion, int weight) {
        if (weight < Criterion.MIN_WEIGHT || weight > Criterion.MAX_WEIGHT) {
            throw new IllegalArgumentException("Vikten ska vara mellan 0 och 5.");
        }
        requireEdit(key).workspace().setWeight(criterion, weight);
    }

    /** Sätter ett betyg 1–5, eller tar bort det med null. */
    @Transactional
    public void setScore(String key, String schoolUnitCode, Criterion criterion, Integer score) {
        if (score != null && (score < Criterion.MIN_SCORE || score > Criterion.MAX_SCORE)) {
            throw new IllegalArgumentException("Betyget ska vara mellan 1 och 5.");
        }
        requireWatched(key, schoolUnitCode).setScore(criterion, score);
    }

    @Transactional
    public void setStatus(String key, String schoolUnitCode, WatchStatus status) {
        requireWatched(key, schoolUnitCode).setStatus(status);
    }

    @Transactional
    public void setNote(String key, String schoolUnitCode, String note) {
        if (note != null && note.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("Anteckningen får vara högst " + MAX_NOTE_LENGTH + " tecken.");
        }
        requireWatched(key, schoolUnitCode).setNote(note == null || note.isBlank() ? null : note);
    }

    private WorkspaceSchool requireWatched(String key, String schoolUnitCode) {
        Long workspaceId = requireEdit(key).workspace().getId();
        return watched.findById(new WorkspaceSchoolId(workspaceId, schoolUnitCode))
                .orElseThrow(() -> new IllegalArgumentException("Skolan " + schoolUnitCode + " är inte bevakad."));
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
