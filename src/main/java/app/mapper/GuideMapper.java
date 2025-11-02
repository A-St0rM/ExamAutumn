package app.mapper;

import app.DTO.GuideDTO;
import app.entities.Guide;

public class GuideMapper {

    public static GuideDTO toDTO(Guide e) {
        if (e == null) return null;
        GuideDTO d = new GuideDTO();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setEmail(e.getEmail());
        d.setPhone(e.getPhone());
        d.setYearsOfExperience(e.getYearsOfExperience());
        return d;
    }

    public static Guide toEntity(GuideDTO d) {
        if (d == null) return null;
        Guide e = new Guide();
        e.setId(d.getId());
        e.setName(d.getName());
        e.setEmail(d.getEmail());
        e.setPhone(d.getPhone());
        e.setYearsOfExperience(d.getYearsOfExperience());
        return e;
    }

    public static void updateEntity(GuideDTO d, Guide e) {
        e.setName(d.getName());
        e.setEmail(d.getEmail());
        e.setPhone(d.getPhone());
        e.setYearsOfExperience(d.getYearsOfExperience());
    }
}
