package br.pucpr.authserver.users

import jakarta.persistence.*

@Entity
@Table(name = "UserTable")
class User (
    @Id @GeneratedValue()
    var id: Long? = null,

    @Column(nullable = false)
    var email: String,

    var password: String,
    var name: String = "",
)