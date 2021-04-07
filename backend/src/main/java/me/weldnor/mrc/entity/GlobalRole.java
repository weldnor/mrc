package me.weldnor.mrc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "global_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long globalRoleId;

    @Column(unique = true, nullable = false)
    private String name;
}
