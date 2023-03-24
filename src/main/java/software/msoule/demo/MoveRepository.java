package software.msoule.demo;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MoveRepository extends Neo4jRepository<Move, Long> {

}
