package com.sena.VotaApp

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Entity
@Table
class Candidate (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var document: String,
    var name: String,
    var voteCount: Long,
    var birthDay: LocalDateTime,
    var email: String,
    var position: Long,
    @ManyToOne
    var party: Party,
){
    constructor() : this(
        document = "",
        name = "",
        voteCount = 0,
        birthDay = LocalDateTime.now(),
        email = "",
        position = 0,
        party = Party()
    )
}

class CandidateRequest(
    var document: String?,
    var name: String?,
    var voteCount: Long?,
    var birthDay: LocalDateTime?,
    var email: String?,
    var position: Long?,
    var partyId: Long?,
)

interface CandidateRepository : JpaRepository<Candidate, Long>

@Service
class CandidateService(
    private val repository: CandidateRepository,
    val partyRepository: PartyRepository
){

    fun findAll(): MutableList<Candidate> = repository.findAll()

    fun findById(id: Long): Candidate = repository.findById(id).orElseThrow{
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    fun new(body: CandidateRequest): Candidate {
        val party = if(body.partyId != null) partyRepository.findById(body.partyId!!).orElseThrow{
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        } else null

        return repository.save(Candidate(null, body.document!!, body.name!!, body.voteCount!!, body.birthDay!!, body.email!!, body.position!!, party!!))
    }

    fun update(body: CandidateRequest, id: Long): Candidate {
        val entity = findById(id)

        val party = if(body.partyId != null) partyRepository.findById(body.partyId!!).orElseThrow{
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        } else null

        body.document?.let { entity.document = it }
        body.name?.let { entity.name = it }
        body.voteCount?.let { entity.voteCount = it }
        body.birthDay?.let { entity.birthDay = it }
        body.email?.let { entity.email = it }
        body.position?.let { entity.position = it }
        party?.let { entity.party = it }

        return repository.save(entity)
    }

    fun delete(id: Long) {
        repository.deleteById(id)
    }
}

@RestController
@CrossOrigin
@RequestMapping("/candidate")
class CandidateController(val service: CandidateService){

    @GetMapping
    fun all(): MutableList<Candidate> = service.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Candidate = if(id > 0L) service.findById(id) else Candidate()

    @PostMapping
    fun new(@RequestBody body: CandidateRequest): Candidate = service.new(body)

    @PutMapping("/{id}")
    fun update(@RequestBody body: CandidateRequest, @PathVariable id: Long): Candidate = service.update(body, id)


    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}