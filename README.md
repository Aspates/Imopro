# Imopro (MVP)

Imopro est une application desktop JavaFX orientée offline-first pour un usage immobilier personnel.

Le MVP contient 6 modules :
- Contacts
- Biens
- Tâches
- Documents
- Pipeline
- Loyers

Architecture :
- `imopro-domain` : entités métier pures
- `imopro-application` : services / cas d'usage
- `imopro-infra` : persistance SQLite + migrations Flyway
- `imopro-ui` : interface JavaFX (views + viewmodels)

---

## Contrôles de saisie (validation en direct)

Dans les modules à formulaire (Contacts, Biens, Tâches, Documents), les champs sont validés pendant la saisie.

- En cas de valeur invalide, un message d'erreur s'affiche à droite du champ.
- La fiche de détail est affichée uniquement lorsqu'un élément est sélectionné/créé.
- Contrôles couverts :
- alphanumérique / texte encadré
- numériques (CP, pièces, taille)
- décimaux (surface, prix)
- format email
- format MIME
- valeurs contraintes (ex: statut tâche `TODO`/`DONE`)

---

## Valeurs par défaut (module Biens)

Menu `Paramètres -> Valeurs par défaut` :
- Adresse
- Ville
- CP
- Type

Ces valeurs sont injectées lors de `Nouveau bien`.

---

## Sauvegarde des informations

Menu `Paramètres -> Voir sauvegarde` :
- Affiche le chemin du repertoire de sauvegarde (donnees SQLite + documents attaches).
- Bouton `Ouvrir répertoire` : ouvre ce dossier dans l'explorateur du système.
- Bouton `Modifier` : permet de choisir un autre répertoire de sauvegarde.

Notes :
- Le nouveau chemin est mémorisé pour les prochains lancements de l'application.
- Le changement s'applique pleinement après fermeture/réouverture de l'application.

---

## Module Loyers

Le module Loyers gère les loyers avec un CRUD, liés à un locataire (Contact) et à un bien (Property).

### Fonctionnalités
- CRUD des loyers
- Jointure vers Contact (locataire)
- Jointure vers Bien
- Règles de tâches automatiques (hebdo/mensuel/trimestriel/annuel)
- Génération automatique de tâches à échéance
- Navigation vers Contacts/Biens/Tâches

### Règles métier (Loyers)
- Le bouton `Ajouter règle` ne remplit plus de liste de règles dédiée (supprimée car redondante).
- `Ajouter règle` crée immédiatement une tâche liée au loyer.
- Format du titre de la tâche créée : `Titre du bien - Nom complet du contact`.
- Format de la description : `Tâche générée automatiquement pour le loyer <Titre du bien> du locataire <Nom complet du contact>`.
- Paramétrage de fréquence :
- Hebdomadaire : jour de semaine
- Mensuelle : jour du mois
- Trimestrielle : jour du mois + période `1..3`
- Annuelle : jour du mois + mois `1..12`
- La combobox `1..3` est visible uniquement en trimestriel.
- La combobox `1..12` est visible uniquement en annuel.
- Tableau des tâches liées :
- colonnes `Type`, `Date d'échéance`, `Renouvelable` (`✔` / `✖`) + action vers la tâche
- ligne rouge : tâche non faite en retard
- ligne verte : tâche faite non renouvelable
- Documents liés au loyer :
- bouton `Ajouter document` (import local, rattachement au loyer)
- tableau `Nom`, `Date d'ajout`, `Ouvrir document`
- Synchronisation Bien <-> Loyer :
- libellé `Montant` (anciennement `Montant mensuel`)
- `Montant` du loyer et `Prix` du bien doivent être cohérents
- à l'enregistrement d'un loyer, le prix du bien est mis à jour avec le montant
- à l'ouverture d'un loyer, le montant est alimenté depuis le prix du bien
- Navigation dans la fiche loyer :
- bouton `Voir contact` à droite de la combobox `Locataire`
- bouton grisé si aucun contact sélectionné

---

## Module Contacts

Le module Contacts gère les personnes (propriétaires, locataires, partenaires, etc.).

### Ce que fait le module
- Afficher la liste des contacts
- Rechercher un contact
- Créer un contact
- Modifier un contact
- Supprimer un contact

### Champs (fiche contact)
- Prénom
- Nom
- Téléphone
- Email
- Adresse
- Notes

