package br.pucpr.authserver.roles

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class RoleService(val repository: RoleRepository) {
    fun insert(role: Role): Role? {
        role.name = role.name.uppercase()
        if (repository.findByName(role.name) != null) {
            return null
        }
        return repository.save(role)
    }

    fun findAll() = repository.findAll(Sort.by("name").ascending())
}