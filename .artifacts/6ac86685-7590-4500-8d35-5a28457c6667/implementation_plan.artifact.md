# Redesign Professionale DrugDose

Trasformazione dell'interfaccia in un software medico professionale di alto livello, mantenendo invariata la logica applicativa esistente.

## User Review Required

> [!IMPORTANT]
> Il redesign introduce il colore istituzionale **#0C604E** e lo sfondo **#F5F6F4**, eliminando completamente i toni viola e blu precedenti.
> Verrà utilizzata la libreria **Material Components** configurata in stile **Material 3** per garantire bordi sottili e un look sobrio.

## Proposed Changes

### Risorse Grafiche e Temi

#### [MODIFY] [colors.xml](file:///Users/gianluca/Uni/Secondo Anno/Dispositivi Mobili/DrugDose/app/src/main/res/values/colors.xml)
- Definizione del colore primario medico (#0C604E).
- Definizione dello sfondo avorio (#F5F6F4).
- Colori per stati di avviso (ambra soft) e errore (rosso clinico).

#### [MODIFY] [dimens.xml](file:///Users/gianluca/Uni/Secondo Anno/Dispositivi Mobili/DrugDose/app/src/main/res/values/dimens.xml)
- Standardizzazione spaziature (multipli di 4/8 dp).
- Riduzione dei raggi di curvatura per un look più serio e meno "consumer".

#### [MODIFY] [styles.xml](file:///Users/gianluca/Uni/Secondo Anno/Dispositivi Mobili/DrugDose/app/src/main/res/values/styles.xml)
- Ridisegno dei `MedicalTextInput` e `MedicalDropdown` con bordi sottili e focus verde.
- Creazione di stili per le card di sezione.

---

### Layout e Attività

#### [MODIFY] [activity_main.xml](file:///Users/gianluca/Uni/Secondo Anno/Dispositivi Mobili/DrugDose/app/src/main/res/layout/activity_main.xml)
- Inserimento di una `MaterialToolbar` istituzionale.
- Riorganizzazione della gerarchia: Farmaco -> Dati Paziente -> Risultato.
- Restyling completo del pannello risultati e delle avvertenze.

#### [MODIFY] [MainActivity.kt](file:///Users/gianluca/Uni/Secondo Anno/Dispositivi Mobili/DrugDose/app/src/main/java/it/uninsubria/drugdose/MainActivity.kt)
- Aggiornamento dei riferimenti alle view se necessario (es. gestione della toolbar).
- Rafforzamento della logica di visualizzazione degli alert con colori coerenti al nuovo stile.

## Verification Plan

### Automated Tests
- `gradlew assembleDebug` per verificare la corretta compilazione con le nuove risorse.

### Manual Verification
- Verifica visiva su emulatore/dispositivo per confermare:
    - Corretta applicazione del colore #0C604E.
    - Leggibilità dei testi sullo sfondo #F5F6F4.
    - Comportamento degli stati di focus e errore negli input.
    - Visibilità delle avvertenze cliniche.
