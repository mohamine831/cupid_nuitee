## Data Update Scheduler

I will create a DataUpdateScheduler service. The `DataUpdateScheduler` is a Spring Boot service that periodically refreshes property data from the Cupid API.
It runs on a configurable cron schedule (`cupid.update-scheduler.cron`) and only if
`cupid.update-scheduler.enabled=true` (defaults to enabled if missing).

On each run:
1. Retrieves all hotel IDs from the database via `PropertyRepository`.
2. Iterates through each hotel ID and calls `CupidFetchService.fetchAndSave(hotelId, 20)`
   to fetch and store up to 20 reviews per property.
3. Logs progress and catches errors per hotel to avoid interrupting the whole process.

### Potential Enhancements
- **Parallel Processing:** Use controlled concurrency to speed up large updates.
- **Dynamic Rate Limiting:** Implement adaptive backoff to avoid overloading the API.
- **Selective Refresh:** Skip properties that haven’t changed since the last update.
- **Metrics & Alerts:** Track failures, successes, and execution time for monitoring.
- **Batch Operations:** Reduce database calls with transactional batching for better performance.
