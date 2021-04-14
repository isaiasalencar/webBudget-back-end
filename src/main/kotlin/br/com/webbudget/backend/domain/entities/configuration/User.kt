package br.com.webbudget.backend.domain.entities.configuration

import br.com.webbudget.backend.domain.entities.PersistentEntity
import br.com.webbudget.backend.infrastructure.config.DefaultSchemas.CONFIGURATION
import javax.persistence.CascadeType.REMOVE
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "users", schema = CONFIGURATION)
class User(
    @Column(name = "name", length = 150, nullable = false)
    val name: String,
    @Column(name = "email", length = 150, nullable = false)
    val email: String,
    @Column(name = "password", nullable = false)
    val password: String,
    @Column(name = "active", nullable = false)
    val active: Boolean,
    @OneToMany(mappedBy = "user", fetch = EAGER, cascade = [REMOVE])
    val grants: List<Grant>
) : PersistentEntity<Long>()