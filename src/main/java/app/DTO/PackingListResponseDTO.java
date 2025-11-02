package app.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PackingListResponseDTO {
    private List<PackingItemDTO> items;
}
