package br.com.webbudget.application.converters

import br.com.webbudget.application.payloads.UserResponse
import br.com.webbudget.domain.entities.configuration.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.core.convert.converter.Converter

@Mapper(config = MappingConfiguration::class)
interface UserToUserResponseConverter : Converter<User, UserResponse> {

    @Mapping(source = "externalId", target = "id")
    override fun convert(user: User): UserResponse?
}
