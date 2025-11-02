package app.DTO;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class PackingItemDTO {
    private String name;
    private int weightInGrams;
    private int quantity;
    private String description;
    private String category;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<BuyingOptionDTO> buyingOptions;
}
