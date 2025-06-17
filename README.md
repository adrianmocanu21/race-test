# Race Betting Application

A Spring Boot application for managing race betting with integration to OpenF1 API.

## Prerequisites

- Docker and Docker Compose installed
- Java 21
- Maven

## Running the Application

1. Build and run the application using Docker Compose:
```bash
docker compose up --build
```

2. The application will be available at `http://localhost:8080`

## API Endpoints

### Event Controller

#### Get Events
- **Endpoint**: `GET /events`
- **Description**: Retrieves a list of events based on optional filters
- **Parameters**:
  - `year`: Integer - Filter by year
  - `country`: String - Filter by country
  - `sessionType`: String - Filter by session type
- **Response**: List of `EventResponseDTO` objects

#### Settle Event Outcome
- **Endpoint**: `POST /events/{sessionKey}/settle`
- **Description**: Settles the outcome of a specific event and processes bets
- **Parameters**:
  - `sessionKey`: Integer - The session key of the event
- **Response**: `EventResultDTO` containing the winning driver information
- **Important Note**: When called multiple times for the same session, the bets information and user balances will remain unchanged from the first settlement. Only the winner driver will be re-calculated (randomly selected) on each call, as the session outcome is not persisted in the database.

### Bet Controller

#### Place Bet
- **Endpoint**: `POST /bets`
- **Description**: Places a new bet on a specific driver
- **Request Body**: `PlaceBetRequestDTO`
  - `userId`: String - The ID of the user placing the bet
  - `sessionKey`: Integer - The session key of the event
  - `driverNumber`: Integer - The number of the driver being bet on
  - `amount`: Integer - The amount being bet
- **Response**: `PlaceBetResponseDTO` containing the bet details
- **Restrictions**: Each user can place only one bet per event

## Development

The application is built using Spring Boot and follows RESTful API design principles. It includes:

### Potential Improvements

1. **Input Validation**
2. **Feign client exception handling**

## Error Handling

The application handles the following exceptions:
- `EventNotFoundException`: When trying to access a non-existent event
- `DriverNotFoundException`: When trying to place a bet on a non-existent driver
- `DuplicateBetException`: When a user tries to place multiple bets on the same event
- `InsufficientBalanceException`: When a user doesn't have enough balance to place a bet
- `UserNotFoundException`: When trying to access a non-existent user
- `BetNotFoundException`: When trying to access a non-existent bet

## Database

The application uses an in-memory database (H2) seeded with three test users:
- user1 (balance: 100)
- user2 (balance: 100)
- user3 (balance: 100)

## External Services

The application integrates with OpenF1 API for:
- Retrieving race events
- Getting driver information
- Retrieving race positions

## Development


The application is built using Spring Boot and follows RESTful API design principles. It includes:
- Docker support for containerization
- Integration with OpenF1 API using Feign Client
- JPA for database operations
- Comprehensive exception handling
- Unit tests for service layer
