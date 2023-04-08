package ru.rudikov.documenthandlerservice.configuration

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("storage")
data class StorageProperties(
    /**
     * Folder location for storing files
     */
    var location: String = "upload-dir"
)