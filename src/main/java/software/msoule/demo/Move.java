package software.msoule.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
public class Move {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;

    private String name;

    private String description;

    private int attackPower;

    private int accuracy;

    private String effect;

    private Element type;
}
