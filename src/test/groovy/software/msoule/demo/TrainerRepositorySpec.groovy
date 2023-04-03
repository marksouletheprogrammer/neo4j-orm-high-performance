package software.msoule.demo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@SpringBootTest
class TrainerRepositorySpec extends Specification {

    @Autowired
    private MoveRepository moveRepository

    @Autowired
    private SpeciesRepository speciesRepository

    @Autowired
    private StatsRepository statsRepository

    @Autowired
    private PCRepository pcRepository

    @Autowired
    private BoxRepository boxRepository

    @Autowired
    private PokedexRepository pokedexRepository

    @Autowired
    private PokemonRepository pokemonRepository

    @Autowired
    private PokedexEntryRepository pokedexEntryRepository

    @Autowired
    private ItemRepository itemRepository

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TrainerRepository trainerRepository

    private final int NUM_MOVES = 4000
    private final int NUM_SPECIES = 4000
    private final int NUM_ITEMS = 800

    def setup() {
        jdbcTemplate.execute("TRUNCATE TABLE box CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE item CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE move CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE pc CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE pokedex CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE pokedex_entry CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE pokemon CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE species CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE stats CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE trainer CASCADE")
    }

    void "save pokemon trainer relations"() {
        given:
        def moves = TestDataGenerator.generateMoves(NUM_MOVES)
        moveRepository.saveAll(moves)
        def species = TestDataGenerator.generateSpecies(moves, NUM_SPECIES)
        def stats = species.stream().map(Species::getBaseStats).toList()
        statsRepository.saveAll(stats)
        speciesRepository.saveAll(species)
        def items = TestDataGenerator.generateItems(NUM_ITEMS)
        def entries = TestDataGenerator.generateEntries(species)
        def pokemon = TestDataGenerator.generatePokemon(species)
        def pokemonStats = pokemon.stream().map(Pokemon::getStats).toList()
        def heldItems = pokemon.stream().map(Pokemon::getHeldItem).toList()
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
        when:
        def beginWrite = System.currentTimeMillis()
        itemRepository.saveAll(items)
        statsRepository.saveAll(pokemonStats)
        itemRepository.saveAll(heldItems)
        pokemonRepository.saveAll(pokemon)
        boxRepository.saveAll(boxes)
        pcRepository.save(pc)
        pokedexEntryRepository.saveAll(entries)
        pokedexRepository.save(pokedex)
        def mark = Trainer.builder()
                .name("Mark")
                .uuid(UUID.randomUUID().toString())
                .team(team)
                .pokedex(pokedex)
                .pc(pc)
                .inventory(items.subList(0, 100))
                .build()
        pokemon.forEach(p -> p.setOwner(mark))
        def result = trainerRepository.save(mark)
        def endWrite = System.currentTimeMillis()


        def writeSeconds = (endWrite - beginWrite) / 1000
        System.out.println("WRITE TIME: " + writeSeconds + " seconds")

        then:
        result
    }
}
