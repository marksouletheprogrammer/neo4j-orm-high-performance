package software.msoule.demo;

import java.util.List;

/**
 * Custom repository with custom methods that have different performance characteristics than the default save.
 */
public interface TrainerCustomRepository {

    void saveTrainer(Trainer trainer);

    /**
     * Save a projection of Trainer. Included as an example.
     */
    TrainerTeamProjection saveProjection(Trainer trainer);

    /**
     * Projection of a Trainer that only includes trainer and team. Saves on a trainer will be scoped to only the nodes,
     * relationships, and fields projected here.
     */
    interface TrainerTeamProjection {
        Long getId();
        String getUuid();
        String getName();
        List<PokemonProjection> getTeam();
    }

    interface PokemonProjection {
        Long getId();
        String getUuid();
        String getNickname();
    }
}
