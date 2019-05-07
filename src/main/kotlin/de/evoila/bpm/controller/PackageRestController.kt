package de.evoila.bpm.controller

import de.evoila.bpm.entities.Package
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
import org.springframework.web.bind.annotation.*
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

    return ResponseEntity(assembler.toResource(page, linkTo(PackageRestController::class.java).slash("/packages").withSelfRel()), responseHeaders, HttpStatus.OK)
  }

  @GetMapping(value = ["/rest/packages/{publisher}"])
  fun searchByPublisher(
      pageable: Pageable,
      @PathVariable(value = "publisher") publisher: String,
      assembler: PagedResourcesAssembler<Package>,
      principal: Principal?
  ): ResponseEntity<PagedResources<Resource<Package>>> {
    val page = packageService.getPackagesByPublisher(principal?.name, pageable, publisher)
    val responseHeaders = HttpHeaders()

    return ResponseEntity(assembler.toResource(page, linkTo(PackageRestController::class.java).slash("/packages").withSelfRel()), responseHeaders, HttpStatus.OK)
  }

  @GetMapping(value = ["/rest/packages/{publisher}/{name}"])
  fun searchByPublisherAndName(
      pageable: Pageable,
      @PathVariable(value = "publisher") publisher: String,
      @PathVariable(value = "name") name: String,
      assembler: PagedResourcesAssembler<Package>,
      principal: Principal?
  ): ResponseEntity<PagedResources<Resource<Package>>> {
    val page = packageService.getPackagesByPublisherAndName(principal?.name, pageable, publisher, name)
    val responseHeaders = HttpHeaders()

    return ResponseEntity(assembler.toResource(page, linkTo(PackageRestController::class.java).slash("/packages").withSelfRel()), responseHeaders, HttpStatus.OK)
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(PackageRestController::class.java)
  }
}