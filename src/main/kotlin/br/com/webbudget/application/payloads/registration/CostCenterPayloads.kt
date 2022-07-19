package br.com.webbudget.application.payloads.registration

import br.com.webbudget.application.payloads.support.SpecificationSupport
import br.com.webbudget.application.payloads.support.StatusFilter
import br.com.webbudget.domain.entities.registration.CostCenter
import java.util.UUID
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.validation.constraints.NotBlank

data class CostCenterForm(
    val id: UUID?,
    val active: Boolean = true,
    @field:NotBlank(message = "cost-center.errors.description-is-blank")
    val description: String,
)

data class CostCenterView(
    val id: UUID,
    val active: Boolean,
    val description: String
)

data class CostCenterFilter(
    val filter: String?,
    val status: StatusFilter?
) : SpecificationSupport<CostCenter> {

    override fun buildPredicates(root: Root<CostCenter>, query: CriteriaQuery<*>, builder: CriteriaBuilder): List<Predicate> {

        val predicates = mutableListOf<Predicate>()

        if (!filter.isNullOrBlank()) {
            predicates.add(builder.like(builder.lower(root.get("description")), likeIgnoringCase(filter)))
        }

        if (status != null && status != StatusFilter.ALL) {
            predicates.add(builder.equal(root.get<Boolean>("active"), status.value))
        }

        return predicates
    }
}
