package com.projdgroep3.omgevingswet.utils

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <A> A?.toResponseEntity(): ResponseEntity<A> = this?.let {
    ResponseEntity.ok(it)
} ?: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(this)