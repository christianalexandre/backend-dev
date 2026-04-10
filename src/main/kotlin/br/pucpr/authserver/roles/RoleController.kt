package br.pucpr.authserver.roles

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/roles")
class RoleController(val service: RoleService) {
    @PostMapping
    fun insert(
        @RequestBody role: Role
    ) = service.insert(role)
                ?.let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
                ?: ResponseEntity.badRequest().build()

    @GetMapping
    fun list() = service.findAll()
        .let { ResponseEntity.ok(it) }
}