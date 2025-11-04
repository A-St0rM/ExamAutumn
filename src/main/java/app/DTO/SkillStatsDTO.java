package app.DTO;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class SkillStatsDTO {
    private String id;
    private String slug;
    private String name;
    private String categoryKey;
    private String description;
    private int popularityScore;
    private int averageSalary;
    private ZonedDateTime updatedAt;
}
