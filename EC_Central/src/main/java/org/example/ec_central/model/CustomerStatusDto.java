package org.example.ec_central.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class CustomerStatusDto extends TaxiStatusDto {
    int customerX;
    int customerY;
    int destX;
    int destY;
}
