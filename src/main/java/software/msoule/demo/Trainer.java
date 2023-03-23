package software.msoule.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Node
public class Trainer {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private String name;

    @Relationship("HAS_ON_TEAM")
    private List<Pokemon> team;

    @Relationship("OWNS")
    private Pokedex pokedex;

    @Relationship("ACCESS_TO")
    private PC pc;


}
