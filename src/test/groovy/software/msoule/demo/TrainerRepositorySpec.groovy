package software.msoule.demo

import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class TrainerRepositorySpec extends Specification {

    @Autowired
    private Driver driver;

    @Autowired
    private MoveRepository moveRepository

    @Autowired
    private SpeciesRepository speciesRepository

    @Autowired
    private TrainerRepository trainerRepository

    def setup() {
        try (var session = driver.session()) {
            // Drop all indexes.
            session.run("CALL apoc.schema.assert({}, {}, true) YIELD label, key RETURN *").consume()
            // Drop all nodes.
            session.run("MATCH (n) DETACH DELETE n").consume()
        }
    }

    // ~1.5 seconds for 20 moves, 10 species, 200 items
    // ~15 seconds for 913 moves, 905 species, 200 items
    // ~94 seconds for 4000 moves, 4000 species, 1000 items
    // 37.255 seconds for 5000 moves, 5000 species, 1000 items
    void "save pokemon trainer graph"() {
        given:
        def moves = TestDataGenerator.generateMoves(4000) //913
        moveRepository.saveAll(moves)
        def species = TestDataGenerator.generateSpecies(moves, 4000) // 905
        speciesRepository.saveAll(species)
        def items = TestDataGenerator.generateItems(1000)

        def entries = TestDataGenerator.generateEntries(species)
        def pokemon = TestDataGenerator.generatePokemon(species)
        def team = pokemon.subList(0, 6)
        def boxes = TestDataGenerator.generateBoxes(pokemon.subList(6, pokemon.size()), 50)

        def pc = PC.builder()
                .uuid(UUID.randomUUID().toString())
                .boxes(boxes)
                .itemBox(items.subList(100, items.size()))
                .build()
        def pokedex = Pokedex.builder()
                .uuid(UUID.randomUUID().toString())
                .entries(entries)
                .build()
        def mark = Trainer.builder()
                .name("Mark")
                .team(team)
                .pokedex(pokedex)
                .pc(pc)
                .inventory(items.subList(0, 100))
                .build()
        pokemon.forEach(p -> p.setOwner(mark))
        when:
        def begin = System.currentTimeMillis()
        def result = trainerRepository.save(mark)
        def end = System.currentTimeMillis()
        def writeSeconds = (end - begin) / 1000
        System.out.println("WRITE TIME: " + writeSeconds + " seconds")
        then:
        result
    }
}
