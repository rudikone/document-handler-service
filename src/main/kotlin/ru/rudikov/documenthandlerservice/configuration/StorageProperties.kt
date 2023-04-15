package ru.rudikov.documenthandlerservice.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationProperties("storage")
class StorageProperties {
    /**
     * Folder location for storing files
     */
    lateinit var location: String

    private val homeDirectory: String get() = System.getProperty("user.home")
    private val serviceDirectory: String get() = "DocumentHandlerService"
    val storageLocation: Path get() = Paths.get(
        homeDirectory + File.separator + serviceDirectory + File.separator + location
    )
}
