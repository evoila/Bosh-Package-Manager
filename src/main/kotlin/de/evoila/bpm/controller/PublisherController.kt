package de.evoila.bpm.controller

import de.evoila.bpm.exceptions.UnauthorizedException
import de.evoila.bpm.service.PublisherService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
class PublisherController(
    val publisherService: PublisherService) {

  @PostMapping(value = ["publishers"])
  fun createNewPublisher(
      @RequestParam("name") name: String,
      principal: Principal
  ): ResponseEntity<Any> = try {
    publisherService.createNewPublisher(name, principal.name)

    ResponseEntity.status(HttpStatus.CREATED).build()
  } catch (e: Exception) {
    e.printStackTrace()

    ResponseEntity.status(HttpStatus.CONFLICT).build()
  }

  @PatchMapping(value = ["publishers/add-member"])
  fun addNewMember(@RequestParam(value = "publisher") publisher: String,
                   @RequestParam(value = "email") email: String,
                   principal: Principal
  ): ResponseEntity<Any> = try {
    publisherService.addMemberToPublisher(
        admin = principal.name,
        publisherName = publisher,
        email = email
    )

    ResponseEntity.status(HttpStatus.ACCEPTED).body("Added $email to $publisher")
  } catch (e: UnauthorizedException) {
    log.error(e.message, e)

    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
  } catch (e: Exception) {
    log.error(e.message, e)

    ResponseEntity.badRequest().body(e.message)
  }

  @GetMapping(value = ["publishers/member-of"])
  fun memberOf(principal: Principal?): ResponseEntity<Any> =
      principal?.let {
        val publisher = publisherService.publishersForUsers(principal.name)
        ResponseEntity.ok<Any>(publisher)
      } ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

  companion object {
    val log: Logger = LoggerFactory.getLogger(PublisherController::class.java)
  }
}