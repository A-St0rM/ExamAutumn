package app.services;

import app.DAO.CandidateDAO;
import app.DAO.SkillDAO;
import app.DTO.CandidateDTO;
import app.entities.Candidate;
import app.entities.Skill;
import app.mapper.DTOMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CandidateService {

    private final CandidateDAO candidateDAO;
    private final SkillDAO skillDAO;

    public CandidateService(CandidateDAO candidateDAO, SkillDAO skillDAO) {
        this.candidateDAO = candidateDAO;
        this.skillDAO = skillDAO;
    }

    public List<CandidateDTO> getAllCandidates() {
        List<Candidate> candidates = candidateDAO.getAll();
        return candidates.stream()
                .map(DTOMapper::toCandidateDTO)
                .collect(Collectors.toList());
    }

    public CandidateDTO getCandidateById(Integer id) {
        Candidate candidate = candidateDAO.getById(id);
        return DTOMapper.toCandidateDTO(candidate);
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
}

