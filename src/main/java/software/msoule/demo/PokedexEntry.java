package software.msoule.demo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
public class PokedexEntry {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;
    private boolean hasSeen;
    private boolean owns;

    private String description;
    private int number;

    @OneToOne
    @JoinColumn(name = "species_id")
    private Species species;


}
