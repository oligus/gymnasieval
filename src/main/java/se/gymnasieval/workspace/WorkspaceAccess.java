package se.gymnasieval.workspace;

/** Resultatet av att slå upp en nyckel: vilken arbetsyta, och med vilken behörighet. */
public record WorkspaceAccess(Workspace workspace, AccessLevel level) {

    public boolean canEdit() {
        return level == AccessLevel.EDIT;
    }
}
