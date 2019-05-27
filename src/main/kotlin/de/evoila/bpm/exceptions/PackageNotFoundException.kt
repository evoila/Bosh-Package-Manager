package de.evoila.bpm.exceptions

import java.lang.IllegalArgumentException

class PackageNotFoundException(message: String?) : IllegalArgumentException(message) {

  constructor(publisher: String, name: String, version: String)
      : this(("didn't not find a package with publisher : $publisher , name : $name:$version"))

}