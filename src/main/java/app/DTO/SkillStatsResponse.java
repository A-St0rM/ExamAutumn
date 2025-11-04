package app.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SkillStatsResponse {

    private List<SkillStatsDTO> data;
}