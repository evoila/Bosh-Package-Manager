package de.evoila.bpm.service

import de.evoila.bpm.entities.Blob
import de.evoila.bpm.repositories.BlobRepository
import de.evoila.bpm.rest.bodies.BlobBody
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class BlobService(
    val blobRepository: BlobRepository
) {

  fun storeBlob(blobBody: BlobBody, type: String, file: MultipartFile): Blob {

    blobRepository.findByNameAndVersion(blobBody.name, blobBody.version)?.let {
      return it
    }

    val filename = "${UUID.randomUUID()}.$type"
    val target = storage.resolve(filename)

    try {
      Files.copy(file.inputStream, target, StandardCopyOption.REPLACE_EXISTING)

    } catch (e: IOException) {
      e.printStackTrace()
    }

    return blobRepository.save(
        Blob(
            name = blobBody.name,
            location = target.toString(),
            version = blobBody.version
        )
    )
  }

  fun findBlobOrThrow(uuid: String) = blobRepository.findById(uuid).orElseThrow { IllegalArgumentException("Blob with uuid:$uuid not found") }!!

  fun findBlobFile(blob: Blob): FileInputStream {

    val file = File(blob.location)

    return FileInputStream(file)

  }


  companion object {
    private val storage = Paths.get("./blobs").toAbsolutePath().normalize()
  }
}
