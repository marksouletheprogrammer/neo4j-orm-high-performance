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

    // ~1.5 seconds for 20 moves, 10 species, 200 items
    // ~15 seconds for 913 moves, 905 species, 200 items
    // ~94 seconds for 4000 moves, 4000 species, 1000 items
    // ~136 seconds for 5000 moves, 5000 species, 2000 items
    void "save pokemon trainer graph"() {
        given:
        def moves = TestDataGenerator.generateMoves(913)
        moveRepository.saveAll(moves)
        def species = TestDataGenerator.generateSpecies(moves, 905)
        speciesRepository.saveAll(species)
        def items = TestDataGenerator.generateItems(200)

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
                .uuid(UUID.randomUUID().toString())
                .team(team)
                .pokedex(pokedex)
                .pc(pc)
                .inventory(items.subList(0, 100))
                .build()
        pokemon.forEach(p -> p.setOwner(mark))
        when:
        def beginWrite = System.currentTimeMillis()
        trainerRepository.save(mark)
        def endWrite = System.currentTimeMillis()

//        def beginRead = System.currentTimeMillis()
//        def trainer = trainerRepository.findByUuid(mark.getName())
//        def endRead = System.currentTimeMillis()

//        def t = trainer.getTeam()
//        def box0 = trainer.getPc().getBoxes().get(0)
//        def toKeep = t.subList(0, 4)
//        toKeep.addAll(box0.getPokemon())
//        box0.setPokemon(toKeep)
//        trainer.setTeam([])

//        def beginUpdate = System.currentTimeMillis()
//        trainerRepository.save(trainer)
//        def endUpdate = System.currentTimeMillis()

        def writeSeconds = (endWrite - beginWrite) / 1000
        System.out.println("WRITE TIME: " + writeSeconds + " seconds")
//        def readSeconds = (endRead - beginRead) / 1000
//        System.out.println("READ TIME: " + readSeconds + " seconds")
//        def updateSeconds = (endUpdate - beginUpdate) / 1000
//        System.out.println("UPDATE TIME: " + updateSeconds + " seconds")

        then:
        true
    }

    // ~1.3 seconds for 20 moves, 10 species, 200 items
    // ~4 seconds for 913 moves, 905 species, 200 items
    // ~14 seconds for 4000 moves, 4000 species, 1000 items
    // ~16 seconds for 5000 moves, 5000 species, 2000 items
    void "performant save"() {
        given:
        def moves = TestDataGenerator.generateMoves(913)
        moveRepository.saveAll(moves)
        def species = TestDataGenerator.generateSpecies(moves, 905)
        speciesRepository.saveAll(species)
        def items = TestDataGenerator.generateItems(200)

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
                .uuid(UUID.randomUUID().toString())
                .team(team)
                .pokedex(pokedex)
                .pc(pc)
                .inventory(items.subList(0, 100))
                .build()
        pokemon.forEach(p -> p.setOwner(mark))
        when:
        def beginWrite = System.currentTimeMillis()
        trainerRepository.saveTrainer(mark)
        def endWrite = System.currentTimeMillis()

//        def beginRead = System.currentTimeMillis()
//        def trainer = trainerRepository.findByUuid(mark.getUuid())
//        def endRead = System.currentTimeMillis()
//
//        def t = trainer.getTeam()
//        def box0 = trainer.getPc().getBoxes().get(0)
//        def toKeep = t.subList(0, 4)
//        toKeep.addAll(box0.getPokemon())
//        box0.setPokemon(toKeep)
//        trainer.setTeam([])
//
//        def beginUpdate = System.currentTimeMillis()
//        trainerRepository.saveTrainer(trainer)
//        def endUpdate = System.currentTimeMillis()

        def writeSeconds = (endWrite - beginWrite) / 1000
        System.out.println("WRITE TIME: " + writeSeconds + " seconds")
//        def readSeconds = (endRead - beginRead) / 1000
//        System.out.println("READ TIME: " + readSeconds + " seconds")
//        def updateSeconds = (endUpdate - beginUpdate) / 1000
//        System.out.println("UPDATE TIME: " + updateSeconds + " seconds")

        then:
        true
    }
}
