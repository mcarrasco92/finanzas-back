# Frontend Implementation Guide: Spaces

This guide describes all frontend changes required to support the multi-space architecture
introduced in the backend. The app is Angular at `http://localhost:4200`.

---

## Overview of changes

| Area | What changes |
|---|---|
| HTTP Interceptor | Add `X-Space-Id` header to every authenticated request |
| Auth flow | After login, load the user's spaces and select one |
| Space selection | UI to switch between spaces |
| Space management | Create shared spaces, invite members, accept invitations |
| Registration | No changes — backend creates the personal space automatically |

---

## 1. Data models (TypeScript interfaces)

Create or extend the models file with the following types:

```typescript
// Space
export interface Space {
  spaceId: string;
  name: string;
  type: 'personal' | 'shared';
  ownerId: string;
  role: string;         // role of the currently logged-in user in this space
}

// Member
export interface Member {
  userId: string;
  role: string;         // 'owner' | 'admin' | 'viewer'
  joinedAt: string;
}

// Invitation
export interface Invitation {
  token: string;
  spaceId: string;
  invitedEmail: string;
  role: string;
  status: string;       // 'pending' | 'accepted' | 'expired'
  expiresAt: string;
}

// Request bodies
export interface CreateSpaceRequest {
  name: string;
}

export interface CreateInvitationRequest {
  email: string;
  role: string;         // 'admin' | 'viewer'
}
```

---

## 2. SpaceService

Create `src/app/services/space.service.ts`. All calls use the existing `ApiService` or
`HttpClient` — no `X-Space-Id` header is needed on space-management endpoints that use the
path `{spaceId}` directly in the URL, but the interceptor (section 3) will add it anyway.

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

const BASE = '/api';
const SPACE_KEY = 'selectedSpaceId';

@Injectable({ providedIn: 'root' })
export class SpaceService {

  private _activeSpace$ = new BehaviorSubject<Space | null>(null);
  activeSpace$ = this._activeSpace$.asObservable();

  constructor(private http: HttpClient) {
    // Restore selection from previous session
    const savedId = localStorage.getItem(SPACE_KEY);
    if (savedId) {
      // The interceptor reads from activeSpaceId(), so we restore it early.
      // The full Space object will be loaded during the post-login flow.
    }
  }

  // --- Read ---

  getSpaces(): Observable<GenericResponse<Space[]>> {
    return this.http.get<GenericResponse<Space[]>>(`${BASE}/spaces`);
  }

  getMembers(spaceId: string): Observable<GenericResponse<Member[]>> {
    return this.http.get<GenericResponse<Member[]>>(`${BASE}/spaces/${spaceId}/members`);
  }

  getPendingInvitations(email: string): Observable<GenericResponse<Invitation[]>> {
    return this.http.get<GenericResponse<Invitation[]>>(
      `${BASE}/invitations/pending`, { params: { email } }
    );
  }

  // --- Write ---

  createSpace(body: CreateSpaceRequest): Observable<GenericResponse<Space>> {
    return this.http.post<GenericResponse<Space>>(`${BASE}/spaces`, body);
  }

  inviteMember(spaceId: string, body: CreateInvitationRequest): Observable<GenericResponse<Invitation>> {
    return this.http.post<GenericResponse<Invitation>>(
      `${BASE}/spaces/${spaceId}/invitations`, body
    );
  }

  acceptInvitation(token: string): Observable<GenericResponse<void>> {
    return this.http.post<GenericResponse<void>>(
      `${BASE}/invitations/${token}/accept`, {}
    );
  }

  // --- Space selection ---

  setActiveSpace(space: Space): void {
    this._activeSpace$.next(space);
    localStorage.setItem(SPACE_KEY, space.spaceId);
  }

  activeSpaceId(): string | null {
    return this._activeSpace$.value?.spaceId ?? localStorage.getItem(SPACE_KEY);
  }

  clearActiveSpace(): void {
    this._activeSpace$.next(null);
    localStorage.removeItem(SPACE_KEY);
  }
}
```

---

## 3. HTTP Interceptor — add `X-Space-Id` to all requests

Create `src/app/interceptors/space.interceptor.ts`:

```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest, HttpHandler, HttpEvent, HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { SpaceService } from '../services/space.service';

@Injectable()
export class SpaceInterceptor implements HttpInterceptor {

  constructor(private spaceService: SpaceService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const spaceId = this.spaceService.activeSpaceId();

    if (spaceId) {
      req = req.clone({
        setHeaders: { 'X-Space-Id': spaceId }
      });
    }

    return next.handle(req);
  }
}
```

Register the interceptor in `app.module.ts` (or the providers array if using standalone):

```typescript
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { SpaceInterceptor } from './interceptors/space.interceptor';

providers: [
  // ... existing interceptors (JWT, etc.) first
  { provide: HTTP_INTERCEPTORS, useClass: SpaceInterceptor, multi: true },
]
```

> **Order matters**: the JWT interceptor should run before the space interceptor so that both
> headers are set before the request is sent.

---

## 4. Post-login flow

After the user logs in and the app receives the JWT, load the user's spaces immediately
**before** navigating to the main page.

### In your `AuthService` or login handler:

```typescript
login(firebaseToken: string): Observable<void> {
  return this.http.post<GenericResponse<string>>('/api/users/create-token', { token: firebaseToken })
    .pipe(
      tap(resp => {
        // 1. Save the JWT
        localStorage.setItem('jwt', resp.data);
      }),
      switchMap(() =>
        // 2. Load spaces
        this.spaceService.getSpaces()
      ),
      tap(resp => {
        const spaces: Space[] = resp.data;

        if (spaces.length === 1) {
          // Auto-select the only space (typically the personal one)
          this.spaceService.setActiveSpace(spaces[0]);
          this.router.navigate(['/home']);
        } else {
          // Show space selector screen
          this.router.navigate(['/select-space'], { state: { spaces } });
        }
      }),
      map(() => void 0)
    );
}
```

---

## 5. Space selector screen

Route: `/select-space`

This screen is shown when a user has more than one space (e.g., personal + shared teams).

```typescript
// select-space.component.ts
@Component({ ... })
export class SelectSpaceComponent {
  spaces: Space[] = history.state['spaces'] ?? [];

