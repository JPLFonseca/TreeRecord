package com.example.treerecord

import com.google.firebase.database.IgnoreExtraProperties

// Modelo de dados para a localização
@IgnoreExtraProperties
data class Localizacao(
    val latitude: Double? = null,
    val longitude: Double? = null
)

// Modelo de dados principal para a árvore
@IgnoreExtraProperties
data class Arvore(
    val nome: String? = null,
    val descricao: String? = null,
    val userId: String? = null,
    val timestamp: Long? = null,
    val urlFoto: String? = null,
    val localizacao: Localizacao? = null,
    // Gostos são um Map onde a chave é o userId e o valor é sempre true
    val gostos: Map<String, Boolean>? = null
)