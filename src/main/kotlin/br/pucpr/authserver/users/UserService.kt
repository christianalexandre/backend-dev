package br.pucpr.authserver.users

import br.pucpr.authserver.roles.RoleRepository
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    val repository: UserRepository,
    val roleRepository: RoleRepository
) {
    fun insert(user: User): User? {
        if (repository.findByEmail(user.email) != null) {
            return null
        }
        return repository.save(user)
    }

    fun findAll(dir: SortDir = SortDir.ASC) = when (dir) {
        SortDir.ASC -> repository.findAll(Sort.by("name").ascending())
        SortDir.DESC -> repository.findAll(Sort.by("name").descending())
    }

    fun findByIdOrNull(id: Long) = repository.findByIdOrNull(id)

    fun delete(id: Long): Boolean? {
        val user = findByIdOrNull(id) ?: return false
        if (user.isAdmin() && repository.findByRole("ADMIN").size == 1) {
            return null
        }
        repository.delete(user)
        return true
    }

    fun findByRole(role: String) = repository.findByRole(role.uppercase())

    fun addRole(id: Long, roleName: String): Boolean? {
        val upperRole = roleName.uppercase()
        val user = findByIdOrNull(id) ?: return null
        if (user.roles.any { it.name == upperRole }) return false

        val role = roleRepository.findByName(upperRole) ?: return null

        user.roles.add(role)
        repository.save(user)
        return true
    }
}