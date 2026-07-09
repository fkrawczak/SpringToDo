package org.example.firstapi.domain.model.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.validation.DomainValidation;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<TaskItem> taskItems = new LinkedHashSet<>();

    protected User() {
    }

    public User(String email, String password, String firstName, String lastName) {
        this.id = UUID.randomUUID();
        this.email = DomainValidation.requireText(email, "email");
        this.password = DomainValidation.requireText(password, "password");
        this.firstName = DomainValidation.requireText(firstName, "firstName");
        this.lastName = DomainValidation.requireText(lastName, "lastName");
    }

    public void changeEmail(String email) {
        this.email = DomainValidation.requireText(email, "email");
    }

    public void changePassword(String password) {
        this.password = DomainValidation.requireText(password, "password");
    }

    public void rename(String firstName, String lastName) {
        this.firstName = DomainValidation.requireText(firstName, "firstName");
        this.lastName = DomainValidation.requireText(lastName, "lastName");
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Set<TaskItem> getTaskItems() {
        return Set.copyOf(taskItems);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User user)) {
            return false;
        }
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
