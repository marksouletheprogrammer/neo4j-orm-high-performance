package software.msoule.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@RelationshipProperties
public class LearnMove {

    @RelationshipId
    private Long id;

    private int level;

    @TargetNode
    private Move move;

}
