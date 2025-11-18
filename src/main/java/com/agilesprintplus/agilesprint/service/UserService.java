package com.agilesprintplus.agilesprint.service;

import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    /**
     * 🔹 Crée un nouvel utilisateur à partir d’un DTO de création.
     * @param dto les données du nouvel utilisateur
     * @return un DTO de réponse contenant les informations persistées
     */
    UserDtos.Response create(UserDtos.Create dto);

    /**
     * 🔹 Récupère un utilisateur par son identifiant unique.
     * @param id l’identifiant de l’utilisateur
     * @return un DTO de réponse représentant l’utilisateur
     */
    UserDtos.Response getById(UUID id);

    /**
     * 🔹 Liste paginée et triée des utilisateurs.
     * @param pageable les paramètres de pagination/sorting
     * @return une page de DTOs utilisateurs
     */
    Page<UserDtos.Response> list(Pageable pageable);

    /**
     * 🔹 Met à jour partiellement les informations d’un utilisateur.
     * @param id identifiant de l’utilisateur
     * @param dto les données à modifier
     * @return un DTO de réponse mis à jour
     */
    UserDtos.Response update(UUID id, UserDtos.Update dto);

    /**
     * 🔹 Supprime (ou désactive) un utilisateur.
     * @param id identifiant de l’utilisateur
     */
    void delete(UUID id);

    /**
     * 🔹 Modifie le mot de passe d’un utilisateur.
     * @param id identifiant de l’utilisateur
     * @param dto DTO contenant l’ancien et le nouveau mot de passe
     */
    void changePassword(UUID id, UserDtos.ChangePassword dto);

    /**
     * 🔹 Active ou désactive un utilisateur.
     * @param id identifiant de l’utilisateur
     * @param enabled true pour activer, false pour désactiver
     * @return DTO utilisateur mis à jour
     */
    UserDtos.Response toggleActive(UUID id, boolean enabled);

    /**
     * 🔹 Recherche d’un utilisateur par username (utile pour l’authentification).
     * @param username nom d’utilisateur
     * @return DTO utilisateur
     */
    UserDtos.Response getByUsername(String username);
}
