package com.example.ogex.apps.bar.services

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

data class Pokemon(val name: String, val order: Int)

@Service
class PokemonService(private val webClient: WebClient) {

    fun getPokemon(name: String?): Mono<Pokemon> {
        return webClient
                .get()
                .uri("https://pokeapi.co/api/v2/pokemon/${name}/")
                .retrieve()
                .bodyToMono(Pokemon::class.java)
    }

    fun lala(): String {
        return "Lala from PokemonService"
    }
}
