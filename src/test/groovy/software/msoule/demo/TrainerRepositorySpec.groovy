package software.msoule.demo

import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(properties = [
        "logging.level.org.springframework.data.neo4j=info"
])
class TrainerRepositorySpec extends Specification {

    @Autowired
    private Driver driver;

    @Autowired
    private MoveRepository moveRepository

    @Autowired
    private SpeciesRepository speciesRepository

    @Autowired
    private TrainerRepository trainerRepository

    private final int NUM_MOVES = 913
    private final int NUM_SPECIES = 905
    private final int NUM_ITEMS = 200

    def setup() {
        try (var session = driver.session()) {
            // Drop all indexes.
            session.run("CALL apoc.schema.assert({}, {}, true) YIELD label, key RETURN *").consume()
            // Drop all nodes.
            session.run("MATCH (n) DETACH DELETE n").consume()
            session.run("CREATE INDEX trainer_uuid_index FOR (n:Trainer) on (n.uuid)").consume()
            session.run("CREATE INDEX pokemon_uuid_index FOR (n:Pokemon) on (n.uuid)").consume()
            session.run("CREATE INDEX item_uuid_index FOR (n:Item) on (n.uuid)").consume()
            session.run("CREATE INDEX species_uuid_index FOR (n:Species) on (n.uuid)").consume()
            session.run("CREATE INDEX stats_uuid_index FOR (n:Stats) on (n.uuid)").consume()
            session.run("CREATE INDEX entry_uuid_index FOR (n:PokedexEntry) on (n.uuid)").consume()
            session.run("CREATE INDEX pc_uuid_index FOR (n:PC) on (n.uuid)").consume()
            session.run("CREATE INDEX box_uuid_index FOR (n:Box) on (n.uuid)").consume()
            session.run("CREATE INDEX move_uuid_index FOR (n:Move) on (n.uuid)").consume()
        }
    }

    void "save pokemon trainer graph"() {
        given:
        def mark = initTrainer("Mark")
        when:
        System.out.println("Congratulations " + mark.getName() + "!")
        System.out.println("You have collected every single pokemon!")
        System.out.println("You collected all " + mark.getPokedex().getEntries().size() + " pokemon.")
        System.out.println("Do you want to save?")
        System.out.println("saving...")

        def beginWrite = System.currentTimeMillis()
        trainerRepository.save(mark)
        def endWrite = System.currentTimeMillis()

        def writeSeconds = (endWrite - beginWrite) / 1000
        System.out.println("save completed in " + writeSeconds + " seconds")
        then:
        mark
    }

















    void "performant save"() {
        given:
        def ash = initTrainer("Ash")
        when:
        def beginWrite = System.currentTimeMillis()
        trainerRepository.saveTrainer(ash)
        def endWrite = System.currentTimeMillis()
        def writeSeconds = (endWrite - beginWrite) / 1000
        System.out.println("save completed in " + writeSeconds + " seconds")
        then:
        ash
    }

    void "projection save"() {
        given:
        def red = initTrainer("Red")
        when:
        def result = trainerRepository.saveProjection(red)
        def lookup = trainerRepository.findByUuid(red.getUuid())
        then:
        result
        lookup
        lookup.getPokedex() == null
        lookup.getTeam().size() == 6
    }

    /**
     * Initialize trainer for the test. I just have this here to make the tests easier to read.
     * Notice that I save moves and species ahead of time. When our test saves it will actually do an upsert.
     */
    private Trainer initTrainer(String name) {
        def moves = TestDataGenerator.generateMoves(NUM_MOVES)
        moveRepository.saveAll(moves)
        def species = TestDataGenerator.generateSpecies(moves, NUM_SPECIES)
        speciesRepository.saveAll(species)
        def items = TestDataGenerator.generateItems(NUM_ITEMS)

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
        def trainer = Trainer.builder()
                .name(name)
                .uuid(UUID.randomUUID().toString())
                .team(team)
                .pokedex(pokedex)
                .pc(pc)
                .inventory(items.subList(0, 100))
                .build()
        pokemon.forEach(p -> p.setOwner(trainer))
        return trainer
    }
}
