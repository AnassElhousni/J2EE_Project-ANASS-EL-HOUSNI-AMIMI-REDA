package com.example.microserviceproduits.web.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.microserviceproduits.configurations.ApplicationPropertiesConfiguration;
import com.example.microserviceproduits.dao.CommandeDao;
import com.example.microserviceproduits.model.Commande;
import com.example.microserviceproduits.web.exception.CommandeNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
public class CommandeController implements HealthIndicator {

    @Autowired
    private CommandeDao commandeDao;

    @Autowired
    private ApplicationPropertiesConfiguration appProperties;

    // Récupérer la liste des commandes
    @GetMapping(value = "/Commandes")
    public List<Commande> listeDesCommandes() throws CommandeNotFoundException {
        System.out.println("********* CommandeController listeDesCommandes() ");
        List<Commande> commandes = commandeDao.findAll();
        System.out.println("Nombre de commandes récupérées : " + commandes.size());

        if (commandes.isEmpty()) {
            throw new CommandeNotFoundException("Aucune commande n'a été trouvée.");
        }

        // Récupérer la limite définie dans la configuration
        int limit = appProperties.getCommandesLast();
        System.out.println("Limite définie dans la configuration : " + limit);

        // Trier les commandes par date décroissante et limiter au nombre défini
        List<Commande> commandesRecentes = commandes.stream()
                .sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate())) // Trier par date décroissante
                .limit(limit) // Limiter selon la propriété
                .toList();

        System.out.println("Nombre de commandes récentes : " + commandesRecentes.size());
        return commandesRecentes;
    }

    // Récupérer une commande par son ID
    @GetMapping(value = "/Commandes/{id}")
    public Optional<Commande> recupererUneCommande(@PathVariable Long id) throws CommandeNotFoundException {
        System.out.println("********* CommandeController recupererUneCommande(@PathVariable Long id) ");
        Optional<Commande> commande = commandeDao.findById(id);

        if (!commande.isPresent()) {
            throw new CommandeNotFoundException("La commande correspondant à l'ID " + id + " n'existe pas.");
        }

        return commande;
    }

    // Ajouter une nouvelle commande
    @PostMapping(value = "/Commandes")
    public ResponseEntity<Commande> ajouterCommande(@RequestBody Commande commande) {
        System.out.println("********* CommandeController ajouterCommande() ");
        // Ajouter une date par défaut si elle n'est pas fournie
        if (commande.getDate() == null) {
            commande.setDate(LocalDate.now());
        }
        Commande nouvelleCommande = commandeDao.save(commande);
        return ResponseEntity.ok(nouvelleCommande);
    }

    // Vérification de l'état de santé du service
    @Override
    public Health health() {
        System.out.println("****** Actuator : CommandeController health() ");
        List<Commande> commandes = commandeDao.findAll();

        if (commandes.isEmpty()) {
            return Health.down().withDetail("error", "No commandes found").build();
        }

        return Health.up().build();
    }
}