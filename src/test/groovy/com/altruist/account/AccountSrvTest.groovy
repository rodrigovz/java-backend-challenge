package com.altruist.account

import spock.lang.Specification
import spock.lang.Unroll

class AccountSrvTest extends Specification {
    AccountRepo mockAccountRepo = Mock()
    AccountSrv srv = new AccountSrv(mockAccountRepo)

    @Unroll
    def "Should validate for missing account field #field"() {
        given: "an account missing fields"
        AccountDto account = new AccountDto(
                username: "username123",
                email: "email@example.com",
        )
        account[field] = null

        when:
        srv.createAccount(account)

        then:
        thrown(NullPointerException)

        where:
        field << ["username", "email"]
    }

    def "Should validate for missing address field #field"() {
        given: "an address missing fields"
        AccountDto account = new AccountDto(
                username: "username123",
                email: "email@example.com",
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )
        account[field] = null

        when:
        srv.createAccount(account)

        then:
        thrown(NullPointerException)

        where:
        field << ["name", "street", "city", "state"]
    }

    def "Should validate for missing address field zipcode"() {
        given: "an address missing zipcode"
        AccountDto account = new AccountDto(
                username: "username123",
                email: "email@example.com",
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA"
        )

        when:
        srv.createAccount(account)

        then:
        thrown(NumberFormatException)
    }

    def "Should save account and address"() {
        given: "an account"
        AccountDto account = new AccountDto(
                username: "username123",
                email: "email@example.com",
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )

        UUID expectedAccountId = UUID.randomUUID()

        when:
        srv.createAccount(account)

        then: "the account and address are saved"
        1 * mockAccountRepo.save(_) >> { Account arg ->
            with(arg){
                username == account.username
                email == account.email
                address.name == account.name
                address.street == account.street
                address.city == account.city
                address.state.toString() == account.state
                address.zipcode == account.zipcode as Integer
            }

            arg.accountUuid = expectedAccountId
            arg
        }
    }
}
