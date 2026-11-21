package se.gymnasieval.workspace;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceKeyTest {

    @Test
    void generatesUrlSafeKeysOf22Characters() {
        String key = WorkspaceKey.generate();

        assertThat(key).hasSize(22).matches("[A-Za-z0-9_-]+");
        assertThat(WorkspaceKey.isValidFormat(key)).isTrue();
    }

    @Test
    void generatesDifferentKeysEachTime() {
        assertThat(WorkspaceKey.generate()).isNotEqualTo(WorkspaceKey.generate());
    }

    @Test
    void hashIsHexAndDeterministic() {
        String key = WorkspaceKey.generate();

        assertThat(WorkspaceKey.hash(key)).hasSize(64).matches("[0-9a-f]{64}").isEqualTo(WorkspaceKey.hash(key));
    }

    @Test
    void viewKeyIsDerivedDeterministicallyFromEditKey() {
        String editKey = WorkspaceKey.generate();

        String viewKey = WorkspaceKey.deriveViewKey(editKey);

        assertThat(viewKey).isEqualTo(WorkspaceKey.deriveViewKey(editKey)).isNotEqualTo(editKey);
        assertThat(WorkspaceKey.isValidFormat(viewKey)).isTrue();
        assertThat(WorkspaceKey.deriveViewKey(WorkspaceKey.generate())).isNotEqualTo(viewKey);
    }

    @Test
    void rejectsMalformedKeys() {
        assertThat(WorkspaceKey.isValidFormat(null)).isFalse();
        assertThat(WorkspaceKey.isValidFormat("")).isFalse();
        assertThat(WorkspaceKey.isValidFormat("för-kort")).isFalse();
        assertThat(WorkspaceKey.isValidFormat("abc+def/ghi=jklmnopqrs")).isFalse(); // vanlig base64, inte url
    }
}
