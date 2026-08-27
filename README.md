# Awesome Pizza — Backend

API REST per la gestione degli ordini della pizzeria Awesome Pizza

Documentazione interattiva delle API: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Flusso funzionale

1. Il cliente consulta il catalogo (`GET /api/pizzas`) e crea un ordine (`POST /api/orders`), ricevendo un **codice ordine** pubblico.
2. Il cliente traccia lo stato del proprio ordine tramite quel codice (`GET /api/orders/{orderCode}`).
3. Il pizzaiolo consulta la coda degli ordini in attesa (`GET /api/kitchen/orders/queue`), prende in carico un ordine alla volta (`PUT .../take-in-charge`) e lo segna pronto quando finito (`PUT .../ready`).

Un ordine passa per gli stati `RECEIVED → IN_PROGRESS → READY`. Il pizzaiolo lavora un ordine per volta: se prova a prenderne in carico un secondo mentre uno è già in lavorazione, riceve un errore.

## Alcune scelte di design

- Il vincolo "un ordine in lavorazione alla volta" oggi è gestito con un lock a livello di processo (`synchronized`), che vale solo per la singola istanza in cui gira. Per scalare su più istanze servirebbe spostare questo lock (e la cache delle pizze) su un servizio esterno condiviso, ad esempio Redis. Un'alternativa sarebbe un indice unico parziale a DB (solo sugli ordini `IN_PROGRESS`), ma H2 non lo supporta.
- Persistenza in-memory: i dati non sopravvivono al riavvio dell'applicazione.
- Totale dell'ordine congelato al momento della creazione: se il prezzo di una pizza cambia dopo, gli ordini già fatti mantengono il totale con cui sono stati creati.
- Area cliente e area cucina separate: due punti di accesso distinti alle API, così in futuro sarà facile aggiungere un login solo per il personale della pizzeria senza toccare il resto.
- Codice ordine pubblico, diverso dall'id interno del database: il cliente traccia il proprio ordine con un codice generato apposta, non con l'identificativo tecnico usato internamente — evita che si possano indovinare gli ordini di altri clienti.
