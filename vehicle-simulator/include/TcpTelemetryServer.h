#pragma once

#include <cstdint>
#include <string>

class TcpTelemetryServer {
public:
    explicit TcpTelemetryServer(std::uint16_t port);
    ~TcpTelemetryServer();

    TcpTelemetryServer(const TcpTelemetryServer&) = delete;
    TcpTelemetryServer& operator=(const TcpTelemetryServer&) = delete;

    bool open();
    void pollForClient();
    void sendLine(const std::string& line);

private:
    void closeClient();
    std::uint16_t port_;
    int listenFd_{-1};
    int clientFd_{-1};
};
