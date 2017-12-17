package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadRequest {
    @JsonProperty(Constants.SERIAL_NUMBER)
    @Getter
    private final String serialNumber;

    public ReadRequest(
            @JsonProperty(value = Constants.SERIAL_NUMBER, required = true)
            final String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
