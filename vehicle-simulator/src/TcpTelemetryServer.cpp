#include "TcpTelemetryServer.h"

#include <cerrno>
#include <cstring>
#include <fcntl.h>
#include <iostream>
#include <netinet/in.h>
#include <sys/socket.h>
#include <unistd.h>

TcpTelemetryServer::TcpTelemetryServer(std::uint16_t port) : port_(port) {}

TcpTelemetryServer::~TcpTelemetryServer() {
    closeClient();
    if (listenFd_ >= 0) ::close(listenFd_);
}

bool TcpTelemetryServer::open() {
    listenFd_ = ::socket(AF_INET, SOCK_STREAM, 0);
    if (listenFd_ < 0) {
        std::perror("socket(TCP)");
        return false;
    }

    int reuse = 1;
    ::setsockopt(listenFd_, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse));
    const int flags = ::fcntl(listenFd_, F_GETFL, 0);
    ::fcntl(listenFd_, F_SETFL, flags | O_NONBLOCK);

    sockaddr_in address{};
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(port_);
    if (::bind(listenFd_, reinterpret_cast<sockaddr*>(&address), sizeof(address)) < 0) {
        std::perror("bind(TCP)");
        return false;
    }
    if (::listen(listenFd_, 1) < 0) {
        std::perror("listen(TCP)");
        return false;
    }

    std::cout << "TCP telemetry listening on 0.0.0.0:" << port_ << "\n";
    return true;
}

void TcpTelemetryServer::pollForClient() {
    if (listenFd_ < 0) return;
    sockaddr_in clientAddress{};
    socklen_t length = sizeof(clientAddress);
    const int accepted = ::accept4(listenFd_, reinterpret_cast<sockaddr*>(&clientAddress), &length, SOCK_NONBLOCK);
    if (accepted >= 0) {
        closeClient();
        clientFd_ = accepted;
        std::cout << "Android telemetry client connected\n";
    }
}

void TcpTelemetryServer::sendLine(const std::string& line) {
    pollForClient();
    if (clientFd_ < 0) return;

    const std::string wire = line + "\n";
    const ssize_t sent = ::send(clientFd_, wire.data(), wire.size(), MSG_NOSIGNAL);
    if (sent < 0 && errno != EAGAIN && errno != EWOULDBLOCK) {
        std::cout << "Android telemetry client disconnected\n";
        closeClient();
    }
}

void TcpTelemetryServer::closeClient() {
    if (clientFd_ >= 0) {
        ::close(clientFd_);
        clientFd_ = -1;
    }
}
