# Imopro (MVP)

Imopro est une application desktop JavaFX orientée **offline-first** pour un usage immobilier personnel.

Ce MVP contient actuellement 2 modules fonctionnels :
- **Contacts**
- **Biens**

L’application suit une architecture modulaire :
- `imopro-domain` : entités métier pures
- `imopro-application` : services / cas d’usage
- `imopro-infra` : persistance SQLite + migrations Flyway
- `imopro-ui` : interface JavaFX (views + viewmodels)

---

## Module Contact

Le module Contact sert à gérer les personnes (propriétaires, acquéreurs potentiels, partenaires, etc.).

### Ce que fait le module
- Afficher la liste des contacts.
- Rechercher un contact rapidement.
- Créer un nouveau contact.
- Modifier un contact existant.
- Supprimer un contact.

### Rôle de chaque champ (fiche contact)
- **Prénom** : prénom de la personne.
- **Nom** : nom de famille.
- **Téléphone** : numéro principal pour les appels/SMS.
- **Email** : adresse e-mail de contact.
- **Adresse** : adresse postale du contact.
- **Notes** : informations libres (préférences, contexte, historique synthétique, etc.).

### Rôle de chaque bouton (module Contact)
- **Nouveau** (colonne de gauche) : crée une fiche contact vide et la sélectionne.
- **Enregistrer** (fiche à droite) : sauvegarde les modifications du contact dans SQLite.
- **Supprimer** : supprime définitivement le contact sélectionné.

### Recherche (liste Contact)
Le champ de recherche filtre la liste par :
- nom complet (prénom + nom),
- email,
- téléphone.

---

## Module Bien

Le module Bien sert à gérer les propriétés immobilières suivies dans l’outil.

### Ce que fait le module
- Afficher la liste des biens.
- Rechercher un bien rapidement.
- Créer un nouveau bien.
- Modifier un bien existant.
- Supprimer un bien.

### Rôle de chaque champ (fiche bien)
- **Titre** : libellé principal du bien (ex. "Appartement T3 centre-ville").
- **Adresse** : rue et numéro.
- **Ville** : commune du bien.
- **CP** : code postal.
- **Type** : type de bien (appartement, maison, terrain, etc.).
- **Surface** : surface (m²) en valeur numérique.
- **Pièces** : nombre de pièces principales.
- **Prix** : prix demandé/estimé.
- **Statut** : état commercial du bien (ex. Lead, Mandat, En vente, etc.).

### Rôle de chaque bouton (module Bien)
- **Nouveau bien** (colonne de gauche) : crée une fiche bien vide et la sélectionne.
- **Enregistrer** (fiche à droite) : sauvegarde les modifications du bien dans SQLite.
- **Supprimer** : supprime définitivement le bien sélectionné.

### Recherche (liste Bien)
Le champ de recherche filtre la liste par :
- titre,
- adresse,
- ville,
- statut.

---

## Navigation de l’application

Barre latérale gauche :
- **Contacts** : ouvre le module Contact.
- **Biens** : ouvre le module Bien.
- **Tâches / Documents / Pipeline** : placeholders pour les itérations suivantes.

---

## Lancer l’application en local

Depuis la racine du dépôt :

```bash
./gradlew :imopro-ui:run
```

Si vous utilisez Gradle installé localement :

```bash
gradle :imopro-ui:run
```

> Note : dans certains environnements, une incompatibilité Gradle/JDK peut empêcher la compilation (ex. erreur sur la version de bytecode). Dans ce cas, aligner la version de Gradle avec Java 25 ou utiliser le wrapper Gradle du projet.
