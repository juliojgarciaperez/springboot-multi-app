package com.example.ogex.apps.bar

import com.example.ogex.apps.bar.services.PokemonService
import com.example.ogex.common.say
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BarController(private val pokemon: PokemonService) {
    // use to verify only bar app is consuming this memory
    private val hugeArray = Array(10_000_000) { "Hello $it" }

    @GetMapping("/bar")
    suspend fun getBar(): Map<String, String> {
        val details = pokemon.getPokemon("bulbasaur").awaitSingle()

        return mapOf("message" to say("bar"), "pokemon" to (details.name))
    }
}
