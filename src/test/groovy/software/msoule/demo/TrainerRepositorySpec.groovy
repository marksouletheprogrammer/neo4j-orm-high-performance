package software.msoule.demo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class TrainerRepositorySpec extends Specification {

    @Autowired
    private TrainerRepository trainerRepository

    void "save ash"() {
        given:
            def ash = Trainer.builder().name("Ash").build()
        when:
            def result = trainerRepository.save(ash)
        then:
            result
    }
}
