package app.mapper;

import app.DTO.CandidateDTO;
import app.DTO.SkillDTO;
import app.entities.Candidate;
import app.entities.Skill;

import java.util.HashSet;
import java.util.Set;

public class DTOMapper {

    // Convert Candidate entity to CandidateDTO
    public static CandidateDTO toCandidateDTO(Candidate candidate) {
        Set<SkillDTO> skillDTOs = new HashSet<>();
        if (candidate.getSkills() != null) {
            for (Skill skill : candidate.getSkills()) {
                skillDTOs.add(toSkillDTO(skill));
            }
        }
        return new CandidateDTO(
                candidate.getId(),
                candidate.getName(),
                candidate.getPhone(),
                candidate.getEducationBackground(),
                skillDTOs
        );
    }

    // Convert Skill entity to SkillDTO
    public static SkillDTO toSkillDTO(Skill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setDescription(skill.getDescription());
        dto.setCategory(skill.getCategory());

        dto.setSlug(generateSlug(skill.getName()));

        return dto;
    }

    private static String generateSlug(String name) {
        if (name == null) return null;
        return name.toLowerCase().replace(" ", "-");
    }

    // Convert CandidateDTO to Candidate entity
    public static Candidate toCandidateEntity(CandidateDTO candidateDTO) {
        Candidate candidate = new Candidate();
        candidate.setId(candidateDTO.getId());
        candidate.setName(candidateDTO.getName());
        candidate.setPhone(candidateDTO.getPhone());
        candidate.setEducationBackground(candidateDTO.getEducationBackground());
        return candidate;
    }

    // Convert SkillDTO to Skill entity
    public static Skill toSkillEntity(SkillDTO skillDTO) {
        Skill skill = new Skill();
        skill.setId(skillDTO.getId());
        skill.setName(skillDTO.getName());
        skill.setDescription(skillDTO.getDescription());
        skill.setCategory(skillDTO.getCategory());
        return skill;
    }
}
