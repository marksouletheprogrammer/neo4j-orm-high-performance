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
public class PC {

    @Id
    @GeneratedValue
    private Long id;

    private String uuid;

    @OneToMany
    @JoinColumn(name = "box_id")
    private List<Box> boxes;

    @OneToMany
    @JoinColumn(name = "item_storage_id")
    private List<Item> itemBox;
}
