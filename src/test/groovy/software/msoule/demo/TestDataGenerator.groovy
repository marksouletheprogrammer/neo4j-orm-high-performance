package software.msoule.demo

import java.security.SecureRandom
import java.util.stream.Collectors

// TODO a lot of this needs javadoc
class TestDataGenerator {

    static List<Move> generateMoves(int numMoves) {
        def moves = new ArrayList()
        for (int i = 0; i < numMoves; i++) {
            moves.add(Move.builder()
                .uuid(UUID.randomUUID().toString())
                .name("move" + i)
                .effect("test effect")
                .description("test description of what the move does")
                .attackPower(pickStatValue(0, 256))
                .accuracy(pickStatValue(0, 101))
                .type(randomEnum(Element.class))
                .build()
            )
        }
        return moves
    }

    static List<Item> generateItems(int numItems) {
        def items = new ArrayList()
        for (int i = 0; i < numItems; i++) {
            items.add(generateItem("item" + i))
        }
        return items
    }

    static Item generateItem(String name) {
        return Item.builder()
                .uuid(UUID.randomUUID().toString())
                .name(name)
                .effect("test effect")
                .sellValue(pickStatValue(0, 4000))
                .build()
    }

    static List<LearnMove> generateLearnMoves(List<Move> moves, int numLearn) {
        def learnMoves = new ArrayList()
        for (int i = 0; i < numLearn; i++) {
            learnMoves.add(LearnMove.builder()
                .move(moves.get(pickIndex(moves.size())))
                .level(numLearn * 4)
                .build()
            )
        }
        return learnMoves
    }

    static Stats generateStats() {
        return Stats.builder()
            .uuid(UUID.randomUUID().toString())
            .hp(pickStatValue(0, 256))
            .attack(pickStatValue(0, 256))
            .defense(pickStatValue(0, 256))
            .specialAttack(pickStatValue(0, 256))
            .specialDefense(pickStatValue(0, 256))
            .speed(pickStatValue(0, 256))
            .build()
    }

    static List<Species> generateSpecies(List<Move> moves, int numSpecies) {
        def species = new ArrayList<Species>()
        for (int i = 0; i < numSpecies; i++) {
            species.add(Species.builder()
                .uuid(UUID.randomUUID().toString())
                .name("pokemon" + i)
                .type1(randomEnum(Element.class))
                .type2(randomEnum(Element.class))
                .moves(generateLearnMoves(moves, 15))
                .baseStats(generateStats())
                .evolutions([] as Set)
                .build()
            )
        }
        // numSpecies / 3 because not all pokemon have evolutions.
        for (int i = numSpecies - 1; i > numSpecies / 3; i -= 3) {
            def specHigh = species.get(i)
            def specMid = species.get(i - 1)
            def specLow = species.get(i - 2)
            def evolutionLowToMid = Set.of(Evolution.builder()
                    .evolutionType(randomEnum(EvolutionType.class))
                    .level(pickStatValue(0, 100))
                    .evolution(specMid)
                    .build())
            def evolutionMidToHigh = Set.of(Evolution.builder()
                    .evolutionType(randomEnum(EvolutionType.class))
                    .level(pickStatValue(0, 100))
                    .evolution(specHigh)
                    .build())
            specLow.setEvolutions(evolutionLowToMid)
            specMid.setEvolutions(evolutionMidToHigh)
        }
        return species
    }

    static List<PokedexEntry> generateEntries(List<Species> species) {
        def entries = new ArrayList()
        for (int i = 1; i <= species.size(); i++) {
            entries.add(PokedexEntry.builder()
                .uuid(UUID.randomUUID().toString())
                .hasSeen(true)
                .owns(true)
                .description("test pokedex entry for pokemon")
                .number(i)
                .species(species.get(i - 1))
                .build())
        }
        return entries
    }

    static List<Pokemon> generatePokemon(List<Species> species) {
        def pokemon = new ArrayList()
        for (int i = 0; i < species.size(); i++) {
            def moves = species.get(i)
                    .getMoves()
                    .subList(0, 4)
                    .stream()
                    .map(LearnMove::getMove)
                    .collect(Collectors.toList())
            pokemon.add(Pokemon.builder()
                .uuid(UUID.randomUUID().toString())
                .nickname("testNickname" + i)
                .species(species.get(i))
                .stats(generateStats())
                .moves(moves)
                .heldItem(generateItem("testHeldItem"))
                .build())
        }
        return pokemon
    }

    static List<Box> generateBoxes(List<Pokemon> pokemon, int boxSize) {
        int numBoxes = pokemon.size() / boxSize
        def boxes = new ArrayList()
        for(int i = 0; i < numBoxes; i++) {
            def boxPokemon = pokemon.subList(i * boxSize, (i * boxSize) + boxSize)
            boxes.add(Box.builder()
                .uuid(UUID.randomUUID().toString())
                .name("testBox" + i)
                .pokemon(boxPokemon)
                .build())
        }
        return boxes
    }

    // todo javadoc [low, high)
    static int pickIndex(int length) {
        return (int) (Math.random() * length);
    }

    // todo javadoc [low, high)
    static int pickStatValue(int low, int high) {
        return (int) (Math.random() * (high - low)) + low;
    }

    static final SecureRandom random = new SecureRandom();
    static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
