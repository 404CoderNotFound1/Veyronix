#include "TelemetryGenerator.h"

#include <cassert>
#include <iostream>

int main() {
    TelemetryGenerator generator;
    const auto frames = generator.next();
    assert(frames.size() == 4);
    assert(frames[0].id == 0x100 && frames[0].data.size() == 2);
    assert(frames[1].id == 0x101 && frames[1].data.size() == 1);
    assert(frames[2].id == 0x102 && frames[2].data.size() == 2);
    assert(frames[3].id == 0x103 && frames[3].data.size() == 1);
    assert(frames[0].toText().find("100#") == 0);
    std::cout << "simulator_tests passed\n";
    return 0;
}
