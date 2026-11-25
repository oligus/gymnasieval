package se.gymnasieval.workspace;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import se.gymnasieval.workspace.WorkspaceService.CreatedWorkspace;
import se.gymnasieval.workspace.WorkspaceService.WatchedSchoolView;
import se.gymnasieval.workspace.WorkspaceService.Weight;

import java.math.BigDecimal;
import java.util.List;

import static se.gymnasieval.workspace.WorkspaceKeyInterceptor.CONTEXT_KEY;

/**
 * GraphQL för arbetsytor. Nyckeln kommer från headern X-Workspace-Key via
 * {@link WorkspaceKeyInterceptor}. Typen Workspace i schemat motsvaras av {@link WorkspaceAccess}.
 */
@Controller
class WorkspaceGraphQlController {

    private final WorkspaceService service;

    WorkspaceGraphQlController(WorkspaceService service) {
        this.service = service;
    }

    @QueryMapping
    WorkspaceAccess workspace(@ContextValue(name = CONTEXT_KEY, required = false) String key) {
        return service.resolve(key).orElse(null);
    }

    @MutationMapping
    CreatedWorkspace createWorkspace() {
        return service.create();
    }

    @MutationMapping
    WorkspaceAccess watchSchool(@ContextValue(name = CONTEXT_KEY, required = false) String key,
                                @Argument String school, @Argument String program) {
        service.watch(key, school, program);
        return service.requireEdit(key);
    }

    @MutationMapping
    WorkspaceAccess unwatchSchool(@ContextValue(name = CONTEXT_KEY, required = false) String key,
                                  @Argument String school) {
        service.unwatch(key, school);
        return service.requireEdit(key);
    }

    @MutationMapping
    WorkspaceAccess setMerit(@ContextValue(name = CONTEXT_KEY, required = false) String key, @Argument Double merit) {
        service.setMerit(key, merit);
        return service.requireEdit(key);
    }

    @MutationMapping
    WorkspaceAccess setWeight(@ContextValue(name = CONTEXT_KEY, required = false) String key,
                              @Argument Criterion criterion, @Argument int weight) {
        service.setWeight(key, criterion, weight);
        return service.requireEdit(key);
    }

    @MutationMapping
    WorkspaceAccess setScore(@ContextValue(name = CONTEXT_KEY, required = false) String key,
                             @Argument String school, @Argument Criterion criterion, @Argument Integer score) {
        service.setScore(key, school, criterion, score);
        return service.requireEdit(key);
    }

    @MutationMapping
    WorkspaceAccess setStatus(@ContextValue(name = CONTEXT_KEY, required = false) String key,
                              @Argument String school, @Argument WatchStatus status) {
        service.setStatus(key, school, status);
        return service.requireEdit(key);
    }

    @MutationMapping
    WorkspaceAccess setNote(@ContextValue(name = CONTEXT_KEY, required = false) String key,
                            @Argument String school, @Argument String note) {
        service.setNote(key, school, note);
        return service.requireEdit(key);
    }

    // ---- CreatedWorkspace ----

    @SchemaMapping(typeName = "CreatedWorkspace", field = "workspace")
    WorkspaceAccess createdWorkspace(CreatedWorkspace created) {
        return service.requireEdit(created.editKey());
    }

    // ---- Workspace (= WorkspaceAccess i Java) ----

    @SchemaMapping(typeName = "Workspace", field = "id")
    Long id(WorkspaceAccess access) {
        return access.workspace().getId();
    }

    @SchemaMapping(typeName = "Workspace", field = "access")
    AccessLevel access(WorkspaceAccess access) {
        return access.level();
    }

    @SchemaMapping(typeName = "Workspace", field = "viewKey")
    String viewKey(WorkspaceAccess access, @ContextValue(name = CONTEXT_KEY, required = false) String key) {
        // Vid createWorkspace finns ingen header än; då hämtar frontend läsnyckeln från CreatedWorkspace.
        return key == null ? null : service.viewKeyFor(key, access).orElse(null);
    }

    @SchemaMapping(typeName = "Workspace", field = "watchedSchools")
    List<WatchedSchoolView> watchedSchools(WorkspaceAccess access) {
        return service.watchedSchools(access.workspace().getId());
    }

    @SchemaMapping(typeName = "Workspace", field = "meritValue")
    BigDecimal meritValue(WorkspaceAccess access) {
        return access.workspace().getMeritValue();
    }

    @SchemaMapping(typeName = "Workspace", field = "weights")
    List<Weight> weights(WorkspaceAccess access) {
        return service.weights(access.workspace().getId());
    }
}
