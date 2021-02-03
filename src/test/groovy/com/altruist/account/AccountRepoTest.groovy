package com.altruist.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepoTest extends Specification {
    @Autowired
    AccountRepo repo

    @Shared
    Account account

    def setup() {
        repo.deleteAll()
    }

    def "Inserts an account with address"() {
        given: "an account with address"
        account = new Account(
                username: "username123",
                email: "email@example.com",
                address: new Address(
                    name: "Some Name",
                    street: "Some street",
                    city: "Some city",
                    state: "CA",
                    zipcode: 99999
                )
        )

        when:
        repo.save(account)

        then: "the account id and address id are returned"
        account.accountUuid
        account.address.addressUuid
    }
}