### Boutons
- `Nouveau` : crée une fiche vide et la sélectionne
- `Enregistrer` : sauvegarde en SQLite
- `Supprimer` : supprime le contact sélectionné
- `Voir loyer` : ouvre le loyer associé au contact (si existant)
- le bouton est grisé s'il n'existe aucun loyer correspondant

### Recherche
Filtre par :
- nom complet (prénom + nom)
- email
- téléphone

---

## Module Biens

Le module Biens gère les propriétés immobilières.

### Ce que fait le module
- Afficher la liste des biens
- Rechercher un bien
- Créer un bien
- Modifier un bien
- Supprimer un bien

### Champs (fiche bien)
- Titre
- Adresse
- Ville
- CP
- Type
- Surface
- Pièces
- Prix
- Statut

### Synchronisation Bien <-> Pipeline
- Le champ `Statut` est alimenté par les étapes du module Pipeline.
- Un changement de statut dans Biens déplace le bien dans la colonne correspondante du Pipeline.
- Un déplacement dans Pipeline met à jour le statut du bien.

### Boutons
- `Nouveau bien`
- `Enregistrer`
- `Supprimer`
- `Voir loyer` : ouvre le loyer associé au bien (si existant)
- bouton grisé si aucune correspondance

### Recherche
Filtre par :
- titre
- adresse
- ville
- statut

---

## Module Tâches

Le module Tâches pilote les actions quotidiennes et relances.

### Ce que fait le module
- Afficher la liste des tâches
- Rechercher une tâche
- Créer une tâche
- Modifier une tâche
- Marquer une tâche comme faite
- Supprimer une tâche
- Filtrer par vues rapides : `Toutes`, `Aujourd'hui`, `En retard`, `Cette semaine`

### Champs (fiche tâche)
- Titre
- Description
- Échéance (`YYYY-MM-DD`)
- Statut (`TODO` / `DONE`)
- Type (issu de la règle de loyer, sinon `Ponctuelle`)
- Renouvelable (`Oui` / `Non`)

### Boutons
- `Nouvelle tâche`
- `Enregistrer`
- `Marquer fait`
- `Supprimer`
- `Voir loyer` : ouvre le loyer lié à la tâche
- bouton grisé si la tâche n'est pas liée à un loyer

### Règle de renouvellement automatique
Si une tâche liée à une règle auto-renouvelable est marquée `DONE`, l'échéance passe à la prochaine occurrence :
- hebdomadaire : `+1 semaine`
- mensuelle : `+1 mois`
- trimestrielle : `+3 mois`
- annuelle : `+1 an`

### Recherche
Filtre par :
- titre
- description
- statut

---

## Module Documents

Le module Documents rattache et consulte des fichiers localement.

### Ce que fait le module
- Importer un fichier depuis la machine
- Copier automatiquement dans `attachments/`
- Enregistrer les métadonnées en base (`document`)
- Rechercher les documents
- Ouvrir un document via l'application par défaut du système
- Modifier les métadonnées (nom, mime, taille)
- Supprimer un document

### Champs (fiche document)
- Nom
- Chemin relatif (lecture seule)
- MIME
- Taille (octets)

### Boutons
- `Importer un fichier`
- `Enregistrer`
- `Ouvrir`
- `Supprimer`

### Recherche
Filtre par :
- nom du fichier
- type MIME
- chemin relatif

---

## Module Pipeline

Le module Pipeline propose une vue Kanban basée sur les étapes commerciales immobilières.

### Ce que fait le module
- Afficher les étapes en colonnes
- Afficher les biens selon leur statut
- Déplacer un bien d'une étape à l'autre (`←` / `→`)
- Historiser les changements dans `pipeline_event`

### Données manipulées
- `property.status` : étape courante
- `pipeline_event` : historique daté des mouvements

---

## Navigation de l'application

Barre latérale gauche :
- Contacts
- Biens
- Tâches
- Documents
- Pipeline
- Loyers

---

## Lancer l'application en local

Depuis la racine :

```bash
./gradlew :imopro-ui:run
```

Si vous utilisez Gradle installé localement :

```bash
gradle :imopro-ui:run
```

> En cas d'incompatibilité Gradle/JDK (ex: erreur de bytecode), aligner les versions ou utiliser le wrapper Gradle du projet.
