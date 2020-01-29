package com.kotlin.multimodulespringboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MultiModuleSpringBootApplication

fun main(args: Array<String>) {
	runApplication<MultiModuleSpringBootApplication>(*args)
}
