package app.mapper;

import app.DTO.GuideDTO;
import app.DTO.TripDTO;
import app.entities.Guide;
import app.entities.Trip;

import java.util.List;

public class TripMapper {

    public static TripDTO toDTO(Trip e) {
        if (e == null) return null;
        TripDTO d = new TripDTO();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setStartDate(e.getStartDate());
        d.setEndDate(e.getEndDate());
        d.setLocationCoordinates(e.getLocationCoordinates());
        d.setPrice(e.getPrice());
        d.setCategory(e.getCategory());
        if (e.getGuide() != null) {
            GuideDTO gdto = new GuideDTO();
            gdto.setId(e.getGuide().getId());
            gdto.setName(e.getGuide().getName());
            gdto.setEmail(e.getGuide().getEmail());
            gdto.setPhone(e.getGuide().getPhone());
            gdto.setYearsOfExperience(e.getGuide().getYearsOfExperience());
            d.setGuide(gdto);
        }
        // Packing items: returnér tom liste indtil du har entityen
        d.setPackingItems(List.of());
        return d;
    }

    public static Trip toEntity(TripDTO d, Guide guide) {
        Trip e = new Trip();
        e.setId(d.getId());
        e.setName(d.getName());
        e.setStartDate(d.getStartDate());
        e.setEndDate(d.getEndDate());
        e.setLocationCoordinates(d.getLocationCoordinates());
        e.setPrice(d.getPrice());
        e.setCategory(d.getCategory());
        e.setGuide(guide);
        return e;
    }

    public static void updateEntity(TripDTO d, Trip e, Guide guide) {
        e.setName(d.getName());
        e.setStartDate(d.getStartDate());
        e.setEndDate(d.getEndDate());
        e.setLocationCoordinates(d.getLocationCoordinates());
        e.setPrice(d.getPrice());
        e.setCategory(d.getCategory());
        if (guide != null) e.setGuide(guide);
    }
}

