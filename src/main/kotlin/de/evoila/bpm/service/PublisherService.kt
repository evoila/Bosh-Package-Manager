package de.evoila.bpm.service

import de.evoila.bpm.custom.elasticsearch.repositories.CustomUserRepository
import de.evoila.bpm.custom.elasticsearch.repositories.CustomPublisherRepository
import de.evoila.bpm.entities.Publisher
import de.evoila.bpm.exceptions.UnauthorizedException
import de.evoila.bpm.exceptions.UserNotFoundException
import de.evoila.bpm.exceptions.PublisherNotFoundException
import org.springframework.stereotype.Service

@Service
class PublisherService(
    private val customPublisherRepository: CustomPublisherRepository,
    private val userRepository: CustomUserRepository
) {

  fun isMemberOf(username: String, publisherName: String): Boolean = customPublisherRepository.findByName(publisherName)?.let {
    return it.isMember(username)
  } ?: false

  fun createNewPublisher(name: String, creator: String) {
    customPublisherRepository.findByName(name)?.let { throw IllegalArgumentException("$name already exists") }
    customPublisherRepository.save(Publisher(
        name = name,
        members = setOf(creator)
    ))
  }

  fun addMemberToPublisher(admin: String, publisherName: String, email: String) {
    val user = userRepository.findByEmail(email)
        ?: throw UserNotFoundException("No user could be found for this email.")
    val vendor = customPublisherRepository.findByName(publisherName)
        ?: throw PublisherNotFoundException("The publisher could not be found")

    if (vendor.isMember(admin)) {
      val updatedPublisher = vendor.copy(members = vendor.members.plus(user.id))

      customPublisherRepository.save(updatedPublisher)
    } else {
      throw UnauthorizedException("User is not a member of $publisherName")
    }
  }

  fun publishersForUsers(name: String): List<Publisher> {
    return customPublisherRepository.memberOfByName(name)
  }

  fun findByName(name: String): Publisher? {
    return customPublisherRepository.findByName(name)
  }
}