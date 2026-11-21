package se.gymnasieval.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data skapar frågorna utifrån metodnamnen (WHERE edit_key_hash = ?). */
interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByEditKeyHash(byte[] editKeyHash);

    Optional<Workspace> findByViewKeyHash(byte[] viewKeyHash);
}
