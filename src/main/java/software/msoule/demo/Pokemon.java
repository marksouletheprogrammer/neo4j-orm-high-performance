package software.msoule.demo;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Node("Pokemon")
public class Pokemon {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private String nickname;

    @Relationship("HAS_MOVE")
    private List<Move> moves;

    @Relationship("HAS_STATS")
    private Stats stats;

    @Relationship("IS_A")
    private Species species;

    @Relationship("BELONGS_TO")
    private Trainer owner;

    @Relationship("HOLDING")
    private Item heldItem;

}
