package se.gymnasieval.workspace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import se.gymnasieval.TestcontainersConfiguration;
import se.gymnasieval.school.OrganizerType;
import se.gymnasieval.school.SchoolStatus;
import se.gymnasieval.skolverket.SkolverketImporter;
import se.gymnasieval.skolverket.SkolverketSnapshot;
import se.gymnasieval.skolverket.SkolverketSnapshot.MunicipalityData;
import se.gymnasieval.skolverket.SkolverketSnapshot.ProgramData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolData;
import se.gymnasieval.skolverket.SkolverketSnapshot.SchoolProgramData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Arbetsytor genom hela kedjan: HTTP med headern X-Workspace-Key → GraphQL → MySQL.
 * Utan @Transactional, så att lat laddning utanför transaktion upptäcks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureHttpGraphQlTester
@Import(TestcontainersConfiguration.class)
class WorkspaceGraphQlTest {

    private static final String WORKSPACE_FIELDS = """
            id access viewKey watchedSchools { school { code name municipality { name } } program { code } }
            """;

    @Autowired
    private HttpGraphQlTester graphQl;

    @Autowired
    private SkolverketImporter importer;

    @BeforeEach
    void importTestData() {
        importer.importSnapshot(new SkolverketSnapshot(Instant.now(),
                List.of(new MunicipalityData("0114", "Upplands Väsby")),
                List.of(new ProgramData("TE25", "Teknikprogrammet"), new ProgramData("NA25", "Naturvetenskapsprogrammet")),
                List.of(school("44444444", "Alfa gymnasium"), school("55555555", "Beta gymnasium"))));
    }

    @Test
    void createWatchAndUnwatch() {
        Map<String, String> keys = createWorkspace();
        HttpGraphQlTester edit = withKey(keys.get("editKey"));

        edit.document("mutation { watchSchool(school: \"55555555\", program: \"TE25\") { id } }").execute();
        edit.document("mutation { watchSchool(school: \"44444444\", program: \"TE25\") { id } }").execute();

        edit.document("{ workspace { " + WORKSPACE_FIELDS + " } }")
                .execute()
                .path("workspace.access").entity(String.class).isEqualTo("EDIT")
                .path("workspace.watchedSchools[*].school.name").entityList(String.class)
                .containsExactly("Alfa gymnasium", "Beta gymnasium")
                .path("workspace.watchedSchools[0].school.municipality.name").entity(String.class)
                .isEqualTo("Upplands Väsby");

        edit.document("mutation { unwatchSchool(school: \"44444444\") { watchedSchools { school { code } } } }")
                .execute()
                .path("unwatchSchool.watchedSchools[*].school.code").entityList(String.class)
                .containsExactly("55555555");
    }

    @Test
    void watchingTwiceOnlyChangesProgram() {
        HttpGraphQlTester edit = withKey(createWorkspace().get("editKey"));

        edit.document("mutation { watchSchool(school: \"44444444\", program: \"TE25\") { id } }").execute();
        edit.document("mutation { watchSchool(school: \"44444444\", program: \"NA25\") { watchedSchools { program { code } } } }")
                .execute()
                .path("watchSchool.watchedSchools[*].program.code").entityList(String.class).containsExactly("NA25");
    }

    @Test
    void viewKeyCanReadButNotWrite() {
        Map<String, String> keys = createWorkspace();
        withKey(keys.get("editKey"))
                .document("mutation { watchSchool(school: \"44444444\", program: \"TE25\") { id } }").execute();
        HttpGraphQlTester view = withKey(keys.get("viewKey"));

        view.document("{ workspace { " + WORKSPACE_FIELDS + " } }")
                .execute()
                .path("workspace.access").entity(String.class).isEqualTo("VIEW")
                .path("workspace.viewKey").valueIsNull()
                .path("workspace.watchedSchools").entityList(Object.class).hasSize(1);

        view.document("mutation { watchSchool(school: \"55555555\", program: \"TE25\") { id } }")
                .execute()
                .errors().expect(e -> e.getErrorType() == ErrorType.FORBIDDEN);
    }

    @Test
    void editKeyRevealsTheDerivedViewKey() {
        Map<String, String> keys = createWorkspace();

        withKey(keys.get("editKey")).document("{ workspace { viewKey } }")
                .execute()
                .path("workspace.viewKey").entity(String.class).isEqualTo(keys.get("viewKey"));
    }

    @Test
    void unknownKeyGivesNoWorkspaceAndCannotWrite() {
        HttpGraphQlTester unknown = withKey(WorkspaceKey.generate());

        unknown.document("{ workspace { id } }").execute().path("workspace").valueIsNull();
        unknown.document("mutation { watchSchool(school: \"44444444\", program: \"TE25\") { id } }")
                .execute()
                .errors().expect(e -> e.getErrorType() == ErrorType.UNAUTHORIZED);
    }

    @Test
    void unknownSchoolIsBadRequest() {
        withKey(createWorkspace().get("editKey"))
                .document("mutation { watchSchool(school: \"00000000\", program: \"TE25\") { id } }")
                .execute()
                .errors().expect(e -> e.getErrorType() == ErrorType.BAD_REQUEST);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> createWorkspace() {
        Map<String, Object> created = graphQl.document("mutation { createWorkspace { editKey viewKey workspace { id access } } }")
                .execute()
                .path("createWorkspace.workspace.access").entity(String.class).isEqualTo("EDIT")
                .path("createWorkspace").entity(Map.class).get();
        assertThat(created.get("editKey")).isNotEqualTo(created.get("viewKey"));
        return Map.of("editKey", (String) created.get("editKey"), "viewKey", (String) created.get("viewKey"));
    }

    private HttpGraphQlTester withKey(String key) {
        return graphQl.mutate().header("X-Workspace-Key", key).build();
    }

    private static SchoolData school(String code, String name) {
        return new SchoolData(code, name, null, OrganizerType.KOMMUNAL, "0114", null, null, null, null, null, null,
                SchoolStatus.AKTIV, List.of(new SchoolProgramData("TE25", new BigDecimal("220.0"), null, (short) 2026,
                null, List.of())));
    }
}
