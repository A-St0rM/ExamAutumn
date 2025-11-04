package app.services;

import app.DAO.CandidateDAO;
import app.DAO.SkillDAO;
import app.DTO.CandidateDTO;
import app.DTO.SkillDTO;
import app.DTO.SkillStatsDTO;
import app.entities.Candidate;
import app.entities.Skill;
import app.mapper.DTOMapper;

import java.util.*;
import java.util.stream.Collectors;

public class CandidateService {

    private final CandidateDAO candidateDAO;
    private final SkillDAO skillDAO;
    private final ApiService apiService;

    public CandidateService(CandidateDAO candidateDAO, SkillDAO skillDAO, ApiService apiService) {
        this.candidateDAO = candidateDAO;
        this.skillDAO = skillDAO;
        this.apiService = apiService;
    }

    public List<CandidateDTO> getCandidatesByCategory(String category) {

        List<Candidate> candidates = candidateDAO.getAll();

        return candidates.stream()
                .filter(candidate -> candidate.getSkills().stream()
                        .anyMatch(skill -> skill.getCategory().equals(category)))
                .map(DTOMapper::toCandidateDTO)
                .collect(Collectors.toList());
    }

    public List<CandidateDTO> getAllCandidates() {
        List<Candidate> candidates = candidateDAO.getAll();
        return candidates.stream()
                .map(DTOMapper::toCandidateDTO)
                .collect(Collectors.toList());
    }

    public CandidateDTO getCandidateById(Integer id) {
        Candidate candidate = candidateDAO.getById(id);
        if (candidate == null) return null;

        CandidateDTO candidateDTO = DTOMapper.toCandidateDTO(candidate);

        if (candidateDTO.getSkills() == null || candidateDTO.getSkills().isEmpty()) {
            candidateDTO.setSkills(Set.of());
            return candidateDTO;
        }


        List<String> slugs = candidateDTO.getSkills().stream()
                .map(SkillDTO::getSlug)
                .filter(Objects::nonNull)
                .toList();

        List<SkillStatsDTO> skillStats = apiService.fetchSkillStats(slugs);

        Map<String, SkillStatsDTO> statsMap = skillStats.stream()
                .collect(Collectors.toMap(SkillStatsDTO::getSlug, s -> s));


        candidateDTO.getSkills().forEach(skillDTO -> {
            SkillStatsDTO stat = statsMap.get(skillDTO.getSlug());
            if (stat != null) {
                skillDTO.setPopularityScore(stat.getPopularityScore());
                skillDTO.setAverageSalary(stat.getAverageSalary());
            }
        });

        return candidateDTO;
    }

    public CandidateDTO createCandidate(CandidateDTO candidateDTO) {
        Candidate candidate = DTOMapper.toCandidateEntity(candidateDTO);
        candidate = candidateDAO.create(candidate);
        return DTOMapper.toCandidateDTO(candidate);
    }

    public CandidateDTO updateCandidate(CandidateDTO candidateDTO) {
        Candidate candidate = DTOMapper.toCandidateEntity(candidateDTO);
        candidate = candidateDAO.update(candidate);
        return DTOMapper.toCandidateDTO(candidate);
    }

    public boolean deleteCandidate(Integer id) {
        return candidateDAO.delete(id);
    }

    public boolean linkSkillToCandidate(Integer candidateId, Integer skillId) {
        Candidate candidate = candidateDAO.getById(candidateId);
        Skill skill = skillDAO.getById(skillId);

        if (candidate == null || skill == null) {
            return false;
        }

        Set<Skill> skills = candidate.getSkills();
        skills.add(skill);
        candidate.setSkills(skills);
        candidateDAO.update(candidate);

        return true;
    }

    public Map<String, Object> getTopCandidateByPopularity() {
        try {
            List<Candidate> candidates = candidateDAO.getAll();

            // Hvis ingen kandidater findes, returner et besked
            if (candidates.isEmpty()) {
                return Map.of("message", "No candidates found");
            }

            // Mapper kandidater til DTO'er for videre behandling
            List<CandidateDTO> candidateDTOs = candidates.stream()
                    .map(DTOMapper::toCandidateDTO)
                    .collect(Collectors.toList());

            double highestAvg = 0.0;
            Integer topCandidateId = null;

            // Beregning af gennemsnitlig popularitet for hver kandidat
            for (CandidateDTO candidateDTO : candidateDTOs) {
                if (candidateDTO.getSkills() == null || candidateDTO.getSkills().isEmpty()) {
                    continue;
                }

                // Få slug for skills og hent statistikker for dem
                List<String> slugs = candidateDTO.getSkills().stream()
                        .map(SkillDTO::getSlug)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (slugs.isEmpty()) continue;

                // Hent skill stats og beregn gennemsnitlig popularitet
                List<SkillStatsDTO> skillStats = apiService.fetchSkillStats(slugs);
                double avgPopularity = skillStats.stream()
                        .mapToInt(SkillStatsDTO::getPopularityScore)
                        .average()
                        .orElse(0.0);

                // Opdater den kandidat med den højeste gennemsnitlige popularitet
                if (avgPopularity > highestAvg) {
                    highestAvg = avgPopularity;
                    topCandidateId = candidateDTO.getId();
                }
            }

            // Hvis ingen kandidat har en beregnet popularitet
            if (topCandidateId == null) {
                return Map.of("message", "No candidates with skill popularity data");
            }

            // Returnér ID for topkandidaten og gennemsnitlig popularitet
            return Map.of(
                    "candidateId", topCandidateId,
                    "averagePopularityScore", highestAvg
            );
        } catch (Exception e) {
            // Håndter eventuelle fejl
            return Map.of("message", "Internal error occurred: " + e.getMessage());
        }
    }

}

