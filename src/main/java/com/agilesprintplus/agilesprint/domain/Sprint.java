package com.agilesprintplus.agilesprint.domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.*;
import java.util.*;
@Entity
@Table(name="sprints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sprint {
  @Id
  @GeneratedValue(strategy=GenerationType.UUID)
  private UUID id;

  @Column(nullable=false)
  private String name;

  private LocalDate startDate;
  private LocalDate endDate;

  private Integer durationDays;
  private String goal;

  @OneToMany(mappedBy="sprint", cascade=CascadeType.ALL, orphanRemoval=true)
  private List<Task> tasks=new ArrayList<>();

  private boolean enabled = true;
  @CreationTimestamp
  private Instant createdAt;
}
