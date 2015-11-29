//
// Created by roobre on 11/28/15.
//

#ifndef BEACONATOR_BEACONATOR_H
#define BEACONATOR_BEACONATOR_H

#include <stdint.h>
#include <stdbool.h>

struct ap {
    struct ap* next;
    uint8_t bssid[6];
    char essid[32];
};

typedef struct ap ap_t;

const extern int DELAY;

extern bool gen;

void send_beacons(ap_t* list, const char* interface);
static void trap_sigint(int signal);

#endif //BEACONATOR_BEACONATOR_H
