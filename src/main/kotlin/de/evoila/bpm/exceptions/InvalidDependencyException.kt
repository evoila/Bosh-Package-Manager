package de.evoila.bpm.exceptions

import java.lang.IllegalArgumentException

class InvalidDependencyException(name: String, version: String)
  : IllegalArgumentException("Could not find a release called $name with version $version")