package com.altruist.account

import com.altruist.core.snippets.AccountSnippetsFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import static org.hamcrest.Matchers.containsString
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [AccountCtrl])
@AutoConfigureRestDocs
class AccountCtrlTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    AccountSrv mockAccountSrv

    private AccountSnippetsFactory snippetPathFactory = new AccountSnippetsFactory()

    def "Should accept account requests"() {
        given: "an account request"
        AccountDto req = new AccountDto(
                username: "username123",
                email: "email@example.com",
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )
        UUID expectedId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "the request is processed"
        1 * mockAccountSrv.createAccount(req) >> expectedId

        and: "a Created response is returned"
        results.andExpect(status().isCreated())

        and: "the order ID is returned"
        results.andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/accounts/$expectedId")))
        results.andExpect(content().json("""{"id":"$expectedId"}"""))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-success'),
                requestFields(
                        fieldWithPath("username").description("Required. Account's username"),
                        fieldWithPath("email").description("Required. Account's email"),
                        fieldWithPath("name").description("Name"),
                        fieldWithPath("street").description("Street"),
                        fieldWithPath("city").description("City"),
                        fieldWithPath("state").description("Two letters State code"),
                        fieldWithPath("zipcode").description("Postal code")),
                responseFields(
                        fieldWithPath("id").description("Id of the Account"))))
    }

    @Unroll
    def "Should validate for missing field username=#username, email=#email"() {
        given: "an account request"
        AccountDto req = new AccountDto(
                username: username,
                email: email,
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "response is conflict"
        results.andExpect(status().isBadRequest())

        and: "expect an error message"
        results.andExpect(jsonPath(field).value(error))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-bad-requests')))

        where:
        username      | email               | field        | error
        null          | "email@example.com" | '$.username' | "Username is mandatory"
        "username123" | null                | '$.email'    | "Email is mandatory"
        "username123" | "emailexample.com"  | '$.email'    | "Must be a valid email"
    }

    def "Should validate existing email"() {
        given: "an account request"
        String email = "email@example.com"
        AccountDto req = new AccountDto(
                username: "username123",
                email: email,
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "the request is processed"
        1 * mockAccountSrv.existsEmail(email) >> true

        and: "expect conflict status"
        results.andExpect(status().isConflict())

        and: "expect an error message"
        results.andExpect(jsonPath('$.error').value("Email already exists"))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-conflict-existing-email')))
    }

    def "Should validate existing user"() {
        given: "an account request"
        String username = "user123"
        AccountDto req = new AccountDto(
                username: username,
                email: "email@example.com",
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .header("Accept-Version", "1.0.0")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "the request is processed"
        1 * mockAccountSrv.existsUsername(username) >> true

        and: "expect conflict status"
        results.andExpect(status().isConflict())

        and: "expect an error message"
        results.andExpect(jsonPath('$.error').value("Username already exists"))

        and: "generate docs"
        results.andDo(document(snippetPathFactory.create("1.0.0", 'post-conflict-existing-user')))
    }

    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        AccountSrv accountSrv() {
            factory.Mock(AccountSrv)
        }

        @Bean
        AccountRepo accountRepo() {
            factory.Mock(AccountRepo)
        }
    }
}
