package software.msoule.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PokedexEntryRepository extends JpaRepository<PokedexEntry, Long> {

}
