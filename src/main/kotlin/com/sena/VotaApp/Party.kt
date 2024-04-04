package com.sena.VotaApp

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Entity
@Table
class Party (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,

    @OneToMany(mappedBy = "party")
    @JsonIgnore
    val candidates: List<Candidate> = mutableListOf()
){
    constructor() : this(
        name = ""
    )
}

interface PartyRepository : JpaRepository<Party, Long>

@Service
class PartyService(private val repository: PartyRepository){
    fun findAll(): MutableList<Party> = repository.findAll()

    fun findById(id: Long): Party = repository.findById(id).orElseThrow{
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    fun new(body: Party): Party {
        body.id = null
        return repository.save(body)
    }

    fun update(body: Party, id: Long): Party {
        val entity = findById(id)

        entity.name = body.name

        return repository.save(entity)
    }

    fun delete(id: Long) {
        repository.deleteById(id)
    }
}

@RestController
@CrossOrigin
@RequestMapping("/party")
class PartyController(val service: PartyService){

    @GetMapping
    fun all(): MutableList<Party> = service.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Party = if(id > 0L) service.findById(id) else Party()

    @PostMapping
    fun new(@RequestBody body: Party): Party = service.new(body)

    @PutMapping("/{id}")
    fun update(@RequestBody body: Party, @PathVariable id: Long): Party = service.update(body, id)


    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}