package com.agilesprintplus.agilesprint.domain;

import com.agilesprintplus.security.entity.Token;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import java.time.Instant;
import java.util.*;
@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  @Id @GeneratedValue(strategy=GenerationType.UUID)
  private UUID id;

  @Column(nullable=false, unique=true, length=64)
  private String username;

  @Column(nullable=false)
  private String passwordHash;

  @Column(nullable=false, unique=true, length=160)
  private String email;

  private String firstName;
  private String lastName;

  @ElementCollection(fetch=FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
  @Column(name="role")
  private Set<Role> roles = new HashSet<>();

  @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @JsonIgnore
  private Set<Task> tasks = new HashSet<>();

  private boolean enabled = true;
  @CreationTimestamp
  private Instant createdAt;
  @UpdateTimestamp
  private Instant updatedAt;

  @Singular("token")
  @JsonIgnore
  @OneToMany(mappedBy = "user")
  private List<Token> tokenList;

  @Builder.Default
  @Column(nullable = false)
  private boolean passwordChangeRequired = false;

  public void addTask(Task t) {
    this.tasks.add(t);
    t.getUsers().add(this);
  }

  public void removeTask(Task t) {
    this.tasks.remove(t);
    t.getUsers().remove(this);
  }
}
