package app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GuideTotalDTO {
    private Integer guideId;
    private Double totalPrice;
}
