package software.msoule.demo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
public class Species {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private String name;

    @OneToOne
    @JoinColumn(name = "stats_id")
    private Stats baseStats;

    private Element type1;

    private Element type2;

    @OneToMany
    @JoinColumn(name = "learn_move_id")
    private List<Move> moves;

    @OneToMany
    @JoinColumn(name = "species_id")
    private Set<Species> evolutions;

}
