package de.evoila.bpm.controller

import de.evoila.bpm.entities.Package
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.service.PackageService
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
  fun getAllPaginated(pageable: Pageable, assembler: PagedResourcesAssembler<Package>, principal: Principal?): ResponseEntity<PagedResources<Resource<Package>>> {

    val page = packageService.getAllPackages(principal?.name, pageable)

    // Works until now without this
    //   val pr = assembler.toResource(page,
    //       linkTo(PackageRestController::class.java).slash("/rest/packages").withSelfRel())
    val responseHeaders = HttpHeaders()
    //  responseHeaders.add("Link", createLinkHeader(pr))


    return ResponseEntity(assembler.toResource(page, linkTo(PackageRestController::class.java).slash("/products").withSelfRel()), responseHeaders, HttpStatus.OK)
  }

  private fun createLinkHeader(pagedResource: PagedResources<Resource<Package>>): String {

    val linkHeader = StringBuilder()

    linkHeader.append(buildLinkHeader(pagedResource.getLinks("first")[0].href, "first"))
    linkHeader.append(", ")
    linkHeader.append(buildLinkHeader(pagedResource.getLinks("next")[0].href, "next"))

    return linkHeader.toString()
  }

  @GetMapping("/rest/packages/search")
  fun searchPackageByVendorNameVersion(
      @RequestParam(value = "vendor") vendor: String,
      @RequestParam(value = "name") name: String,
      @RequestParam(value = "version") version: String,
      principal: Principal?): ResponseEntity<Any> = try {

    val packageBody = packageService.accessPackage(vendor, name, version, principal?.name)

    log.info("Exposing package information for '$name:$version by $vendor'")

    ResponseEntity.ok(packageBody)
  } catch (e: PackageNotFoundException) {
    ResponseEntity.notFound().build()
  }


  fun buildLinkHeader(uri: String, rel: String): String {
    return "<$uri>; rel=\"$rel\""
  }

  companion object {
    val log = LoggerFactory.getLogger(PackageRestController::class.java)
  }
}