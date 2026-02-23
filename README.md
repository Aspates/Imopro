# Imopro (MVP)

Imopro est une application desktop JavaFX orientée **offline-first** pour un usage immobilier personnel.

Ce MVP contient actuellement 5 modules fonctionnels :
- **Contacts**
- **Biens**
- **Tâches**
- **Documents**
- **Pipeline**

L’application suit une architecture modulaire :
- `imopro-domain` : entités métier pures
- `imopro-application` : services / cas d’usage
- `imopro-infra` : persistance SQLite + migrations Flyway
- `imopro-ui` : interface JavaFX (views + viewmodels)

---

## Contrôles de saisie (validation en direct)

Dans tous les modules à formulaire (Contacts, Biens, Tâches, Documents), chaque champ texte est validé pendant la saisie.

- En cas de valeur invalide, un message d'erreur rouge s'affiche à droite du champ.
- Les contrôles couvrent les cas usuels :
  - alphanumérique / texte libre encadré,
  - numériques uniquement (CP, pièces, taille),
  - décimaux (surface, prix),
  - format e-mail,
  - format MIME,
  - valeurs contraintes (ex: statut tâche `TODO`/`DONE`).

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
- **Statut** : étape courante du pipeline location (sélection via liste déroulante).

### Synchronisation Bien ↔ Pipeline
- Le champ **Statut** est un **ComboBox** alimenté par les étapes du module Pipeline.
- Changer le statut dans la fiche Bien déplace automatiquement le bien dans la colonne correspondante du Pipeline (et inversement, un mouvement dans le Pipeline met à jour le statut du bien).

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

## Module Tâche

Le module Tâche sert à piloter les actions quotidiennes et relances.

### Ce que fait le module
- Afficher la liste des tâches.
- Rechercher une tâche.
- Créer une nouvelle tâche.
- Modifier une tâche.
- Marquer une tâche comme faite.
- Supprimer une tâche.
- Filtrer les tâches par vues rapides : **Toutes**, **Aujourd'hui**, **En retard**, **Cette semaine**.

### Rôle de chaque champ (fiche tâche)
- **Titre** : intitulé court de l’action.
- **Description** : détails de la tâche.
- **Échéance** : date limite au format `YYYY-MM-DD`.
- **Statut** : `TODO` (à faire) ou `DONE` (fait).

### Rôle de chaque bouton (module Tâche)
- **Nouvelle tâche** : crée une tâche vide et la sélectionne.
- **Enregistrer** : sauvegarde les modifications de la tâche.
- **Marquer fait** : passe la tâche en `DONE` et renseigne la date de complétion.
- **Supprimer** : supprime définitivement la tâche sélectionnée.
- **Toutes / Aujourd'hui / En retard / Cette semaine** : applique un filtre rapide sur la liste.

### Recherche (liste Tâche)
Le champ de recherche filtre la liste par :
- titre,
- description,
- statut.

---

## Module Documents

Le module Documents sert à rattacher et consulter des fichiers (PDF, photos, etc.) localement.

### Ce que fait le module
- Importer un fichier depuis la machine.
- Copier automatiquement le fichier dans `attachments/` (données locales de l'app).
- Enregistrer les métadonnées en base SQLite (`document`).
- Rechercher les documents importés.
- Ouvrir un document via l'application par défaut du système.
- Modifier des métadonnées (nom, mime, taille) et supprimer un document.

### Rôle de chaque champ (fiche document)
- **Nom** : nom d'affichage du document.
- **Chemin relatif** : emplacement relatif dans `attachments/` (lecture seule).
- **MIME** : type du document (ex. `application/pdf`, `image/jpeg`).
- **Taille (octets)** : taille du fichier.

### Rôle de chaque bouton (module Documents)
- **Importer un fichier** : ouvre un sélecteur de fichier et importe le document.
- **Enregistrer** : sauvegarde les métadonnées modifiées.
- **Ouvrir** : ouvre le fichier avec l'application par défaut de l'OS.
- **Supprimer** : supprime le document en base et supprime le fichier local si présent.

### Recherche (liste Documents)
Le champ de recherche filtre par :
- nom du fichier,
- type MIME,
- chemin relatif.

---

## Module Pipeline

Le module Pipeline propose une vue Kanban simple basée sur les étapes commerciales immobilières.

### Ce que fait le module
- Afficher les étapes du pipeline location en colonnes (Prospect locataire → Dossier reçu → Visite planifiée → Dossier complet → Bail à signer → Entrée locataire → Loué/Clos → Archive).
- Afficher chaque bien dans sa colonne selon son `statut`.
- Déplacer un bien vers l'étape précédente/suivante via les boutons fléchés (`←` / `→`).
- Enregistrer chaque changement d'étape dans `pipeline_event` (audit).

### Rôle des éléments UI (module Pipeline)
- **Colonnes** : représentent les étapes du cycle de location (`pipeline_stage`).
- **Cartes bien** : chaque carte représente un bien avec son titre/ville.
- **Bouton ←** : recule la carte d'une étape.
- **Bouton →** : avance la carte d'une étape.

### Données manipulées
- `property.status` = nom d'étape courante.
- `pipeline_event` = historique daté des mouvements de colonnes.

---

## Navigation de l’application

Barre latérale gauche :
- **Contacts** : ouvre le module Contact.
- **Biens** : ouvre le module Bien.
- **Tâches** : ouvre le module Tâche.
- **Documents** : ouvre le module Documents.
- **Pipeline** : ouvre le module Pipeline (kanban simple).

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
