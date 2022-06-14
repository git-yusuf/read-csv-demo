import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CsvConfig {
    @JsonProperty(value = "csv_attr")
    private String csvAttribute;
    @JsonProperty(value = "type")
    private String type;
    @JsonProperty(value = "json_attr")
    private String jsonAttribute;
}
