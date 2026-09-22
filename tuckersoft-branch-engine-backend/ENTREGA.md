# Entrega - Tuckersoft Branch Engine

## Estrellas obtenidas
- Ejecutar `autotests` y colocar aquí el resultado real antes de entregar.

## Flujo asíncrono
`DecisionService` guarda la decisión dentro de una transacción y publica `DecisionCommittedEvent`.
Después del COMMIT, `BranchNotificationListener` recibe el evento con
`@TransactionalEventListener(AFTER_COMMIT)` y `@Async("branchExecutor")`,
abre una transacción `REQUIRES_NEW`, envía el correo mediante `JavaMailSender`
y registra `RealityLog`.

## Pendientes
Completar esta sección con cualquier funcionalidad que no haya quedado terminada.
