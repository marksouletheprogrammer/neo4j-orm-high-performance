package software.msoule.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Node("Move")
public class Move {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;

    private String name;

    private String description;

    private int attackPower;

    private int accuracy;

    private String effect;

    private Element type;
}
