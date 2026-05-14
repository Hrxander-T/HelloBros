# Changelog

All notable changes to this project will be documented in this file.

## [1.2.0] - 2026-05-14

### Added
- Signaling server on Cloudflare Workers for room management
- Room ID system (6-character alphanumeric e.g. X4K9P2)
- Auto-reconnect when  tunnel drops — client polls signaling server for new port
- Tic Tac Toe with full network sync
- New game request/accept flow with confirmation dialog
- Random first player on new game
- Lobby screen — choose between Chat and Game after connecting
- File sending via paperclip button in chat
- Chat history saved to chatlog.txt
- Protocol constants via Protocol.java — no magic strings
- Name generator with randomize button on startup screen
-  tunnel retry logic with max attempts

### Changed
- Startup screen redesigned — Host/Join flow with always-visible Room ID field
- Port replaced by Room ID — no manual port sharing needed
- NetworkManager refactored — owns all network lifecycle (server, client, tunnel, signaling)
- Server.stop() properly releases port on exit
- Shutdown hook cleans up room on signaling server on app exit

### Fixed
- ChatScreen panel built once — chat history preserved on navigation
- Tunnel address now shown in lobby before entering chat
- Multiple reconnect pollers prevented with reconnecting flag

---

## [1.1.0] - 2026-05-12

### Added
- CI/CD pipeline with GitHub Actions workflows for Windows and Linux builds
- Navigation system for screen transitions
- README documentation

### Changed
- Reorganized codebase into proper folder structure
- Refactored Screen class to use Navigator

---

## [1.0.0] - 2026-05-12

### Added
- Initial release
- Host/Join peer-to-peer chat functionality
- Tunneling for NAT traversal