package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRequest {
    @JsonProperty(Constants.ORIGINAL_SERIAL_NUMBER)
    @Getter
    private final String originalSerialNumber;

    @JsonProperty(Constants.FIELD_TO_UPDATE)
    @Getter
    private final String fieldToUpdate;

    @JsonProperty(Constants.VALUE_OF_FIELD)
    @Getter
    private final Object valueOfField;

    @JsonCreator
    public UpdateRequest(
            @JsonProperty(
                    value = Constants.ORIGINAL_SERIAL_NUMBER, required = true)
            final String originalSerialNumber,
            @JsonProperty(value = Constants.FIELD_TO_UPDATE, required = true)
            final String fieldToUpdate,
            @JsonProperty(value = Constants.VALUE_OF_FIELD, required = true)
            final Object valueOfField) {
        this.originalSerialNumber = originalSerialNumber;
        this.fieldToUpdate = fieldToUpdate;
        this.valueOfField = valueOfField;
    }
}
