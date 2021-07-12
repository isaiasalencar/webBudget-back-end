package br.com.webbudget.controllers.administration

import br.com.webbudget.AbstractControllerTest
import br.com.webbudget.application.payloads.UserForm
import br.com.webbudget.application.payloads.UserView
import br.com.webbudget.domain.entities.configuration.Grant
import br.com.webbudget.domain.entities.configuration.User
import br.com.webbudget.infrastructure.repository.configuration.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID

class UserControllerTest : AbstractControllerTest() {

    @Value("classpath:/payloads/user/create-user.json")
    private lateinit var createUserJson: Resource

    @Value("classpath:/payloads/user/update-user.json")
    private lateinit var updateUserJson: Resource

    @Value("classpath:/payloads/user/invalid-user.json")
    private lateinit var invalidUserJson: Resource

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `should require proper authentication`() {
        mockMvc.get(ENDPOINT_URL) {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser
    fun `should create an user account`() {

        val payload = resourceAsString(createUserJson)

        mockMvc.post(ENDPOINT_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect {
            status { isCreated() }
        }

        val user = userRepository.findByEmail("user@webbudget.com.br")
        assertThat(user).isNotNull

        user?.let {
            assertThat(it.name).isEqualTo("User")
            assertThat(it.email).isEqualTo("user@webbudget.com.br")

            assertThat(passwordEncoder.matches("user", it.password)).isTrue

            assertThat(it.id).isNotNull
            assertThat(it.externalId).isNotNull
            assertThat(it.active).isFalse

            assertThat(it.grants).isNotEmpty

            val roles = it.grants!!
                .map { grant -> grant.authority.name }
                .toCollection(mutableListOf())

            assertThat(roles).containsExactlyInAnyOrder("REGISTRATION")
        }
    }

    @Test
    @WithMockUser
    fun `should fail if required fields are not present`() {

        val payload = resourceAsString(invalidUserJson)
        val requiredFields = arrayOf("name", "email", "password", "roles")

        mockMvc.post(ENDPOINT_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = payload
        }.andExpect {
            status { isUnprocessableEntity() }
        }.andExpect {
            jsonPath("\$.violations[*].property", containsInAnyOrder(*requiredFields))
        }
    }

    @Test
    @WithMockUser
    fun `should find an user by id`() {

        val userId = UUID.fromString("6706a395-6690-4bad-948a-5c3c823e93d2")

        val result = mockMvc.get("$ENDPOINT_URL/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }.andReturn()

        val found = jsonToObject(result.response.contentAsString, UserForm::class.java)

        assertThat(found).isNotNull
        assertThat(found.id).isEqualTo(userId)
        assertThat(found.roles).containsExactlyInAnyOrder("")
    }

    @Test
    @WithMockUser
    fun `should get no content if user does not exists`() {

        val userId = UUID.randomUUID()

        mockMvc.get("$ENDPOINT_URL/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNoContent() }
        }

        val found = userRepository.findByExternalId(userId)
        assertThat(found).isNull()
    }

    @Test
    @WithMockUser
    fun `should delete an user account`() {

        val user = createUser("To be deleted", "tobedeleted@test.com", "password")

        mockMvc.delete("$ENDPOINT_URL/${user.externalId}") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        val found = userRepository.findByExternalId(user.externalId!!)
        assertThat(found).isNull()
    }

    @Test
    @WithMockUser
    fun `should get no content when delete an unknown user account`() {

        val userId = UUID.randomUUID()

        mockMvc.delete("$ENDPOINT_URL/$userId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNoContent() }
        }

        val found = userRepository.findByExternalId(userId)
        assertThat(found).isNull()
    }

    @Test
    @WithMockUser
    fun `should find users using filters`() {

        val parameters = mapOf(
            "page" to "0",
            "size" to "1",
            "name" to "Administrador",
            "email" to "admin@webbudget.com.br",
            "active" to "true"
        )

        val result = mockMvc.get(ENDPOINT_URL) {
            contentType = MediaType.APPLICATION_JSON
            params = fromMap(parameters)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        val users = jsonToObject(result.response.contentAsString, "/content", UserView::class.java)

        assertThat(users)
            .hasSize(1)
            .extracting("id", "name", "email")
            .contains(tuple("6706a395-6690-4bad-948a-5c3c823e93d2", "Administrador", "admin@webbudget.com.br"))
    }

    private fun createUser(
        name: String,
        email: String,
        password: String,
        active: Boolean = true,
        roles: List<Grant> = listOf()
    ): User {
        return userRepository.save(User(name, email, password, active, roles))
    }

    companion object {
        private const val ENDPOINT_URL = "/api/users/"
    }
}
