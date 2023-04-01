package software.msoule.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@RelationshipProperties
public class Evolution {

    @RelationshipId
    private Long id;

    private EvolutionType evolutionType;

    private int level;

    @TargetNode
    private Species evolution;

}
