package se.gymnasieval.workspace;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaces;

    WorkspaceService(WorkspaceRepository workspaces) {
        this.workspaces = workspaces;
    }

    /**
     * Nyckelparet i klartext. Returneras bara när arbetsytan skapas; därefter finns
     * nycklarna bara hos användaren (i länken), aldrig i databasen.
     */
    public record CreatedWorkspace(Long id, String editKey, String viewKey) {}

    @Transactional
    public CreatedWorkspace create() {
        String editKey = WorkspaceKey.generate();
        String viewKey = WorkspaceKey.generate();
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
        byte[] hash = WorkspaceKey.hash(key);
        Optional<WorkspaceAccess> access = workspaces.findByEditKeyHash(hash)
                .map(workspace -> new WorkspaceAccess(workspace, AccessLevel.EDIT))
                .or(() -> workspaces.findByViewKeyHash(hash)
                        .map(workspace -> new WorkspaceAccess(workspace, AccessLevel.VIEW)));
        access.ifPresent(a -> a.workspace().markUsed(Instant.now()));
        return access;
    }
}
