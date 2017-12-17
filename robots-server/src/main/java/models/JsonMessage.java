package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonMessage {
    @JsonProperty(Constants.MESSAGE)
    @Getter
    private final String message;

    public JsonMessage(
            @JsonProperty(value = Constants.MESSAGE, required = true)
            final String message) {
        this.message = message;
    }
}
