package de.evoila.bpm.exceptions

import java.lang.IllegalArgumentException

class PackageNotFoundException(message: String?) : IllegalArgumentException(message)