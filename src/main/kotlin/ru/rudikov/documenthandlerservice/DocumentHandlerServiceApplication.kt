package ru.rudikov.documenthandlerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DocumentHandlerServiceApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<DocumentHandlerServiceApplication>(*args)
}
