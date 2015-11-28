//
// Created by roobre on 11/28/15.
//

#ifndef BEACONATOR_BEACONATOR_H
#define BEACONATOR_BEACONATOR_H

#include <stdint.h>

struct ap {
    struct ap* next;
    uint8_t bssid[6];
    char essid[32];
};

typedef struct ap ap_t;

const extern int DELAY;

void send_beacons(const char* interface, ap_t* list);

#endif //BEACONATOR_BEACONATOR_H
