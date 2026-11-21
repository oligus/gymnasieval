package se.gymnasieval.workspace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import se.gymnasieval.TestcontainersConfiguration;
import se.gymnasieval.workspace.WorkspaceService.CreatedWorkspace;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, WorkspaceService.class})
class WorkspaceServiceTest {

    @Autowired
    private WorkspaceService service;

    @Autowired
    private TestEntityManager em;

    @Test
    void createReturnsTwoDifferentKeys() {
        CreatedWorkspace created = service.create();

        assertThat(created.id()).isNotNull();
        assertThat(created.editKey()).isNotEqualTo(created.viewKey());
    }

    @Test
    void storesOnlyHashesNeverPlainKeys() {
        CreatedWorkspace created = service.create();
        em.flush();

        String storedEditHash = (String) em.getEntityManager()
                .createNativeQuery("SELECT edit_key_hash FROM workspace WHERE id = :id")
                .setParameter("id", created.id())
                .getSingleResult();

        assertThat(storedEditHash).isEqualTo(WorkspaceKey.hash(created.editKey())).hasSize(64);
        assertThat(storedEditHash).doesNotContain(created.editKey());
    }

    @Test
    void editKeyGivesEditAccess() {
        CreatedWorkspace created = service.create();

        assertThat(service.resolve(created.editKey())).hasValueSatisfying(access -> {
            assertThat(access.workspace().getId()).isEqualTo(created.id());
            assertThat(access.level()).isEqualTo(AccessLevel.EDIT);
            assertThat(access.canEdit()).isTrue();
        });
    }

    @Test
    void viewKeyGivesReadOnlyAccess() {
        CreatedWorkspace created = service.create();

        assertThat(service.resolve(created.viewKey())).hasValueSatisfying(access -> {
            assertThat(access.workspace().getId()).isEqualTo(created.id());
            assertThat(access.level()).isEqualTo(AccessLevel.VIEW);
            assertThat(access.canEdit()).isFalse();
        });
    }

    @Test
    void unknownOrMalformedKeysGiveNoAccess() {
        service.create();

        assertThat(service.resolve(WorkspaceKey.generate())).isEmpty();
        assertThat(service.resolve("inte-en-nyckel")).isEmpty();
        assertThat(service.resolve(null)).isEmpty();
    }
}
