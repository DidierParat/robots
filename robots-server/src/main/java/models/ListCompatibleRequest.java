package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListCompatibleRequest {
    @JsonProperty(Constants.SERIAL_NUMBER)
    @Getter
    private final String serialNumber;

    @JsonProperty(Constants.NUMBER)
    @Getter
    private final Integer number;

    public ListCompatibleRequest(
            @JsonProperty(value = Constants.SERIAL_NUMBER, required = true)
            final String serialNumber,
            @JsonProperty(value = Constants.NUMBER, required = true)
            final Integer number) {
        this.serialNumber = serialNumber;
        this.number = number;
    }
}
