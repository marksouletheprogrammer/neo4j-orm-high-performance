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
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Node("Species")
public class Species {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private String name;

    @Relationship("HAS_STATS")
    private Stats baseStats;

    private Element type1;

    private Element type2;

    @Relationship("LEARNS_MOVE")
    private List<LearnMove> moves;

    @Relationship("EVOLVES_INTO")
    private Set<Evolution> evolutions;

}
