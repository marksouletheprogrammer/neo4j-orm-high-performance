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
@Node("PC")
public class PC {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;

    @Relationship("HAS_BOX")
    private List<Box> boxes;

    @Relationship("CONTAINS")
    private List<Item> itemBox;
}
