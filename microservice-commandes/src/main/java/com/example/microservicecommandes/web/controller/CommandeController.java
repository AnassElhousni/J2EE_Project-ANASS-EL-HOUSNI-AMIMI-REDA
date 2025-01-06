package com.example.microservicecommandes.web.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.microservicecommandes.configurations.ApplicationPropertiesConfiguration;
import com.example.microservicecommandes.dao.CommandeDao;
import com.example.microservicecommandes.model.Commande;
import com.example.microservicecommandes.web.exception.CommandeNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class CommandeController implements HealthIndicator {
    @Autowired
    CommandeDao commandeDao;

    @Autowired
    ApplicationPropertiesConfiguration appProperties;

    // Circuit Breaker for listing commandes with timeout handling
    @GetMapping(value = "/Commandes")
    @CircuitBreaker(name = "commandesCircuitBreaker", fallbackMethod = "handleTimeout")
    public List<Commande> listeDesCommandes() throws CommandeNotFoundException {
        System.out.println("********* CommandeController listeDesCommandes() ");

        // Simulating a delay to trigger the timeout
        try {
            Thread.sleep(5000); // 5 seconds delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Commande> commandes = commandeDao.findAll();
        System.out.println("Nombre de commandes récupérées : " + commandes.size());

        if (commandes.isEmpty()) {
            throw new CommandeNotFoundException("Aucune commande n'a été trouvée.");
        }

        // Retrieve the limit from configuration
        int limit = appProperties.getCommandesLast();
        System.out.println("Limite définie dans la configuration : " + limit);

        // Get today's date
        LocalDate today = LocalDate.now();

        // Filter commandes for the last 'limit' days
        List<Commande> commandesFiltrees = commandes.stream()
                .filter(commande -> commande.getDate().isAfter(today.minusDays(limit))) // Filter for last 'limit' days
                .sorted((c1, c2) -> {
                    // First compare by date (descending order)
                    int dateComparison = c2.getDate().compareTo(c1.getDate());
                    // If dates are the same, compare by ID (ascending order)
                    if (dateComparison == 0) {
                        return c1.getId().compareTo(c2.getId());
                    }
                    return dateComparison;
                })
                .collect(Collectors.toList());

        System.out.println("Nombre de commandes filtrées et triées : " + commandesFiltrees.size());

        if (commandesFiltrees.isEmpty()) {
            throw new CommandeNotFoundException("Aucune commande n'a été trouvée dans les derniers jours.");
        }

        // Return the filtered and sorted commandes
        return commandesFiltrees;
    }

    // Fallback method to handle timeout
    public List<Commande> handleTimeout(Exception ex) {
        System.out.println("***** Fallback activated: The service is temporarily unavailable.");
        return List.of();  // Return empty list as fallback
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

    // Simulate a timeout for testing purposes
    @GetMapping(value = "/Commandes/timeout")
    public String simulateTimeout() throws InterruptedException {
        // Simulate a delay of 5 seconds (which can cause a timeout)
        Thread.sleep(3000);  // 3 seconds delay
        return "Commande traitée après un délai";
    }

    // Ajouter une nouvelle commande
    @PostMapping(value = "/Commandes")
    public ResponseEntity<List<Commande>> ajouterCommandes(@RequestBody List<Commande> commandes) {
        System.out.println("********* CommandeController ajouterCommandes() ");
        List<Commande> nouvellesCommandes = commandeDao.saveAll(commandes); // Utilisez saveAll pour sauvegarder plusieurs commandes
        return ResponseEntity.ok(nouvellesCommandes);
    }
    @PutMapping(value = "/Commandes/{id}")
    public ResponseEntity<Commande> updateCommande(@PathVariable Long id, @RequestBody Commande updatedCommande) {
        return commandeDao.findById(id).map(commande -> {
            commande.setDescription(updatedCommande.getDescription());
            commande.setQuantite(updatedCommande.getQuantite());
            commande.setDate(updatedCommande.getDate());
            commande.setMontant(updatedCommande.getMontant());
            Commande savedCommande = commandeDao.save(commande);
            return ResponseEntity.ok(savedCommande);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/Commandes/{id}")
    public ResponseEntity<Object> deleteCommande(@PathVariable Long id) {
        return commandeDao.findById(id).map(commande -> {commandeDao.delete(commande);
            return ResponseEntity.noContent().build(); // Renvoie un statut 204 No Content si la suppression est réussie
        }).orElseGet(() -> ResponseEntity.notFound().build()); // Renvoie 404 Not Found si la commande n'existe pas
    }



    // Health check for the service
    @Override
    public Health health() {
        System.out.println("****** Actuator : CommandeController health() ");
        List<Commande> commandes = commandeDao.findAll();

        if (commandes.isEmpty()) {
            return Health.down().build();
        }
        return Health.up().build();
    }
}
