package com.projdgroep3.omgevingswet.service

import com.fasterxml.jackson.annotation.JsonIgnore

interface Identifiable {
    @JsonIgnore
    fun getIdentifier(): String
}