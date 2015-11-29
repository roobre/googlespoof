#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "beaconator.h"
#include "wigle.h"

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <interface-monitor>\n", argv[0]);
        exit(1);
    }

    ap_t* list;

    fetch_wigle(&list, 41.3879, 2.1698, 0.0003, 12, 40);
    send_beacons(list, argv[1]);
}