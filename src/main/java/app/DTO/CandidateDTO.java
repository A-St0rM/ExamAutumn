package app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CandidateDTO {
    private Integer id;
    private String name;
    private String phone;
    private String educationBackground;
    private Set<SkillDTO> skills;

}
