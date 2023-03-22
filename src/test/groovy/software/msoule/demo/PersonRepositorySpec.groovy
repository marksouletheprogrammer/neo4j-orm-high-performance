package software.msoule.demo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class PersonRepositorySpec extends Specification {

    @Autowired
    private PersonRepository personRepository

    void "save mark"() {
        given:
            def mark = new Person("mark")
        when:
            def result = personRepository.save(mark)
        then:
            result
    }
}
