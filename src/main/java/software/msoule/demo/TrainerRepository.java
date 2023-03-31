package software.msoule.demo;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TrainerRepository extends Neo4jRepository<Trainer, Long>, TrainerCustomRepository {

    Trainer findByName(String name);

    Trainer findByUuid(String uuid);

}
