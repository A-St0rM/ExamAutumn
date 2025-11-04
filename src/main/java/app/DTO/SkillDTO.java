package app.DTO;

import app.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SkillDTO {
    private Integer id;
    private String name;
    private String description;
    private Category category;
}
