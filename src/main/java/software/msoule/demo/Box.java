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
public class Box {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;

    private String name;

    @OneToMany
    @JoinColumn(name = "pokemon_box_id")
    private List<Pokemon> pokemon;
}
