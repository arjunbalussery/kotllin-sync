package com.example.sync

import com.fasterxml.jackson.annotation.JsonProperty

data class SaveProduct(@JsonProperty("name") val name: String)
