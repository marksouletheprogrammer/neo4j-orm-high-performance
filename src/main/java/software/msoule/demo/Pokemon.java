package software.msoule.demo;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
public class Pokemon {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private String nickname;

    @OneToMany
    @JoinColumn(name = "has_move_id")
    private List<Move> moves;

    @OneToOne
    @JoinColumn(name = "stats_id")
    private Stats stats;

    @ManyToOne
    @JoinColumn(name = "species_id")
    private Species species;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer owner;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item heldItem;

}
