# CampusSwap Backend (Java + Spring Boot)

REST API for the CampusSwap frontend: student accounts, item listings, item requests
(accept / reject) and notifications. Spring Boot 3.3, Java 17+, Spring Data JPA,
Spring Security with JWT, H2 by default (MySQL supported).

## Run

```bash
mvn spring-boot:run          # needs JDK 17+ and Maven
```

API runs at `http://localhost:8080`. On first start a demo account is created
(same as the frontend README): **student@campus.edu / Password123!**, plus a second
student (`riya@campus.edu`, same password) with a few listings.
Data is stored in `./data/campusswap` (H2 file). Delete that folder to reset.

Environment variables for real deployments: `JWT_SECRET` (32+ chars), `CORS_ORIGINS`
(e.g. `https://campusswap.example.edu`). To use MySQL, follow the comments in
`application.properties`. Set `app.seed-demo-data=false` to skip demo data.

## Structure

```
com.campusswap
├── CampusSwapApplication
├── controller   AuthController, ItemController, RequestController, NotificationController
├── service      AuthService, ItemService, RequestService, NotificationService
├── repository   UserRepository, ItemRepository, RequestRepository, NotificationRepository
├── model        User, Item, Request, Notification, RequestStatus, NotificationType
├── dto          request/response records (JSON shapes, validation)
├── security     JwtService, JwtAuthFilter
├── config       SecurityConfig (JWT, CORS, BCrypt), DataSeeder
└── exception    ApiException, GlobalExceptionHandler
```

Added beyond your original tree: `NotificationService` / `NotificationRepository`
(needed by `NotificationController`), plus `dto`, `security`, `config`, `exception`.

## Endpoints

All except signup/login need `Authorization: Bearer <token>`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/signup` | `{name,email,collegeId,phone,branch,year,password}` |
| POST | `/api/auth/login` | `{email,password}` → `{token, user}` |
| GET | `/api/auth/me` | current user |
| GET | `/api/items?category=&search=` | browse (category `All` = no filter) |
| GET | `/api/items/mine` · `/api/items/{id}` | my listings · one item |
| POST | `/api/items` | `{name,category,description,condition,location,listingType}` |
| DELETE | `/api/items/{id}` | owner only |
| POST | `/api/requests` | `{itemId}` |
| GET | `/api/requests/incoming` · `/outgoing` | requests for my items · requests I sent |
| PATCH | `/api/requests/{id}/status` | `{status:"Accepted"\|"Rejected"}`, owner only |
| GET | `/api/notifications` · `/unread-count` | list · badge count |
| PATCH | `/api/notifications/{id}/read` · `/read-all` | mark read |

Errors always look like `{"status":409,"error":"You have already requested this item."}`.

## Business rules (moved from the frontend into the server)

- You can't request your own item, or the same item twice (a rejected request can be re-sent).
- Only the item's owner can accept/reject, and only while the request is Pending.
- A new request notifies the owner; accept/reject notifies the requester.
- Accepting marks the item `available=false`, auto-rejects other pending requests for it,
  and reveals both students' phone numbers on that request.
- Deleting a listing cancels its requests and notifies pending requesters.
- Login errors are deliberately generic ("Invalid email or password.") so accounts can't be probed.

## Connecting the frontend

`frontend-integration/api.js` is a ready-made client. Copy it next to your HTML files,
add `<script src="api.js"></script>` before the other scripts, then swap the localStorage
code for API calls, e.g.:

```js
// script.js (login)        const user = await CampusSwapAPI.login(email, password);
// dashboard.js getItems()  allItems = await CampusSwapAPI.getItems();
// createRequest(item)      await CampusSwapAPI.createRequest(item.id);
// updateRequestStatus()    await CampusSwapAPI.updateRequestStatus(id, "Accepted");
// list-item.js submit      await CampusSwapAPI.createItem({name, category, description, condition, location, listingType});
```

Response objects keep the same field names the frontend used in localStorage
(`ownerEmail`, `requesterName`, `status: "Pending"`, `read`, ...), so the rendering code
needs very little change. The render functions just become `async`, since data now arrives over HTTP.
