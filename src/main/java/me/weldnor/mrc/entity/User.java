package me.weldnor.mrc.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserPassword password;

    @Column(nullable = false)
    private LocalDate registrationDate = LocalDate.now();

    @ManyToMany
    @JoinTable(
            name = "person_global_role",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "global_role_id"))
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<GlobalRole> globalRoles = new HashSet<>();

}
