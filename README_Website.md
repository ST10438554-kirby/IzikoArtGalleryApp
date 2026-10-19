# Iziko Museum – Website

A web version of the Iziko Museum Android app, with a frontend and a backend. It uses the same colours,
fonts, images, screens, data and API as the app.

## What's inside

```
IzikoMuseumWeb/
├── README.md
└── server/
    ├── app.py            Backend: REST API (SQLite) + serves the website
    ├── seed.json         Artworks, artists and exhibitions (loaded into the database on first run)
    └── web/              Frontend
        ├── index.html
        ├── style.css
        ├── app.js
        ├── museum_seed.json   Saved copy of the data, used if the server can't be reached
        └── images/            Artwork, artist and exhibition pictures, logo
```

The database file `iziko.db` is created automatically the first time the server starts.

## Requirements

- Python 3.8 or newer. Nothing else to install.
- Any modern web browser.

On Windows, download Python from https://www.python.org/downloads/ and tick
**"Add python.exe to PATH"** in the installer. Then open a new Command Prompt.

## Run it

1. Unzip `IzikoMuseumWeb.zip`.
2. Open a terminal inside the `server` folder. On Windows: open the folder in File Explorer,
   click the address bar, type `cmd` and press Enter.
3. Start the server:

   ```
   python app.py
   ```

   (Use `python3 app.py` on Mac/Linux, or `py app.py` on Windows if `python` isn't found.)
4. Open **http://localhost:8000** in your browser.
5. Press **Ctrl+C** in the terminal to stop the server.

### Options

```
python app.py --port 9000     # use a different port
python app.py --reset         # delete iziko.db and re-create it from seed.json
python app.py --host 127.0.0.1  # only allow this computer (default is 0.0.0.0, the whole network)
```

### View it on a phone

Put the phone on the same Wi-Fi as the computer and open `http://<computer's IP address>:8000`,
for example `http://192.168.1.20:8000`. The computer's firewall must allow port 8000.

## Screens

| Screen | What it does |
|---|---|
| Welcome | Logo, "Log in" and "Create an account" |
| Log in / Register | Account forms (password at least 8 characters) |
| Home | Hero photo, Explore tiles, Now showing, Featured works |
| Explore Artwork | Search by title or artist, filter by category |
| Artwork detail | Image, facts, description, link to the artist |
| Discover Artists / Artist detail | Artist list, biography and their artworks |
| Exhibitions | Current exhibitions and "Plan your visit" |
| Settings | Account, dark mode, notifications, server address, about |

You must be logged in to see anything beyond the Welcome, Log in and Register screens.
Dark mode follows your device by default; the Settings switch overrides it.

## API

All responses are JSON. Errors look like `{"error": "message"}` with a matching HTTP status.

| Method | Path | Notes |
|---|---|---|
| GET | `/api/health` | `{"status":"ok"}` |
| GET | `/api/artworks` | optional `?q=text&category=Painting&featured=1` |
| GET | `/api/artworks/<id>` | |
| GET | `/api/categories` | |
| GET | `/api/artists` | |
| GET | `/api/artists/<id>` | includes that artist's `artworks` |
| GET | `/api/exhibitions` | |
| POST | `/api/auth/register` | `{"fullName","email","password"}` |
| POST | `/api/auth/login` | `{"email","password"}` returns `{"token","user"}` |
| POST | `/api/auth/logout` | header `Authorization: Bearer <token>` |
| GET | `/api/me` | header `Authorization: Bearer <token>` |

## Changing the content

1. Edit `server/seed.json`. Each artwork, artist and exhibition has an `image` field with a file name
   (without extension) from `server/web/images`, or `null`.
2. Copy it over the offline copy: `server/web/museum_seed.json`.
3. Run `python app.py --reset`. This also deletes registered users.

To add a picture, put it in `server/web/images/` as `.jpg` and use its name in `image`.
(`.png` and `.webp` files need an entry in `IMG_EXT` at the top of `web/app.js`.)

## Before putting it online

- Serve it over HTTPS (behind nginx or Caddy, or a host that provides it).
- Add rate limiting to the login and register endpoints.
- Back up `iziko.db`, which holds the user accounts.

## Troubleshooting

| Problem | Fix |
|---|---|
| "Python was not found" | Install Python and tick "Add to PATH", then open a new terminal |
| Page won't load | Make sure the terminal still shows the server running, and use `http://`, not `https://` |
| Port already in use | Run `python app.py --port 9000` and open `http://localhost:9000` |
| Old version showing | Hard-refresh: Ctrl+Shift+R (Cmd+Shift+R on Mac) |
| Can't log in after `--reset` | The accounts were deleted; register again |