  constructor(
    private spaceService: SpaceService,
    private router: Router
  ) {}

  select(space: Space): void {
    this.spaceService.setActiveSpace(space);
    this.router.navigate(['/home']);
  }
}
```

```html
<!-- select-space.component.html -->
<h2>Selecciona un espacio</h2>
<ul>
  <li *ngFor="let space of spaces" (click)="select(space)">
    <strong>{{ space.name }}</strong>
    <span>{{ space.type }}</span>
    <span>Tu rol: {{ space.role }}</span>
  </li>
</ul>
```

Add a space switcher widget in the app shell (header/sidebar) that calls the same
`setActiveSpace()` and reloads the active page.

---

## 6. Space management screens

### 6a. Create shared space

A simple form/dialog anywhere in the settings area:

```typescript
createSpace(name: string): void {
  this.spaceService.createSpace({ name }).subscribe(resp => {
    // Optionally switch to the new space
    this.spaceService.setActiveSpace(resp.data);
    this.router.navigate(['/home']);
  });
}
```

### 6b. Members list

Route: `/spaces/members` (or a tab in Settings)

```typescript
ngOnInit(): void {
  const spaceId = this.spaceService.activeSpaceId()!;
  this.spaceService.getMembers(spaceId).subscribe(resp => {
    this.members = resp.data;
  });
}
```

### 6c. Invite a member

Role options: `'admin'` or `'viewer'`.
Only users with role `owner` or `admin` may invite (the backend enforces this).

```typescript
invite(email: string, role: string): void {
  const spaceId = this.spaceService.activeSpaceId()!;
  this.spaceService.inviteMember(spaceId, { email, role }).subscribe(resp => {
    // Show success message: invitation sent
  });
}
```

### 6d. Accept an invitation

The invitation token can arrive via:
- A deep link (e.g. `https://app.com/invitations/accept?token=<UUID>`)
- A notification or email link

```typescript
// InvitationAcceptComponent
ngOnInit(): void {
  const token = this.route.snapshot.queryParams['token'];
  this.spaceService.acceptInvitation(token).subscribe(resp => {
    // Reload spaces list so the new space appears
    this.spaceService.getSpaces().subscribe(r => {
      const newSpace = r.data.find(s => /* identify new space */);
      if (newSpace) this.spaceService.setActiveSpace(newSpace);
      this.router.navigate(['/home']);
    });
  });
}
```

### 6e. Pending invitations (on login)

After loading spaces, also check for pending invitations for the logged-in user's email:

```typescript
// After login, alongside loading spaces
this.spaceService.getPendingInvitations(userEmail).subscribe(resp => {
  if (resp.data?.length) {
    // Show a banner or notification: "You have X pending invitations"
  }
});
```

---

## 7. Logout

Clear the selected space on logout:

```typescript
logout(): void {
  localStorage.removeItem('jwt');
  this.spaceService.clearActiveSpace();
  this.router.navigate(['/login']);
}
```

---

## 8. Error handling for 403 (invalid space membership)

The backend throws a `RuntimeException("403: ...")` when `validateMembership` fails.
The `GeneralService.handleExcepcion` wraps it in `GenericResponse` with some error code.

Add a case in your global error handler / response interceptor:

```typescript
// In your HTTP response interceptor
if (resp.body?.coderr === '403' || resp.body?.message?.startsWith('403:')) {
  this.spaceService.clearActiveSpace();
  this.router.navigate(['/select-space']);
}
```

---

## 9. Checklist

- [ ] `SpaceService` created with all methods
- [ ] `SpaceInterceptor` created and registered (after JWT interceptor)
- [ ] `activeSpaceId()` persists to `localStorage` so page refresh keeps the selection
- [ ] Post-login flow loads spaces before routing to home
- [ ] Space selector screen (`/select-space`) implemented
- [ ] Space switcher widget in app shell (header or sidebar)
- [ ] Create shared space form
- [ ] Members list view
- [ ] Invite member form (owner/admin only)
- [ ] Accept invitation flow (`/invitations/accept?token=...`)
- [ ] Pending invitations check on login
- [ ] Logout clears space selection
- [ ] 403 membership error handled globally

---

## API reference

| Method | Endpoint | Auth | X-Space-Id needed | Description |
|---|---|---|---|---|
| GET | `/api/spaces` | JWT | No | List user's spaces |
| POST | `/api/spaces` | JWT | No | Create shared space |
| GET | `/api/spaces/{spaceId}/members` | JWT | No | List members |
| POST | `/api/spaces/{spaceId}/invitations` | JWT | No | Invite member |
| POST | `/api/invitations/{token}/accept` | JWT | No | Accept invitation |
| GET | `/api/invitations/pending?email=` | JWT | No | Pending invitations |
| ALL other `/api/*` | — | JWT | **Yes** | All existing endpoints |

> Space-management endpoints do not need `X-Space-Id` because the space is identified by the
> path parameter or derived from the JWT. The interceptor will add the header anyway — that
> is harmless.
