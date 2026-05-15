# Changelog

All notable changes to this project will be documented in this file.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]
### Added
- Java Swing don't have good emoji support. Swing will be relplace.
- Mobile Support

---

## [1.2.1] - 2026-05-15

### Added
- Right-click on message → emoji picker
- Count displays beside the message (👍 2)
- Message IDs synced across clients so reactions match correctly
- ### Changed
- Chat Screeen UI updated to support emoji

---

## [1.2.0] - 2026-05-14

### Added
- Room ID system — share a 6-character code (e.g. `X4K9P2`) instead of a port number
- Auto-reconnect — app reconnects automatically if the connection drops
- Tic Tac Toe — play with your friend directly in the app
- Game request flow — opponent gets a confirmation dialog before a new game starts
- Random first player on each new game
- Lobby screen — choose between Chat and Tic Tac Toe after connecting
- File sharing — send files via the 📎 button in chat
- Chat history — saved to `chatlog.txt` automatically
- Name generator — randomize your display name on the startup screen

### Changed
- Startup screen redesigned — Host/Join flow with always-visible Room ID field
- Replaced manual port sharing with Room ID system

### Fixed
- Chat history no longer resets when switching screens
- Tunnel address now shown in lobby before entering chat
- Fixed duplicate reconnect attempts on unstable connections

---

## [1.1.0] - 2026-05-12

### Added
- Automated Windows and Linux builds via CI/CD
- Screen navigation system
- README with download links and usage guide

### Changed
- Reorganized project structure

---

## [1.0.0] - 2026-05-12

### Added
- Initial release
- Host/Join peer-to-peer chat
- NAT traversal via tunneling