package app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "candidate")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_id", nullable = false, unique = true)
    private Integer id;

    private String name;

    private String phone;

    private String educationBackground;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "candidate_skill",
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;

    public Candidate(String name, String number, String educationBackground) {
        this.name = name;
        this.phone = number;
        this.educationBackground = educationBackground;
    }
}
