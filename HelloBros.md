# Hello Bros ( Real time app for Connecting with  Brothers from all over the world)

## Goal

Free (no cost, no card), desktop Java app with:

- remote chat
- games 
- file transfer
- users connect over internet
- group chat
- audio /vedio chat (not priority , future thought)

* * *

## Final Architecture (Chosen)

### 1\. Client

- Java Swing app / (Swtich to something better later , we will build with that on mind)
- Handles UI + logic
- Sends/receives network packets

* * *

### 2\. Transport (Core Decision)

- **Peer-to-Peer (P2P) direct connection**
- No permanent relay server for data

* * *

### 3\. Signaling Layer (ONLY required backend)

- Minimal “coordination server”

- Used for:
  
  - room creation
  - peer discovery
  - exchanging connection info

- Does NOT handle chat/files/game data

* * *

## Connection Flow

1. User A creates room
2. Signaling server generates room ID
3. User B joins using room ID
4. Server exchanges connection info
5. A ↔ B connect directly (P2P)
6. Server drops out of data path
- We will add the feature where user can connect with each other without room id 

* * *

## Data Flow (After Connect)

Direct peer communication:

- chat messages
- game moves / datas
- file chunks

* * *

## File Transfer Plan

- split file into chunks
- send via P2P stream
- reconstruct on receiver side

Packet types:

- FILE\_START
- FILE\_CHUNK
- FILE\_END

*Or any other better way , if extist, after thorough planning*

* * *

## Networking Stack

- Java Sockets (TCP initially)
- JSON message protocol
- optional upgrade later to UDP/WebRTC

* * *

## What We Have Now

- Swing UI started
- basic navigation system
- tunnel-based connection concept (to be replaced)

* * *

## What We Need To Build

### Client Side

- P2P connection manager
- protocol handler (JSON packets)
- file transfer module
- room join/create UI integration

### Server Side (Minimal)

- room registry
- peer matching
- signaling exchange only

* * *

## Key Design Rules

- server = only coordination
- no server data relay
- all heavy traffic = peer-to-peer
- everything works without paid services

* * *

## Limitations (Accepted)

- NAT failure possible on some networks (We will try to overcome it)
- fallback not implemented initially (We will keep on mind, and add time to time)
- reliability depends on P2P success rate ( We will try to have best success rate)

* * *

## Next Step

Build in this order: (Can be changed after planning)

1. signaling server (room + peer exchange)
2. P2P TCP connection
3. chat system
4. game sync protocol
5. file transfer system
