package se.gymnasieval.workspace;

/** Kastas när en ändring görs utan nyckel, med okänd nyckel eller med en läsnyckel. */
public class WorkspaceAccessDeniedException extends RuntimeException {

    private final boolean readOnly;

    private WorkspaceAccessDeniedException(String message, boolean readOnly) {
        super(message);
        this.readOnly = readOnly;
    }

    static WorkspaceAccessDeniedException missingKey() {
        return new WorkspaceAccessDeniedException("Ingen giltig nyckel till arbetsytan.", false);
    }

    static WorkspaceAccessDeniedException readOnly() {
        return new WorkspaceAccessDeniedException("Läslänken kan inte användas för att ändra.", true);
    }

    /** true = giltig läsnyckel (FORBIDDEN), false = ingen eller okänd nyckel (UNAUTHORIZED). */
    public boolean isReadOnly() {
        return readOnly;
    }
}
