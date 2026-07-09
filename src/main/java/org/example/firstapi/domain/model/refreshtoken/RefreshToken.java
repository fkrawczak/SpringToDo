package org.example.firstapi.domain.model.refreshtoken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.validation.DomainValidation;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    protected RefreshToken() {
    }

    public RefreshToken(User user, String tokenHash, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.user = Objects.requireNonNull(user, "user cannot be null");
        this.tokenHash = DomainValidation.requireText(tokenHash, "tokenHash");
        this.createdAt = DomainValidation.requireDateTime(createdAt, "createdAt");
        this.expiresAt = DomainValidation.requireDateTime(expiresAt, "expiresAt");
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RefreshToken refreshToken)) {
            return false;
        }
        return id != null && Objects.equals(id, refreshToken.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
