package br.com.webbudget.backend.controllers

import br.com.webbudget.backend.AbstractControllerTest
import br.com.webbudget.backend.application.payloads.Credential
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

class AuthenticationControllerTest : AbstractControllerTest() {

    @Test
    fun `should authenticate`() {
        this.mockMvc.post("$ENDPOINT_URL/login") {
            contentType = MediaType.APPLICATION_JSON
            content = toJson(Credential("admin@webbudget.com.br", "admin"))
        }.andExpect {
            status { isOk() }
        }.andExpect {
            jsonPath("$.token", notNullValue())
        }
    }

    companion object {
        private const val ENDPOINT_URL = "/authentication/"
    }
}