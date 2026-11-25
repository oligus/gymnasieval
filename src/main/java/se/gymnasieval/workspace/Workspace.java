package se.gymnasieval.workspace;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import org.hibernate.annotations.MapKeyJdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
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

    /** Vikt 0–5 per kriterium. Saknas ett kriterium gäller dess standardvikt. */
    @ElementCollection
    @CollectionTable(name = "workspace_weight", joinColumns = @JoinColumn(name = "workspace_id"))
    @MapKeyColumn(name = "criterion", length = 20)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyJdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "weight", nullable = false, columnDefinition = "TINYINT")
    private Map<Criterion, Integer> weights = new EnumMap<>(Criterion.class);

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

    /** Alla kriteriers vikter, med standardvikt där ingen är satt. */
    public Map<Criterion, Integer> getWeights() {
        Map<Criterion, Integer> all = new EnumMap<>(Criterion.class);
        for (Criterion criterion : Criterion.values()) {
            all.put(criterion, weights.getOrDefault(criterion, criterion.defaultWeight()));
        }
        return all;
    }

    void setWeight(Criterion criterion, int weight) {
        weights.put(criterion, weight);
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
