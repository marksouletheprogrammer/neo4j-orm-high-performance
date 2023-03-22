package software.msoule.demo

import spock.lang.Specification

class Neo4jOrmHighPerformanceApplicationSpec extends Specification {

    void "contextLoads"() {
        given:
            def s = "test"
        when:
            s = s.toUpperCase()
        then:
            s == "TEST"
    }
}
