package br.pucpr.authserver.users

import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(val repository: UserRepository) {
    fun insert(user: User): User? {
        if (repository.findByEmail(user.email) != null) {
            return null
        }
        return repository.save(user)
    }

    fun findAll(dir: SortDir = SortDir.ASC): List<User> = when (dir) {
        SortDir.ASC -> repository.findAll(Sort.by("name").ascending())
        SortDir.DESC -> repository.findAll(Sort.by("name").descending())
    }

    fun findByIdOrNull(id: Long) = repository.findByIdOrNull(id)

    fun delete(id: Long): Boolean {
        val user = findByIdOrNull(id) ?: return false
        repository.delete(user)
        return true
    }

}