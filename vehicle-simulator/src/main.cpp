#include "CanPublisher.h"
#include "TcpTelemetryServer.h"
#include "TelemetryGenerator.h"

#include <chrono>
#include <cstdlib>
#include <iostream>
#include <string>
#include <thread>

struct Options {
    std::string interfaceName{"vcan0"};
    int port{5555};
    int intervalMs{500};
    bool tcpOnly{false};
    bool once{false};
};

Options parseOptions(int argc, char** argv) {
    Options options;
    for (int i = 1; i < argc; ++i) {
        const std::string arg = argv[i];
        if (arg == "--interface" && i + 1 < argc) options.interfaceName = argv[++i];
        else if (arg == "--port" && i + 1 < argc) options.port = std::atoi(argv[++i]);
        else if (arg == "--interval-ms" && i + 1 < argc) options.intervalMs = std::atoi(argv[++i]);
        else if (arg == "--tcp-only") options.tcpOnly = true;
        else if (arg == "--once") options.once = true;
        else if (arg == "--help") {
            std::cout << "Usage: vehicle_telemetry_simulator [--interface vcan0] [--port 5555] "
                         "[--interval-ms 500] [--tcp-only] [--once]\n";
            std::exit(0);
        } else {
            std::cerr << "Unknown or incomplete option: " << arg << "\n";
            std::exit(2);
        }
    }
    if (options.port <= 0 || options.port > 65535 || options.intervalMs <= 0) {
        std::cerr << "Port and interval must be positive and valid\n";
        std::exit(2);
    }
    return options;
}

int main(int argc, char** argv) {
    const Options options = parseOptions(argc, argv);
    CanPublisher canPublisher(options.interfaceName);
    if (!options.tcpOnly && canPublisher.open()) {
        std::cout << "Publishing SocketCAN frames on " << options.interfaceName << "\n";
    } else if (!options.tcpOnly) {
        std::cout << "SocketCAN unavailable; continuing with TCP. Run scripts/setup_vcan.sh to enable it.\n";
    }

    TcpTelemetryServer tcpServer(static_cast<std::uint16_t>(options.port));
    if (!tcpServer.open()) return 1;

    TelemetryGenerator generator;
    do {
        for (const auto& frame : generator.next()) {
            if (canPublisher.available()) canPublisher.publish(frame);
            tcpServer.sendLine(frame.toText());
            std::cout << frame.toText() << '\n';
        }
        if (!options.once) std::this_thread::sleep_for(std::chrono::milliseconds(options.intervalMs));
    } while (!options.once);

    return 0;
}
