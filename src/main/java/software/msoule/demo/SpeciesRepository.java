package software.msoule.demo;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SpeciesRepository extends Neo4jRepository<Species, Long> {

}
