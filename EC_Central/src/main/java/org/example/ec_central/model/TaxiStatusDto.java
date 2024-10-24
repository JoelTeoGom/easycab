package org.example.ec_central.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TaxiStatusDto {

    private String taxiId;
    private int x;
    private int y;
    private String status;

    public TaxiStatusDto(@JsonProperty("taxiId") String taxiId,
                         @JsonProperty("x") int x,
                         @JsonProperty("y") int y,
                         @JsonProperty("status") String status) {
        this.taxiId = taxiId;
        this.x = x;
        this.y = y;
        this.status = status;
    }

    // Getters and setters

}
