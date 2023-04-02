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
public class Pokedex {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;

    @OneToMany
    @JoinColumn(name = "entry_id")
    private List<PokedexEntry> entries;
}
