package app.DTO;

import app.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TripDTO {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String locationCoordinates;
    private double price;
    private Category category;

    //Da vi skal bruge alle oplysninger for guide er dte bedre med objekt reference end attributter
    private GuideDTO guide;

    private List<PackingItemDTO> packingItems;
}

