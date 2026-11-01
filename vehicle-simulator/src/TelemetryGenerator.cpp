#include "TelemetryGenerator.h"

#include <algorithm>
#include <cmath>
#include <iomanip>
#include <sstream>

namespace {
constexpr std::uint32_t SPEED_ID = 0x100;
constexpr std::uint32_t BATTERY_ID = 0x101;
constexpr std::uint32_t TEMPERATURE_ID = 0x102;
constexpr std::uint32_t STATUS_ID = 0x103;

std::vector<std::uint8_t> encodeU16(std::uint16_t value) {
    return {
        static_cast<std::uint8_t>((value >> 8) & 0xFF),
        static_cast<std::uint8_t>(value & 0xFF),
    };
}

std::vector<std::uint8_t> encodeI16(std::int16_t value) {
    return encodeU16(static_cast<std::uint16_t>(value));
}
}

std::string VehicleFrame::toText() const {
    std::ostringstream output;
    output << std::uppercase << std::hex << std::setfill('0') << std::setw(3) << id << '#';
    for (const auto byte : data) {
        output << std::setw(2) << static_cast<int>(byte);
    }
    return output.str();
}

std::vector<VehicleFrame> TelemetryGenerator::next() {
    const double t = static_cast<double>(tick_);
    const double speed = std::clamp(38.0 + 30.0 * std::sin(t / 12.0), 0.0, 120.0);
    const int battery = std::max(8, 92 - static_cast<int>(tick_ / 40));
    const double temperature = 21.0 + 6.0 * std::sin(t / 25.0);

    const bool doorOpen = tick_ % 140 >= 48 && tick_ % 140 <= 53;
    const bool charging = tick_ % 180 >= 145;
    const bool warning = tick_ % 220 >= 190 && tick_ % 220 <= 196;
    std::uint8_t status = 0;
    if (doorOpen) status |= 0x01;
    if (charging) status |= 0x02;
    if (warning) status |= 0x04;

    const auto speedRaw = static_cast<std::uint16_t>(std::lround(speed * 10.0));
    const auto tempRaw = static_cast<std::int16_t>(std::lround(temperature * 10.0));
    ++tick_;

    return {
        VehicleFrame{SPEED_ID, encodeU16(speedRaw)},
        VehicleFrame{BATTERY_ID, {static_cast<std::uint8_t>(battery)}},
        VehicleFrame{TEMPERATURE_ID, encodeI16(tempRaw)},
        VehicleFrame{STATUS_ID, {status}},
    };
}
