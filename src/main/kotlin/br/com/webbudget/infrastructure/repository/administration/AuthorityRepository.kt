package br.com.webbudget.infrastructure.repository.administration

import br.com.webbudget.domain.entities.administration.Authority
import br.com.webbudget.infrastructure.repository.DefaultRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorityRepository : DefaultRepository<Authority> {

    fun findByName(name: String): Authority?
}
