#pragma once

#include "VehicleFrame.h"
#include <string>

class CanPublisher {
public:
    explicit CanPublisher(std::string interfaceName);
    ~CanPublisher();

    CanPublisher(const CanPublisher&) = delete;
    CanPublisher& operator=(const CanPublisher&) = delete;

    bool open();
    bool publish(const VehicleFrame& frame) const;
    bool available() const { return socketFd_ >= 0; }

private:
    std::string interfaceName_;
    int socketFd_{-1};
};
