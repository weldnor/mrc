package me.weldnor.mrc.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_passwords")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_password_id")
    private Long userPasswordId;

    private String passwordHash;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;
}
