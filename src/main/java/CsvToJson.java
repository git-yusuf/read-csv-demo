import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvToJson {
    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(Paths.get("src/main/resources/config.json").toFile());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem reading configuration file. Exiting program.");
            return;
        }

        JsonNode configs = rootNode.get("configs");

        List<CsvConfig> csvConfigs = null;

        if (configs.isArray()){
            try {
                csvConfigs = mapper.readValue(configs.toString(), new TypeReference<List<CsvConfig>>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println("Invalid configuration file. Exiting program.");
                return;
            }
        } else {
            System.out.println("Invalid configuration file. Exiting program.");
            return;
        }

        Path input_file = Paths.get("src/main/resources/input_file.csv");

        try (Stream<String> stream = Files.lines(input_file)) {

            List<String> fileEntries = stream.collect(Collectors.toList());

            ArrayNode personsNode = mapper.createArrayNode();

            List<CsvConfig> finalCsvConfigs = csvConfigs;

            List<ObjectNode> nodesList = fileEntries.stream()
                    .skip(1)
                    .filter(entry -> {
                        String[] row = entry.split(",");
                        return row.length == finalCsvConfigs.size();
                    })
                    .map(validRecord -> {
                        ObjectNode objectNode = mapper.createObjectNode();
                        String[] row = validRecord.split(",");

                        int i = 0;

                        for (CsvConfig config : finalCsvConfigs) {
                            if (StringUtils.equalsIgnoreCase(config.getType(), "Int")){
                                objectNode.put(config.getJsonAttribute(), Integer.parseInt(row[i]));
                            } else {
                                objectNode.put(config.getJsonAttribute(), row[i]);
                            }
                            i++;
                        }
                        return objectNode;
                    })
                    .collect(Collectors.toList());

            personsNode.addAll(nodesList);

            ObjectNode root = mapper.createObjectNode();

            root.set("persons", personsNode);

            System.out.println(root.toPrettyString());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem reading input csv file. Exiting program.");
        }
    }
}
