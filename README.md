# Iziko Museum

An app for exploring the **Iziko South African National Gallery** in Cape Town: its artworks, artists and
exhibitions. This repository contains the same experience in two forms, an **Android app** and a **website**,
both powered by one small REST API with a SQLite database.

## Repository layout

```
iziko-museum/
├── README.md                  This file
├── .gitignore
├── IzikoMuseum/               Android app (Kotlin) + the original API server
│   ├── README.md              Full Android guide
│   ├── app/                   App source, layouts, images, offline data
│   └── server/                Python API (app.py, seed.json)
└── IzikoMuseumWeb/            Website (frontend + backend)
    ├── README.md              Full website guide
    └── server/
        ├── app.py             REST API + serves the website
        ├── seed.json          Artworks, artists, exhibitions
        └── web/               index.html, style.css, app.js, images/


| Project | What it is | Guide |
|---|---|---|
| Website | Web version with the same look, screens and data as the app | [IzikoMuseumWeb/README.md](IzikoMuseumWeb/README.md) |
| Android app | Native app, falls back to bundled data when offline | [IzikoMuseum/README.md](IzikoMuseum/README.md) |

## Features (both versions)

- Create an account, log in and log out
- Home screen with a hero photo, Explore tiles, the current exhibition and featured works
- Explore Artwork: search by title or artist and filter by category
- Artwork detail pages and artist profiles with their works
- Exhibitions and "Plan your visit" information
- Settings: account, dark mode, notifications, server address, about
- Light and dark themes in the gallery's orange, red and green palette
- Offline fallback: if the server can't be reached, saved data is shown with a notice

## Requirements

| To run | You need |
|---|---|
| Website | Python 3.8 or newer and a web browser (nothing else to install) |
| Android app | Android Studio, JDK 11+, an emulator or a phone, plus Python 3.8+ for the server |

On Windows, install Python from https://www.python.org/downloads/ and tick
**"Add python.exe to PATH"** in the installer. Then open a new Command Prompt.

## Quick start: website

1. Open a terminal in `IzikoMuseumWeb/server`.
   (Windows: open that folder in File Explorer, click the address bar, type `cmd`, press Enter.)
2. Start the server:

   python app.py

   Use `python3 app.py` on Mac/Linux, or `py app.py` on Windows if `python` isn't found.
3. Open **http://localhost:8000** in your browser.
4. Register an account, then explore. Press **Ctrl+C** in the terminal to stop.

Handy options:

python app.py --port 9000    # different port
python app.py --reset        # wipe the database and re-create it from seed.json


## Quick start: Android app

1. Start the API server:

   cd IzikoMuseum/server
   python app.py

2. In Android Studio choose **File > Open** and select the `IzikoMuseum` folder. Wait for Gradle sync.
3. Pick an emulator or a connected phone and press **Run**.

| Where the app runs | Server address in the app |
|---|---|
| Android emulator | `http://10.0.2.2:8000` (default, nothing to change) |
| Real phone on the same Wi-Fi | `http://<your computer's IP>:8000`, set under **Settings > Server > Save and test** |

The computer's firewall must allow port 8000 for a real phone.

## View the website on your phone

Put the phone on the same Wi-Fi as the computer running the server and open
`http://<your computer's IP>:8000`, for example `http://192.168.1.20:8000`.

## API

Both projects use the same API. Responses are JSON, and errors look like `{"error": "message"}` with a
matching HTTP status.

| Method | Path | Notes |
|---|---|---|
| GET | `/api/health` | `{"status":"ok"}` |
| GET | `/api/artworks` | optional `?q=text&category=Painting&featured=1` |
| GET | `/api/artworks/<id>` | |
| GET | `/api/categories` | |
| GET | `/api/artists` | |
| GET | `/api/artists/<id>` | includes that artist's `artworks` |
| GET | `/api/exhibitions` | |
| POST | `/api/auth/register` | `{"fullName","email","password"}` (8+ characters) |
| POST | `/api/auth/login` | `{"email","password"}` returns `{"token","user"}` |
| POST | `/api/auth/logout` | header `Authorization: Bearer <token>` |
| GET | `/api/me` | header `Authorization: Bearer <token>` |

## Changing the content

The data lives in `seed.json`. Each artwork, artist and exhibition has an `image` field holding a picture
name (without extension) or `null`.

1. Edit `seed.json` in the project you want to change.
2. Keep the offline copies in step:
   - Android: copy it to `IzikoMuseum/app/src/main/assets/museum_seed.json`
   - Website: copy it to `IzikoMuseumWeb/server/web/museum_seed.json`
3. Run `python app.py --reset` in that project's `server` folder. This also deletes registered users.

Pictures go in `IzikoMuseum/app/src/main/res/drawable-nodpi/` (Android) and
`IzikoMuseumWeb/server/web/images/` (website). The two projects have separate databases, so accounts
created in one don't exist in the other unless both use the same running server.

## Security notes

- Passwords are stored as salted PBKDF2-SHA256 hashes, and sessions use random tokens that expire after 30 days.
- Before going public, serve the API over **HTTPS** (nginx, Caddy, or a host that provides it).
  Then remove `android:usesCleartextTraffic="true"` from the Android manifest.
- Add **rate limiting** to the login and register endpoints.
- Back up `iziko.db`, which holds the user accounts.

## Troubleshooting

| Problem | Fix |
|---|---|
| "Python was not found" | Install Python with "Add to PATH" ticked, then open a new terminal |
| Website won't load | Check the server is still running and use `http://`, not `https://` |
| Port already in use | Run `python app.py --port 9000` and use that port |
| Old version showing in the browser | Hard-refresh with Ctrl+Shift+R (Cmd+Shift+R on Mac) |
| Android app shows "Can't reach the server" | Start the server, check the address in Settings, and on a real phone use your computer's IP, not `10.0.2.2` |
| Phone can't connect | Same Wi-Fi network, and allow port 8000 through the firewall |
| Gradle sync fails | Update Android Studio and make sure it uses JDK 11 or newer |
| Can't log in after `--reset` | The accounts were deleted; register again |

## About

Content is based on the Iziko South African National Gallery, Government Avenue, Company Garden,
Cape Town 8001 (www.iziko.org.za). This is a student project and is not an official Iziko application.
