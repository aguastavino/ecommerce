# E-Commerce

Sistema BE per E-Commerce, da ampliare.

## Tecnologie

| Ambito | Tecnologia |
|---|---|
| Linguaggio | Java 17 |
| Framework | Spring Boot 3.3.5 (Spring Web, Spring Data JPA, Bean Validation) |
| Persistenza | Hibernate / JPA |
| Database | H2 in memoria |
| Migrazioni | Liquibase (changelog XML) |
| Documentazione API | springdoc-openapi 2.6.0 (OpenAPI 3, Swagger UI) |
| Build | Maven |
| Utility | Lombok |
| Test | JUnit 5, Mockito, Spring Boot Test (MockMvc) |
| Coverage | JaCoCo (soglia minima 80% di linee) |
| Configurazione | `application.yaml` |

## Funzionalità

- **Clienti**: aggiunta e lista (email e codice fiscale univoci).
- **Prodotti**: aggiunta e lista (codice univoco), con quantità a stock.
- **Ordini**: creazione per un cliente con uno o più prodotti, e lista.
  - Un ordine non evadibile (quantità superiore allo stock) viene rifiutato con `409`.
  - Alla creazione lo stock dei prodotti diminuisce della quantità ordinata.
  - Gli ordini concorrenti sono gestiti con lock pessimistico sul prodotto: non si vende mai più dello stock disponibile.
- **Stato ordine**: `ORDINATO` e `CONSEGNATO`. L'evento `DELIVERED` fa passare l'ordine da uno stato all'altro.
- **Cancellazione ordine**: consentita finché l'ordine non è `CONSEGNATO`; lo stock riservato viene ripristinato.
- **Paginazione** di clienti, prodotti e ordini (`page`, `size`, `sort`).
- **Errori** uniformi in formato RFC 7807 (`400`, `404`, `409`, `500`).
- **Log** su ogni operazione: `INFO` per inizio e fine, `WARN` per i rifiuti di business, `ERROR` per gli errori imprevisti. Le query SQL di Hibernate sono visibili in console.

### API principali

| Metodo | Percorso | Descrizione |
|---|---|---|
| `POST` / `GET` | `/api/customers` | Aggiunge un cliente / lista paginata |
| `POST` / `GET` | `/api/products` | Aggiunge un prodotto / lista paginata |
| `POST` / `GET` | `/api/orders` | Crea un ordine / lista paginata |
| `POST` | `/api/orders/{id}/events` | Registra un evento (`{"type": "DELIVERED"}`) |
| `DELETE` | `/api/orders/{id}` | Cancella un ordine non consegnato |

Il dettaglio di richieste e risposte è in Swagger UI.

## Architettura e pattern

Struttura a livelli: `controller` → `facade` → `service` → `repository`, con `dto` e `adapter` verso l'esterno. Pattern usati: Facade, Adapter, Strategy, Command, Factory, Builder. Il codice segue i principi SOLID. Il dettaglio è in [`DOCUMENTATION.md`](DOCUMENTATION.md).

## Gestione della concorrenza

Il rischio da evitare è la *lost update* sullo stock: due ordini concorrenti leggono lo stesso valore (ad esempio 1), entrambi lo ritengono sufficiente e lo decrementano, vendendo più di quanto disponibile.

**Soluzione: lock pessimistico sul prodotto.**

1. `OrderServiceImpl.create` è transazionale. Per ogni prodotto richiesto usa `ProductRepository.findByIdForUpdate`, annotato con `@Lock(PESSIMISTIC_WRITE)`, che esegue una `SELECT ... FOR UPDATE`.
2. Una transazione concorrente sullo stesso prodotto resta in attesa finché la prima non termina con commit o rollback.
3. Con il lock acquisito, la `StockAvailabilityStrategy` verifica lo stock e solo dopo avviene il decremento. Leggere lo stock aggiornato è quindi sempre sicuro.
4. Se il controllo fallisce, la transazione va in rollback e nessuno stock viene modificato.

**Prevenzione dei deadlock.** Le righe dello stesso prodotto sono accorpate e i prodotti vengono bloccati sempre in ordine crescente di id. Due ordini che coinvolgono gli stessi prodotti li acquisiscono quindi nello stesso ordine e non si bloccano a vicenda.

**La stessa regola vale per la cancellazione**: il ripristino dello stock avviene sotto lock sul prodotto.

**Perché pessimistico e non ottimistico.** Sui prodotti molto richiesti i conflitti sono frequenti: con un `@Version` molti ordini validi fallirebbero e andrebbero ritentati. Il lock pessimistico li serializza in modo semplice e prevedibile, con transazioni brevi. Il compromesso è una minore scalabilità sul singolo prodotto "caldo".

**Verifica.** `ConcurrentOrdersTest` lancia 30 ordini da 1 unità su 8 thread contro un prodotto con stock 10: ne vengono accettati esattamente 10, lo stock finale è 0 e gli ordini creati sono 10.

## Requisiti

- JDK 17
- Maven 3.9+ (oppure quello integrato nell'IDE)

## Compilare, testare e avviare

```
mvn clean package       # compila ed esegue i test
mvn test                # solo test
mvn verify              # test + report JaCoCo + verifica coverage >= 80%
mvn spring-boot:run     # avvia l'applicazione
```

In alternativa, dall'IDE: eseguire la classe `it.ecommerce.Application`.

Il report di coverage è in `target/site/jacoco/index.html`.

## Dopo l'avvio

L'applicazione è su `http://localhost:8080`.

| Risorsa | URL |
|---|---|
| Swagger UI (API testabili) | http://localhost:8080/swagger-ui.html |
| Specifica OpenAPI | http://localhost:8080/v3/api-docs |
| Console H2 | http://localhost:8080/h2-console |

Per la console H2: JDBC URL `jdbc:h2:mem:ecommerce`, utente `sa`, password vuota. Il database è in memoria, quindi i dati si perdono a ogni riavvio.

## Esempio d'uso

1. `POST /api/customers` con un cliente.
2. `POST /api/products` con un prodotto e il suo stock.
3. `POST /api/orders` con `{"customerId": 1, "items": [{"productId": 1, "quantity": 2}]}`.
4. `POST /api/orders/1/events` con `{"type": "DELIVERED"}`.
