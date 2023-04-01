package software.msoule.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Node("PokedexEntry")
public class PokedexEntry {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private boolean hasSeen;
    private boolean owns;

    private String description;
    private int number;

    @Relationship("ENTRY_FOR")
    private Species species;


}
