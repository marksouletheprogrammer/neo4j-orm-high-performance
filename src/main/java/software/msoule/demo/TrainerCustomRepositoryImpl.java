package software.msoule.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TrainerCustomRepositoryImpl implements TrainerCustomRepository {

    private static final int BATCH_SIZE = 1000;

    private final Neo4jClient neo4jClient;

    private final ObjectMapper objectMapper;

    public TrainerCustomRepositoryImpl(Neo4jClient neo4jClient, ObjectMapper objectMapper) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
    }

    // TODO javadoc
    @Override
    public void saveTrainer(Trainer trainer) {
        TrainerSaveInput.TrainerInput prepared = prepareInput(trainer);
        upsertFullTrainer(prepared);
        deleteDetached(trainer.getUuid(), prepared);
    }

    private TrainerSaveInput.TrainerInput prepareInput(Trainer trainer) {
        // Trainer
        var trainerNodes = List.of(TrainerSaveInput.TrainerNode.builder()
                .uuid(trainer.getUuid())
                .name(trainer.getName())
                .build());
        // Pokedex
        var pokedexNodes = List.of(TrainerSaveInput.PokedexNode.builder()
                .uuid(trainer.getPokedex().getUuid())
                .build());
        // PC
        var pcNodes = List.of(TrainerSaveInput.PCNode.builder()
                .uuid(trainer.getPc().getUuid())
                .build());


        // Pokedex Entries
        var entries = trainer.getPokedex().getEntries();
        var entryNodes = entries.stream()
                .map(n -> TrainerSaveInput.PokedexEntryNode.builder()
                        .uuid(n.getUuid())
                        .hasSeen(n.isHasSeen())
                        .owns(n.isOwns())
                        .description(n.getDescription())
                        .number(n.getNumber())
                        .build())
                .toList();

        // Boxes
        var boxes = trainer.getPc().getBoxes();
        var boxNodes = boxes.stream()
                .map(b -> TrainerSaveInput.BoxNode.builder()
                        .uuid(b.getUuid())
                        .name(b.getName())
                        .build())
                .toList();

        // Species
        var species = entries.stream()
                .map(PokedexEntry::getSpecies)
                .toList();
        var speciesNodes = species.stream()
                .map(s -> TrainerSaveInput.SpeciesNode.builder()
                        .uuid(s.getUuid())
                        .name(s.getName())
                        .type1(s.getType1())
                        .type2(s.getType2())
                        .build())
                .toList();


        // pokemon
        var pokemon = new ArrayList<>(trainer.getTeam());
        boxes.forEach(b -> pokemon.addAll(b.getPokemon()));
        var pokemonNodes = pokemon.stream()
                .map(p -> TrainerSaveInput.PokemonNode.builder()
                    .uuid(p.getUuid())
                    .nickname(p.getNickname())
                    .build())
                .toList();


        // moves
        var moves = new ArrayList<Move>();
        pokemon.forEach(p -> moves.addAll(p.getMoves()));
        species.stream()
                .flatMap(s -> s.getMoves().stream())
                .map(LearnMove::getMove)
                .forEach(moves::add);
        var moveNodes = moves.stream()
                .map(m -> TrainerSaveInput.MoveNode.builder()
                    .uuid(m.getUuid())
                    .name(m.getName())
                    .description(m.getDescription())
                    .attackPower(m.getAttackPower())
                    .accuracy(m.getAccuracy())
                    .effect(m.getEffect())
                    .type(m.getType())
                .build()).toList();

        // stats
        var stats = new ArrayList<Stats>();
        pokemon.forEach(p -> stats.add(p.getStats()));
        species.forEach(s -> stats.add(s.getBaseStats()));
        var statsNodes = stats.stream()
                .map(s -> TrainerSaveInput.StatsNode.builder()
                    .uuid(s.getUuid())
                    .hp(s.getHp())
                    .attack(s.getAttack())
                    .specialAttack(s.getSpecialAttack())
                    .defense(s.getDefense())
                    .specialDefense(s.getDefense())
                    .speed(s.getSpeed())
                    .build())
                .toList();

        // Items
        var items = new ArrayList<Item>();
        items.addAll(trainer.getInventory());
        items.addAll(trainer.getPc().getItemBox());
        items.addAll(pokemon.stream().map(Pokemon::getHeldItem).toList());
        var itemNodes = items.stream()
                .map(i -> TrainerSaveInput.ItemNode.builder()
                    .uuid(i.getUuid())
                    .name(i.getName())
                    .effect(i.getEffect())
                    .sellValue(i.getSellValue())
                    .build())
                .toList();

        var accessTo = List.of(new TrainerSaveInput.UUIDRelationship(trainer.getUuid(), trainer.getPc().getUuid()));
        var owns = List.of(new TrainerSaveInput.UUIDRelationship(trainer.getUuid(), trainer.getPokedex().getUuid()));
        var hasBox = boxes.stream()
                .map(b -> new TrainerSaveInput.UUIDRelationship(trainer.getPc().getUuid(), b.getUuid()))
                .toList();
        var pcContains = trainer.getPc().getItemBox().stream()
                .map(i -> new TrainerSaveInput.UUIDRelationship(trainer.getPc().getUuid(), i.getUuid()))
                .toList();
        var boxContains = boxes.stream()
                .flatMap(b -> b.getPokemon().stream()
                        .map(p -> new TrainerSaveInput.UUIDRelationship(b.getUuid(), p.getUuid())))
                .toList();
        var catalogs = entries.stream()
                .map(e -> new TrainerSaveInput.UUIDRelationship(trainer.getPokedex().getUuid(), e.getUuid()))
                .toList();
        var hasOnTeam = trainer.getTeam().stream()
                .map(p -> new TrainerSaveInput.UUIDRelationship(trainer.getUuid(), p.getUuid()))
                .toList();
        var hasItem = trainer.getInventory().stream()
                .map(i -> new TrainerSaveInput.UUIDRelationship(trainer.getUuid(), i.getUuid()))
                .toList();
        var belongsTo = pokemon.stream()
                .map(p -> new TrainerSaveInput.UUIDRelationship(p.getUuid(), p.getOwner().getUuid()))
                .toList();
        var holding = pokemon.stream()
                .map(p -> new TrainerSaveInput.UUIDRelationship(p.getUuid(), p.getHeldItem().getUuid()))
                .toList();
        var isA = pokemon.stream()
                .map(p -> new TrainerSaveInput.UUIDRelationship(p.getUuid(), p.getSpecies().getUuid()))
                .toList();
        var entryFor = entries.stream()
                .map(e -> new TrainerSaveInput.UUIDRelationship(e.getUuid(), e.getSpecies().getUuid()))
                .toList();
        var pokemonHasStats = pokemon.stream()
                .map(p -> new TrainerSaveInput.UUIDRelationship(p.getUuid(), p.getStats().getUuid()))
                .toList();
        var speciesHasStats = species.stream()
                .map(s -> new TrainerSaveInput.UUIDRelationship(s.getUuid(), s.getBaseStats().getUuid()))
                .toList();
        var hasMove = pokemon.stream()
                .flatMap(p -> p.getMoves().stream()
                        .map(m -> new TrainerSaveInput.UUIDRelationship(p.getUuid(), m.getUuid())))
                .toList();
        var evolvesInto = species.stream()
                .flatMap(s -> s.getEvolutions().stream()
                        .map(e -> new TrainerSaveInput.EvolutionRelationship(
                                s.getUuid(),
                                e.getEvolution().getUuid(),
                                TrainerSaveInput.EvolutionRelationshipProperties.builder()
                                        .evolutionType(e.getEvolutionType())
                                        .level(e.getLevel())
                                        .build())))
                .toList();
        var learnsMove = species.stream()
                .flatMap(s -> s.getMoves().stream()
                        .map(m -> new TrainerSaveInput.LearnMoveRelationship(
                                s.getUuid(),
                                m.getMove().getUuid(),
                                TrainerSaveInput.LearnMoveRelationshipProperties.builder()
                                        .level(m.getLevel())
                                        .build())))
                .toList();

        return new TrainerSaveInput.TrainerInput(
                trainerNodes,
                pcNodes,
                boxNodes,
                pokedexNodes,
                entryNodes,
                pokemonNodes,
                moveNodes,
                speciesNodes,
                statsNodes,
                itemNodes,
                owns,
                accessTo,
                hasOnTeam,
                hasItem,
                hasBox,
                pcContains,
                boxContains,
                hasMove,
                pokemonHasStats,
                speciesHasStats,
                isA,
                holding,
                catalogs,
                entryFor,
                belongsTo,
                evolvesInto,
                learnsMove);
    }

    private void upsertFullTrainer(TrainerSaveInput.TrainerInput input) {
        // nodes
        executeWithJson(TrainerSaveQuery.TrainerNode, input.getTrainers());
        executeWithJson(TrainerSaveQuery.PokedexNode, input.getPokedexes());
        executeWithJson(TrainerSaveQuery.PCNode, input.getPcs());
        executeWithJson(TrainerSaveQuery.ItemNode, input.getItems());
        executeWithJson(TrainerSaveQuery.BoxNode, input.getBoxes());
        executeWithJson(TrainerSaveQuery.PokedexEntryNode, input.getEntries());
        executeWithJson(TrainerSaveQuery.MoveNode, input.getMoves());
        executeWithJson(TrainerSaveQuery.PokemonNode, input.getPokemon());
        executeWithJson(TrainerSaveQuery.SpeciesNode, input.getSpecies());
        executeWithJson(TrainerSaveQuery.StatsNode, input.getStats());

        // relationships
        executeWithJson(TrainerSaveQuery.OwnsRelationship, input.getOwns());
        executeWithJson(TrainerSaveQuery.AccessToRelationship, input.getAccessTo());
        executeWithJson(TrainerSaveQuery.HasOnTeamRelationship, input.getHasOnTeam());
        executeWithJson(TrainerSaveQuery.HasItemRelationship, input.getHasItem());
        executeWithJson(TrainerSaveQuery.HasBoxRelationship, input.getHasBox());
        executeWithJson(TrainerSaveQuery.ContainsRelationship, input.getBoxContains());
        executeWithJson(TrainerSaveQuery.ContainsItemsRelationship, input.getPcContains());
        executeWithJson(TrainerSaveQuery.HasMoveRelationship, input.getHasMove());
        executeWithJson(TrainerSaveQuery.HasStatsRelationship, input.getPokemonHasStats());
        executeWithJson(TrainerSaveQuery.HasBaseStatsRelationship, input.getSpeciesHasStats());
        executeWithJson(TrainerSaveQuery.IsARelationship, input.getIsA());
        executeWithJson(TrainerSaveQuery.HoldingRelationship, input.getHolding());
        executeWithJson(TrainerSaveQuery.CatalogsRelationship, input.getCatalogs());
        executeWithJson(TrainerSaveQuery.EntryForRelationship, input.getEntryFor());
        executeWithJson(TrainerSaveQuery.BelongsToRelationship, input.getBelongsTo());
        executeWithJson(TrainerSaveQuery.EvolvesIntoRelationship, input.getEvolvesInto());
        executeWithJson(TrainerSaveQuery.LearnsMoveRelationship, input.getLearnMoves());
    }

    private void deleteDetached(String trainerUUID, TrainerSaveInput.TrainerInput input) {
        var pokemonOnTeam = input.getHasOnTeam().stream().map(TrainerSaveInput.UUIDRelationship::getTargetNode).toList();
        executeWithJson(TrainerSaveQuery.DeletePokemonFromTeam, trainerUUID, input);

    }

    // TODO javadoc
    private void executeWithJson(String query, List<?> data) {
        Lists.partition(data, BATCH_SIZE).forEach(b ->
            this.neo4jClient.query(query)
                    .bind(toJson(b)).to(TrainerSaveQuery.InputBindingName)
                    .fetch().all()
        );
    }

    // TODO javadoc
    private void executeWithJson(String query, String uuid, Object data) {
        this.neo4jClient.query(query)
                .bind(uuid).to(TrainerSaveQuery.UUIDBindingName)
                .bind(toJson(data)).to(TrainerSaveQuery.InputBindingName)
                .fetch().all();
    }

    /**
     * Serialize object to JSON. Only should apply to neo4j queries that take JSON as input.
     * @param toSerialize Probably one of the types in TrainerSaveInput.
     * @return Object as JSON string.
     */
    private String toJson(Object toSerialize) {
        try {
            return objectMapper.writeValueAsString(toSerialize);
        } catch (JsonProcessingException e) {
            throw new SerializationError(e, toSerialize);
        }
    }

    private static class TrainerSaveInput {
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class TrainerInput {
            private List<TrainerSaveInput.TrainerNode> trainers;
            private List<TrainerSaveInput.PCNode> pcs;
            private List<TrainerSaveInput.BoxNode> boxes;
            private List<TrainerSaveInput.PokedexNode> pokedexes;
            private List<TrainerSaveInput.PokedexEntryNode> entries;
            private List<TrainerSaveInput.PokemonNode> pokemon;
            private List<TrainerSaveInput.MoveNode> moves;
            private List<TrainerSaveInput.SpeciesNode> species;
            private List<TrainerSaveInput.StatsNode> stats;
            private List<TrainerSaveInput.ItemNode> items;

            private List<TrainerSaveInput.UUIDRelationship> owns;
            private List<TrainerSaveInput.UUIDRelationship> accessTo;
            private List<TrainerSaveInput.UUIDRelationship> hasOnTeam;
            private List<TrainerSaveInput.UUIDRelationship> hasItem;
            private List<TrainerSaveInput.UUIDRelationship> hasBox;
            private List<TrainerSaveInput.UUIDRelationship> pcContains;
            private List<TrainerSaveInput.UUIDRelationship> boxContains;

            private List<TrainerSaveInput.UUIDRelationship> hasMove;
            private List<TrainerSaveInput.UUIDRelationship> pokemonHasStats;
            private List<TrainerSaveInput.UUIDRelationship> speciesHasStats;

            private List<TrainerSaveInput.UUIDRelationship> isA;
            private List<TrainerSaveInput.UUIDRelationship> holding;
            private List<TrainerSaveInput.UUIDRelationship> catalogs;
            private List<TrainerSaveInput.UUIDRelationship> entryFor;
            private List<TrainerSaveInput.UUIDRelationship> belongsTo;
            private List<TrainerSaveInput.EvolutionRelationship> evolvesInto;
            private List<TrainerSaveInput.LearnMoveRelationship> learnMoves;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class TrainerNode {
            private String uuid;
            private String name;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class PCNode {
            private String uuid;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class PokedexNode {
            private String uuid;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class PokedexEntryNode {
            private String uuid;
            private boolean hasSeen;
            private boolean owns;
            private String description;
            private int number;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class PokemonNode {
            private String uuid;
            private String nickname;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class BoxNode {
            private String uuid;
            private String name;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class ItemNode {
            private String uuid;
            private String name;
            private int sellValue;
            private String effect;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class SpeciesNode {
            private String uuid;
            private String name;
            private Element type1;
            private Element type2;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class StatsNode {
            private String uuid;
            private int hp;
            private int attack;
            private int defense;
            private int specialAttack;
            private int specialDefense;
            private int speed;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class MoveNode {
            private String uuid;
            private String name;

            private String description;

            private int attackPower;

            private int accuracy;

            private String effect;

            private Element type;
        }


        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class UUIDRelationship {
            private String sourceNode;
            private String targetNode;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class UUIDToIDRelationship {
            private String sourceNode;
            private Long targetNode;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class IDToUUIDRelationship {
            private Long sourceNode;
            private String targetNode;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class EvolutionRelationship {
            private String sourceNode;
            private String targetNode;
            private EvolutionRelationshipProperties properties;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class EvolutionRelationshipProperties {
            private EvolutionType evolutionType;
            private int level;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class LearnMoveRelationship {
            private String sourceNode;
            private String targetNode;
            private LearnMoveRelationshipProperties properties;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder(toBuilder = true)
        static class LearnMoveRelationshipProperties {
            private int level;
        }
    }

    private static class TrainerSaveQuery {
        static final String InputBindingName = "input";
        static final String UUIDBindingName = "uuid";

        // TODO do we need to return at all here???
        static final String TrainerNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as trainer_node
            MERGE (t:Trainer{uuid:trainer_node.uuid}) SET t=properties(trainer_node)
            RETURN id(t) as id
        """;

        static final String PokedexNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as pokedex_node
            MERGE (p:Pokedex{uuid:pokedex_node.uuid}) SET p=properties(pokedex_node)
            RETURN id(p) as id
        """;

        static final String PCNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as pc_node
            MERGE (p:PC{uuid:pc_node.uuid}) SET p=properties(pc_node)
            RETURN id(p) as id
        """;

        static final String PokemonNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as pokemon_node
            MERGE (p:Pokemon{uuid:pokemon_node.uuid}) SET p=properties(pokemon_node)
            RETURN id(p) as id
        """;

        static final String PokedexEntryNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as entry_node
            MERGE (p:PokedexEntry{uuid:entry_node.uuid}) SET p=properties(entry_node)
            RETURN id(p) as id
        """;

        static final String SpeciesNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as species_node
            MERGE (s:Species{uuid:species_node.uuid}) SET s=properties(species_node)
            RETURN id(s) as id
        """;

        static final String ItemNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as item_node
            MERGE (i:Item{uuid:item_node.uuid}) SET i=properties(item_node)
            RETURN id(i) as id
        """;

        static final String MoveNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            WITH input
            UNWIND input as move_node
            MERGE (m:Move{uuid:move_node.uuid}) SET m=properties(move_node)
            RETURN id(m) as id
        """;

        static final String BoxNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as box_node
            MERGE (b:Box{uuid:box_node.uuid}) SET b=properties(box_node)
            RETURN id(b) as id
        """;

        static final String StatsNode = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input as stats_node
            MERGE (s:Stats{uuid:stats_node.uuid}) SET s=properties(stats_node)
            RETURN id(s) as id
        """;

        static final String HasOnTeamRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Trainer) WHERE a.uuid = relation.sourceNode
            MATCH (b: Pokemon) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HAS_ON_TEAM]->(b)
            RETURN id(r) AS id
        """;

        static final String OwnsRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Trainer) WHERE a.uuid = relation.sourceNode
            MATCH (b: Pokedex) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:OWNS]->(b)
            RETURN id(r) AS id
        """;

        static final String AccessToRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Trainer) WHERE a.uuid = relation.sourceNode
            MATCH (b: PC) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:ACCESS_TO]->(b)
            RETURN id(r) AS id
        """;

        static final String HasItemRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Trainer) WHERE a.uuid = relation.sourceNode
            MATCH (b: Item) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HAS_ITEM]->(b)
            RETURN id(r) AS id
        """;

        static final String HasBoxRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: PC) WHERE a.uuid = relation.sourceNode
            MATCH (b: Box) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HAS_BOX]->(b)
            RETURN id(r) AS id
        """;

        static final String ContainsItemsRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: PC) WHERE a.uuid = relation.sourceNode
            MATCH (b: Item) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:CONTAINS]->(b)
            RETURN id(r) AS id
        """;

        static final String CatalogsRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Pokedex) WHERE a.uuid = relation.sourceNode
            MATCH (b: PokedexEntry) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:CATALOGS]->(b)
            RETURN id(r) AS id
        """;

        static final String EntryForRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: PokedexEntry) WHERE a.uuid = relation.sourceNode
            MATCH (b: Species) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:ENTRY_FOR]->(b)
            RETURN id(r) AS id
        """;

        static final String IsARelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Pokemon) WHERE a.uuid = relation.sourceNode
            MATCH (b: Species) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:IS_A]->(b)
            RETURN id(r) AS id
        """;

        static final String HasBaseStatsRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Species) WHERE a.uuid = relation.sourceNode
            MATCH (b: Stats) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HAS_STATS]->(b)
            RETURN id(r) AS id
        """;

        static final String HasStatsRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Pokemon) WHERE a.uuid = relation.sourceNode
            MATCH (b: Stats) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HAS_STATS]->(b)
            RETURN id(r) AS id
        """;

        static final String HoldingRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Pokemon) WHERE a.uuid = relation.sourceNode
            MATCH (b: Item) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HOLDING]->(b)
            RETURN id(r) AS id
        """;

        static final String BelongsToRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Pokemon) WHERE a.uuid = relation.sourceNode
            MATCH (b: Trainer) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:BELONGS_TO]->(b)
            RETURN id(r) AS id
        """;

        static final String HasMoveRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Pokemon) WHERE a.uuid = relation.sourceNode
            MATCH (b: Move) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HAS_MOVE]->(b)
            RETURN id(r) AS id
        """;

        static final String ContainsRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Box) WHERE a.uuid = relation.sourceNode
            MATCH (b: Pokemon) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:CONTAINS]->(b)
            RETURN id(r) AS id
        """;

        static final String LearnsMoveRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Species) WHERE a.uuid = relation.sourceNode
            MATCH (b: Move) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HOLDING]->(b) SET r=relation.properties
            RETURN id(r) AS id
        """;

        static final String EvolvesIntoRelationship = """
            WITH apoc.convert.fromJsonList($input) AS input
            UNWIND input AS relation
            MATCH (a: Species) WHERE a.uuid = relation.sourceNode
            MATCH (b: Species) WHERE b.uuid = relation.targetNode
            MERGE (a)-[r:HOLDING]->(b) SET r=relation.properties
            RETURN id(r) AS id
        """;

        static final String DeletePokemonFromTeam = """     
            WITH apoc.convert.fromJsonList($input) AS input
            MATCH (t:Trailer)-->(p:Pokemon) where t.uuid = $uuid and NOT p.uuid in input
            DETACH DELETE p
        """;


    }


}
