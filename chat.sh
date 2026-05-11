#!/bin/bash
cd "$(dirname "$0")"
# ─────────────────────────────────────────
#         Java Chat App - Start Script
# ─────────────────────────────────────────

SECRET="chatbros"   # change this to anything, keep it consistent
PORT=4999

show_help() {
    echo ""
    echo "Usage: ./chat.sh [command]"
    echo ""
    echo "  build        Compile and package JARs"
    echo "  server       Start the chat server"
    echo "  client       Start the chat client (localhost)"
    echo "  tunnel       Start bore tunnel (for remote access)"
    echo "  remote       Start client with custom host and port"
    echo ""
    echo "Examples:"
    echo "  ./chat.sh build"
    echo "  ./chat.sh server"
    echo "  ./chat.sh tunnel"
    echo "  ./chat.sh remote bore.pub 26485"
    echo ""
}

build() {
    echo "Compiling..."
    javac -d out src/ChatServer.java src/ChatClient.java
    if [ $? -ne 0 ]; then
        echo "Compilation failed."
        exit 1
    fi
    echo "Building JARs..."
    jar cfe ChatServer.jar ChatServer -C out .
    jar cfe ChatClient.jar ChatClient -C out .
    echo "Done. ChatServer.jar and ChatClient.jar ready."
}

start_server() {
    echo "Starting server on port $PORT..."
    java -jar ChatServer.jar
}

start_client_local() {
    echo "Connecting to localhost:$PORT..."
    java -jar ChatClient.jar localhost $PORT
}

start_tunnel() {
    if ! command -v ./bore &> /dev/null; then
        echo "bore not found. Make sure bore binary is in this folder."
        exit 1
    fi
    echo "Starting bore tunnel on port $PORT..."
    echo "Share the port number with your friend once connected."
    echo "─────────────────────────────────────────"

    # keep tunnel alive by pinging it every 25 seconds in background
    while true; do
        sleep 25
        nc -zw1 bore.pub $PORT 2>/dev/null
    done &
    PING_PID=$!

    ./bore local $PORT --to bore.pub

    # kill ping loop when bore exits
    kill $PING_PID 2>/dev/null
}

start_client_remote() {
    if [ $# -eq 1 ]; then
        HOST="bore.pub"
        RPORT=$1
    elif [ $# -eq 2 ]; then
        HOST=$1
        RPORT=$2
    else
        echo "Usage: ./chat.sh remote <port>"
        echo "       ./chat.sh remote <host> <port>"
        exit 1
    fi
    echo "Connecting to $HOST:$RPORT..."
    java -jar ChatClient.jar $HOST $RPORT
}

# ─── Main ───────────────────────────────

case "$1" in
    build)   build ;;
    server)  start_server ;;
    client)  start_client_local ;;
    tunnel)  start_tunnel ;;
    remote)  start_client_remote $2 $3 ;;
    *)       show_help ;;
esac