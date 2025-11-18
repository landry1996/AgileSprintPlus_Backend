package com.agilesprintplus.agilesprint.domain;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name="gamification_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamificationProfile {
  @Id @GeneratedValue(strategy=GenerationType.UUID)
  private UUID id;

  @OneToOne(optional=false)
  private User user;

  private int xp;
  private int badges;
  private int tasksDone;
  private int sprintsCompleted;
}
