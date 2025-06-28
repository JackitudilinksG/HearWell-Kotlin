package com.example.hearwell

val frequenciesGlobal = listOf(250.0, 500.0, 1000.0, 2000.0, 4000.0, 8000.0)

// Make them mutable so they can be updated from anywhere
var leftResultsGlobal: MutableList<Float?> = MutableList(frequenciesGlobal.size) { null }
var rightResultsGlobal: MutableList<Float?> = MutableList(frequenciesGlobal.size) { null }
