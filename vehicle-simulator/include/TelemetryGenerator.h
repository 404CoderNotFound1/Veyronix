#pragma once

#include "VehicleFrame.h"
#include <cstddef>
#include <vector>

class TelemetryGenerator {
public:
    std::vector<VehicleFrame> next();

private:
    std::size_t tick_{0};
};
