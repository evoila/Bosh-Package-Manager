package de.evoila.bpm.controller

import de.evoila.bpm.entities.Package
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.service.PackageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.PagedResources
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@CrossOrigin(origins = ["http://localhost:4200"])
@RestController
class PackageRestController(
    val packageService: PackageService
) {

  @GetMapping(value = ["/rest/packages"])
  fun getAllPaginated(pageable: Pageable,
                      assembler: PagedResourcesAssembler<Package>,
                      principal: Principal?
  ): ResponseEntity<PagedResources<Resource<Package>>> {

    val page = packageService.getAllPackages(principal?.name, pageable)
    val responseHeaders = HttpHeaders()

    return ResponseEntity(assembler.toResource(page, linkTo(PackageRestController::class.java).slash("/products").withSelfRel()), responseHeaders, HttpStatus.OK)
  }



  private fun createLinkHeader(pagedResource: PagedResources<Resource<Package>>): String {

    val linkHeader = StringBuilder()

    linkHeader.append(buildLinkHeader(pagedResource.getLinks("first")[0].href, "first"))
    linkHeader.append(", ")
    linkHeader.append(buildLinkHeader(pagedResource.getLinks("next")[0].href, "next"))

    return linkHeader.toString()
  }

  fun buildLinkHeader(uri: String, rel: String): String {
    return "<$uri>; rel=\"$rel\""
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(PackageRestController::class.java)
  }
}