package app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GuideDTO {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private int yearsOfExperience;
}

