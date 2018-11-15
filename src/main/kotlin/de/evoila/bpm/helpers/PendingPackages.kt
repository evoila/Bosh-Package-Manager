package de.evoila.bpm.helpers

import de.evoila.bpm.entities.Package
import org.slf4j.LoggerFactory
import java.time.Instant

class PendingPackages(
    private var allowedAge: Long
) {

  private var map = emptyMap<String, Package>()
    @Synchronized get
    @Synchronized set

  fun cleanUp() {

    log.info("Starting clean up.")

    if (map.isNotEmpty()) {

      map = map.filterValues {
        log.info("Pending Package found! $it")
        !Instant.parse(it.uploadDate).isBefore(Instant.now().minusSeconds(allowedAge))
      }
    }
  }

  fun put(key: String, value: Package) {
    map = map.plus(pair = Pair(key, value))
  }

  fun remove(key: String): Package? {
    val remove = map[key]
    map = map.minus(key)
    return remove
  }

  companion object {
    private val log = LoggerFactory.getLogger(PendingPackages::class.java)
  }
}