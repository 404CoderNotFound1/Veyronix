#pragma once

#include <cstdint>
#include <string>
#include <vector>

struct VehicleFrame {
    std::uint32_t id{};
    std::vector<std::uint8_t> data;

    std::string toText() const;
};
