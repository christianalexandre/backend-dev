package br.pucpr.authserver.users

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(val service: UserService) {
    @GetMapping
    fun list(@RequestParam sortDir: String? = null, @RequestParam role: String? = null) =
        if (role != null) {
            val users = service.findByRole(role)
            ResponseEntity.ok(users)
        } else {
            SortDir.findOrNull(sortDir ?: "ASC")
                ?.let { service.findAll(it) }
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity.badRequest().build()
        }

    @PostMapping
    fun insert(@RequestBody user: User) =
        service.insert(user)
            ?.let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
            ?: ResponseEntity.badRequest().build()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) =
        service.findByIdOrNull(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (service.delete(id)) ResponseEntity.ok().build()
        else ResponseEntity.notFound().build()

    @PutMapping("/{id}/roles/{role}")
    fun grant(@PathVariable id: Long, @PathVariable role: String): ResponseEntity<Void> =
        service.addRole(id, role)
            ?.let { if (it) ResponseEntity.ok().build() else ResponseEntity.noContent().build() }
            ?: ResponseEntity.badRequest().build()
}