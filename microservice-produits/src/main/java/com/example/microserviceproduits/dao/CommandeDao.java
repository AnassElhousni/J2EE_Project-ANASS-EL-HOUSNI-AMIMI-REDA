package com.example.microserviceproduits.dao;

import com.example.microserviceproduits.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository est une annotation Spring qui indique que cette classe est responsable
// de la communication avec une source de données (comme une base de données).
// Cette classe est une spécialisation de @Component et est détectée automatiquement par Spring.
@Repository
public interface CommandeDao extends JpaRepository<Commande, Long> {
}
