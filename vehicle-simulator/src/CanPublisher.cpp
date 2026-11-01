#include "CanPublisher.h"

#include <cstring>
#include <iostream>
#include <linux/can.h>
#include <linux/can/raw.h>
#include <net/if.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <unistd.h>

CanPublisher::CanPublisher(std::string interfaceName) : interfaceName_(std::move(interfaceName)) {}

CanPublisher::~CanPublisher() {
    if (socketFd_ >= 0) {
        ::close(socketFd_);
    }
}

bool CanPublisher::open() {
    socketFd_ = ::socket(PF_CAN, SOCK_RAW, CAN_RAW);
    if (socketFd_ < 0) {
        std::perror("socket(PF_CAN)");
        return false;
    }

    ifreq ifr{};
    std::strncpy(ifr.ifr_name, interfaceName_.c_str(), IFNAMSIZ - 1);
    if (::ioctl(socketFd_, SIOCGIFINDEX, &ifr) < 0) {
        std::cerr << "SocketCAN interface '" << interfaceName_ << "' not found\n";
        ::close(socketFd_);
        socketFd_ = -1;
        return false;
    }

    sockaddr_can address{};
    address.can_family = AF_CAN;
    address.can_ifindex = ifr.ifr_ifindex;
    if (::bind(socketFd_, reinterpret_cast<sockaddr*>(&address), sizeof(address)) < 0) {
        std::perror("bind(SocketCAN)");
        ::close(socketFd_);
        socketFd_ = -1;
        return false;
    }
    return true;
}

bool CanPublisher::publish(const VehicleFrame& frame) const {
    if (socketFd_ < 0 || frame.data.size() > CAN_MAX_DLEN) return false;

    can_frame canFrame{};
    canFrame.can_id = frame.id;
    canFrame.can_dlc = static_cast<__u8>(frame.data.size());
    std::copy(frame.data.begin(), frame.data.end(), canFrame.data);
    return ::write(socketFd_, &canFrame, sizeof(canFrame)) == sizeof(canFrame);
}
