package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RobotPart {
    @JsonProperty(Constants.NAME)
    @Getter
    @Setter
    private final String name;

    @JsonProperty(Constants.SERIAL_NUMBER)
    @Getter
    @Setter
    private final String serialNumber;

    @JsonProperty(Constants.MANUFACTURER)
    @Getter
    @Setter
    private final String manufacturer;

    @JsonProperty(Constants.WEIGHT)
    @Getter
    @Setter
    private final Integer weight;

    @JsonProperty(Constants.COMPATIBILITIES)
    @Getter
    @Setter
    private final String[] compatibilities;

    @JsonCreator
    public RobotPart(
            @JsonProperty(value = Constants.NAME, required = true)
            final String name,
            @JsonProperty(value = Constants.SERIAL_NUMBER, required = true)
            final String serialNumber,
            @JsonProperty(value = Constants.MANUFACTURER, required = true)
            final String manufacturer,
            @JsonProperty(value = Constants.WEIGHT, required = true)
            final Integer weight,
            @JsonProperty(value = Constants.COMPATIBILITIES, required = true)
            final String[] compatibilities) {
        this.name = name;
        this.serialNumber = serialNumber;
        this.manufacturer = manufacturer;
        this.weight = weight;
        this.compatibilities = compatibilities;
    }
}
