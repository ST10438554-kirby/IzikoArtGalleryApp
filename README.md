# Iziko Museum – Android App

An Android app for exploring the Iziko South African National Gallery: its artworks, artists and
exhibitions. It talks to the Iziko Museum API (the Python server in `server/`) and shows a bundled
copy of the data if the server can't be reached.

- Package: `com.example.izikomuseum`
- Language: Kotlin, Android Views (XML layouts), Material Components
- Min SDK 24 (Android 7.0), target SDK 36, Java 11
- Version 1.0

## Features

- Welcome, register and log in (accounts are stored on the server)
- Home with a hero photo, Explore tiles, the current exhibition and featured works
- Explore Artwork: search by title or artist and filter by category
- Artwork and artist detail pages
- Exhibitions and "Plan your visit"
- Settings: account, dark mode, notifications, server address, about
- Offline fallback: if the server can't be reached, the app shows `assets/museum_seed.json` and a notice

## Project structure

```
IzikoMuseum/
├── app/src/main/
│   ├── java/com/example/izikomuseum/
│   │   ├── ui/            Activities (Main, Login, Register, Home, Artwork, Artists, Exhibitions, Settings...)
│   │   ├── data/model/    User, Artwork, Artist, Exhibition
│   │   ├── data/repository/  Auth, Museum and Settings repositories
│   │   └── data/remote/   ApiClient (HTTP) and Background (threading)
│   ├── res/               Layouts, colours, themes (light and dark), images
│   └── assets/museum_seed.json   Offline copy of the data
├── server/                Python API + database (see below)
└── gradle/                Version catalog and wrapper
```

## Requirements

- Android Studio (a recent version that supports Android Gradle Plugin 9.x)
- JDK 11 or newer (the one bundled with Android Studio is fine)
- Python 3.8+ to run the server
- An Android emulator, or a phone with USB debugging turned on

## Run it

### 1. Start the server

```
cd IzikoMuseum/server
python app.py
```

It runs on port 8000 and creates `iziko.db` from `seed.json` on first start.
(Use `python3` or `py` if `python` isn't found.)

### 2. Build and run the app

1. In Android Studio choose **File > Open** and select the `IzikoMuseum` folder.
2. Wait for Gradle sync to finish.
3. Pick an emulator or connected phone and press **Run**.

### 3. Server address

| Where the app runs | Server address |
|---|---|
| Android emulator | `http://10.0.2.2:8000` (the default, no change needed) |
| Real phone on the same Wi-Fi | `http://<your computer's IP>:8000` |

For a real phone, open the app, go to **Settings > Server**, enter the address and tap **Save and test**.
The computer's firewall must allow port 8000. A message "Connected to the server." confirms it works.

## Server API

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
| POST | `/api/auth/login` | returns `{"token","user"}` |
| POST | `/api/auth/logout` | header `Authorization: Bearer <token>` |
| GET | `/api/me` | header `Authorization: Bearer <token>` |

## Editing the data

1. Edit `server/seed.json`. Each item's `image` is the name of a picture in
   `app/src/main/res/drawable-nodpi` (for example `"butcher_boys"`), or `null`.
2. Re-create the database: `python app.py --reset` (this also deletes registered users).
3. Copy the data into the app's offline copy:

   ```
   cp server/seed.json app/src/main/assets/museum_seed.json
   ```
   (On Windows: `copy server\seed.json app\src\main\assets\museum_seed.json`)

## Before releasing

- Serve the API over HTTPS, then remove `android:usesCleartextTraffic="true"` from `AndroidManifest.xml`.
- Add rate limiting to the login and register endpoints.
- Replace the placeholder package name `com.example.izikomuseum` and create a signed release build.

## Troubleshooting

| Problem | Fix |
|---|---|
| App shows "Can't reach the server" | Start the server, check the address in Settings, and on a real phone use your computer's IP, not `10.0.2.2` |
| Phone can't connect | Same Wi-Fi network, and allow port 8000 through the firewall |
| Gradle sync fails | Update Android Studio, and make sure it is using JDK 11 or newer |
| Log in fails after `--reset` | The accounts were deleted; register again |
