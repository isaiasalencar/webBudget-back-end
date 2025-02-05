package br.com.webbudget.application.mappers.configuration

import br.com.webbudget.application.mappers.MappingConfiguration
import br.com.webbudget.application.payloads.configuration.UserForm
import br.com.webbudget.application.payloads.configuration.UserView
import br.com.webbudget.domain.entities.administration.Grant
import br.com.webbudget.domain.entities.administration.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.springframework.stereotype.Component

@Component
@Mapper(config = MappingConfiguration::class)
interface UserMapper {

    @Mappings(
        Mapping(source = "externalId", target = "id"),
        Mapping(source = "grants", target = "authorities")
    )
    fun map(user: User): UserView

    fun map(userForm: UserForm): User

    companion object {
        @JvmStatic
        fun grantsToAuthorities(grants: List<Grant>): List<String> {
            return grants
                .map { it.authority }
                .map { it.name }
                .toCollection(arrayListOf())
        }
    }
}