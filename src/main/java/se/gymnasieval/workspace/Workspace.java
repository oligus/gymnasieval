package se.gymnasieval.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * En arbetsyta: all data för en person eller familj, utan konto.
 * Nås via två hemliga nycklar (redigering och läsning). Bara nycklarnas SHA-256-hash sparas.
 */
@Entity
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT i MySQL
    private Long id;

    /** SHA-256 av redigeringsnyckeln, som hex. */
    @Column(nullable = false, columnDefinition = "CHAR(64)")
    private String editKeyHash;

    /** SHA-256 av läsnyckeln, som hex. */
    @Column(nullable = false, columnDefinition = "CHAR(64)")
    private String viewKeyHash;

    @Column(precision = 4, scale = 1)
    private BigDecimal meritValue;

    @Column(precision = 9, scale = 6)
    private BigDecimal homeLatitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal homeLongitude;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastUsedAt;

    /**
     * Optimistisk låsning: Hibernate räknar upp versionen vid varje uppdatering och
     * kastar ett fel om någon annan hunnit ändra arbetsytan sedan den lästes.
     */
    @Version
    private int version;

    protected Workspace() {
    }

    Workspace(String editKeyHash, String viewKeyHash, Instant now) {
        this.editKeyHash = editKeyHash;
        this.viewKeyHash = viewKeyHash;
        this.createdAt = now;
        this.lastUsedAt = now;
    }

    void markUsed(Instant now) {
        this.lastUsedAt = now;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getMeritValue() {
        return meritValue;
    }

    public void setMeritValue(BigDecimal meritValue) {
        this.meritValue = meritValue;
    }

    public BigDecimal getHomeLatitude() {
        return homeLatitude;
    }

    public BigDecimal getHomeLongitude() {
        return homeLongitude;
    }

    public void setHomePosition(BigDecimal latitude, BigDecimal longitude) {
        this.homeLatitude = latitude;
        this.homeLongitude = longitude;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public int getVersion() {
        return version;
    }

    // Avsiktligt inga getters för nyckelhasharna: de behövs bara för uppslag via repositoryt.

    @Override
    public boolean equals(Object o) {
        return o instanceof Workspace other && id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // id saknas innan entiteten sparats
    }
}
