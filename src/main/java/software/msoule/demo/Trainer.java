package software.msoule.demo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
public class Trainer {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private String name;

    @OneToMany
    @JoinColumn(name = "team_id")
    private List<Pokemon> team;

    @OneToMany
    @JoinColumn(name = "inventory_id")
    private List<Item> inventory;

    @OneToOne
    @JoinColumn(name = "pokedex_id")
    private Pokedex pokedex;

    @OneToOne
    @JoinColumn(name = "pc_id")
    private PC pc;


}
